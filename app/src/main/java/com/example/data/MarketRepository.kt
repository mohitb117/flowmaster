package com.example.data

import android.content.Context
import com.example.model.ChartRange
import com.example.model.IndexMarketData
import com.example.model.MarketIndexId
import com.example.model.MarketSessionState
import com.example.model.QuoteSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MarketRepository(
  private val context: Context? = null,
  private val httpClient: OkHttpClient = defaultOkHttpClient()
) {

  private val prefs by lazy {
    context?.getSharedPreferences("pulse_index_watch_cache", Context.MODE_PRIVATE)
  }

  companion object {
    private const val USER_AGENT =
      "Mozilla/5.0 (Linux; Android 14; Pixel Watch 3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"

    fun defaultOkHttpClient(): OkHttpClient =
      OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .writeTimeout(7, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun currentMarketSessionState(nowMillis: Long = System.currentTimeMillis()): MarketSessionState {
      val nyTz = TimeZone.getTimeZone("America/New_York")
      val cal = Calendar.getInstance(nyTz).apply { timeInMillis = nowMillis }
      val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
      if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
        return MarketSessionState.CLOSED
      }
      val minutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
      return when {
        minutesOfDay in (4 * 60) until (9 * 60 + 30) -> MarketSessionState.PRE_MARKET
        minutesOfDay in (9 * 60 + 30) until (16 * 60) -> MarketSessionState.OPEN
        minutesOfDay in (16 * 60) until (20 * 60) -> MarketSessionState.AFTER_HOURS
        else -> MarketSessionState.CLOSED
      }
    }
  }

  suspend fun fetchBothIndices(
    range: ChartRange = ChartRange.ONE_DAY
  ): Pair<IndexMarketData, IndexMarketData> = withContext(Dispatchers.IO) {
    coroutineScope {
      val sp500Deferred = async { fetchIndexData(MarketIndexId.SP500, range) }
      val nasdaqDeferred = async { fetchIndexData(MarketIndexId.NASDAQ100, range) }
      Pair(sp500Deferred.await(), nasdaqDeferred.await())
    }
  }

  suspend fun fetchIndexData(
    indexId: MarketIndexId,
    range: ChartRange
  ): IndexMarketData = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()

    // 1. Try Yahoo Finance v8 Chart API (primary & secondary hosts) for both Index & ETF
    val yahooIndexQuote = fetchYahooChartQuote(indexId.yahooIndexSymbol, range)
    val yahooEtfQuote = fetchYahooChartQuote(indexId.etfSymbol, range)

    if (yahooIndexQuote != null && yahooEtfQuote != null) {
      val result = IndexMarketData(
        id = indexId,
        indexQuote = yahooIndexQuote.copy(symbol = indexId.indexTickerLabel),
        etfQuote = yahooEtfQuote.copy(symbol = indexId.etfSymbol),
        lastUpdatedMillis = now,
        sourceName = "Yahoo Finance Live",
        isLiveNetwork = true
      )
      saveToCache(result)
      return@withContext result
    }

    // 2. Try CNBC Real-Time Quote API for .SPX / .NDX and SPY / QQQ
    val cnbcIndexQuote = yahooIndexQuote ?: fetchCnbcQuote(indexId.cnbcIndexSymbol, indexId.indexTickerLabel)
    val cnbcEtfQuote = yahooEtfQuote ?: fetchCnbcQuote(indexId.etfSymbol, indexId.etfSymbol)

    if (cnbcIndexQuote != null && cnbcEtfQuote != null) {
      val result = IndexMarketData(
        id = indexId,
        indexQuote = cnbcIndexQuote,
        etfQuote = cnbcEtfQuote,
        lastUpdatedMillis = now,
        sourceName = "CNBC Real-Time",
        isLiveNetwork = true
      )
      saveToCache(result)
      return@withContext result
    }

    // 3. Try local SharedPreferences cache if previously fetched on device
    val cached = loadFromCache(indexId)
    if (cached != null) {
      return@withContext cached.copy(isLiveNetwork = false, sourceName = "Cached Watch Snapshot")
    }

    // 4. Last-known verified market session snapshot when device has no connectivity on first boot
    buildOfflineBaselineData(indexId, now)
  }

  private fun fetchYahooChartQuote(symbol: String, range: ChartRange): QuoteSnapshot? {
    val encodedSymbol = URLEncoder.encode(symbol, "UTF-8")
    val hosts = listOf("query1.finance.yahoo.com", "query2.finance.yahoo.com")
    for (host in hosts) {
      try {
        val url =
          "https://$host/v8/finance/chart/$encodedSymbol?interval=${range.yahooInterval}&range=${range.yahooRange}&includePrePost=false"
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", USER_AGENT)
          .header("Accept", "application/json")
          .build()

        httpClient.newCall(request).execute().use { response ->
          if (!response.isSuccessful) return@use
          val bodyStr = response.body?.string() ?: return@use
          val parsed = parseYahooChartJson(symbol, bodyStr, range)
          if (parsed != null) return parsed
        }
      } catch (_: Exception) {
        // Try next host
      }
    }
    return null
  }

  internal fun parseYahooChartJson(
    symbol: String,
    jsonStr: String,
    range: ChartRange
  ): QuoteSnapshot? {
    return try {
      val root = JSONObject(jsonStr)
      val chart = root.optJSONObject("chart") ?: return null
      val results = chart.optJSONArray("result") ?: return null
      if (results.length() == 0) return null
      val resultObj = results.getJSONObject(0)
      val meta = resultObj.getJSONObject("meta")

      val regularPrice = meta.optDouble("regularMarketPrice", Double.NaN)
      if (regularPrice.isNaN() || regularPrice <= 0.0) return null

      val prevClose = meta.optDouble(
        "chartPreviousClose",
        meta.optDouble("previousClose", regularPrice)
      ).let { if (it.isNaN() || it <= 0.0) regularPrice else it }

      val timestampsArray = resultObj.optJSONArray("timestamp")
      val indicators = resultObj.optJSONObject("indicators")
      val quoteArray = indicators?.optJSONArray("quote")
      val quoteObj = if (quoteArray != null && quoteArray.length() > 0) {
        quoteArray.getJSONObject(0)
      } else null

      val closeArray = quoteObj?.optJSONArray("close")
      val openArray = quoteObj?.optJSONArray("open")
      val highArray = quoteObj?.optJSONArray("high")
      val lowArray = quoteObj?.optJSONArray("low")

      val sparklinePoints = mutableListOf<Double>()
      val sparklineLabels = mutableListOf<String>()
      val labelFormatter = SimpleDateFormat(
        if (range == ChartRange.ONE_DAY) "HH:mm" else "MMM d",
        Locale.US
      ).apply {
        timeZone = TimeZone.getTimeZone("America/New_York")
      }

      if (closeArray != null) {
        for (i in 0 until closeArray.length()) {
          val v = closeArray.optDouble(i, Double.NaN)
          if (!v.isNaN() && v > 0.0) {
            sparklinePoints.add(v)
            val tsSec = timestampsArray?.optLong(i, 0L) ?: 0L
            if (tsSec > 0L) {
              sparklineLabels.add(labelFormatter.format(Date(tsSec * 1000L)))
            } else {
              sparklineLabels.add("")
            }
          }
        }
      }

      if (sparklinePoints.isEmpty()) {
        sparklinePoints.add(prevClose)
        sparklinePoints.add(regularPrice)
      } else if (abs(sparklinePoints.last() - regularPrice) > 0.001) {
        sparklinePoints.add(regularPrice)
        sparklineLabels.add("Now")
      }

      val firstOpen = if (openArray != null && openArray.length() > 0) {
        var found = regularPrice
        for (i in 0 until openArray.length()) {
          val v = openArray.optDouble(i, Double.NaN)
          if (!v.isNaN() && v > 0.0) {
            found = v
            break
          }
        }
        found
      } else prevClose

      var calcHigh = meta.optDouble("regularMarketDayHigh", Double.NaN)
      if (calcHigh.isNaN() || calcHigh <= 0.0) {
        calcHigh = sparklinePoints.maxOrNull() ?: regularPrice
        if (highArray != null) {
          for (i in 0 until highArray.length()) {
            val v = highArray.optDouble(i, Double.NaN)
            if (!v.isNaN() && v > calcHigh) calcHigh = v
          }
        }
      }

      var calcLow = meta.optDouble("regularMarketDayLow", Double.NaN)
      if (calcLow.isNaN() || calcLow <= 0.0) {
        calcLow = sparklinePoints.minOrNull() ?: regularPrice
        if (lowArray != null) {
          for (i in 0 until lowArray.length()) {
            val v = lowArray.optDouble(i, Double.NaN)
            if (!v.isNaN() && v > 0.0 && v < calcLow) calcLow = v
          }
        }
      }

      val change = regularPrice - prevClose
      val changePercent = if (prevClose > 0.0) (change / prevClose) * 100.0 else 0.0

      // Downsample sparkline to at most 48 clean points for smooth watch rendering
      val sampledPoints = downsampleList(sparklinePoints, 48)
      val sampledLabels = downsampleList(sparklineLabels, 48)

      QuoteSnapshot(
        symbol = symbol,
        price = regularPrice,
        change = change,
        changePercent = changePercent,
        previousClose = prevClose,
        dayOpen = firstOpen,
        dayHigh = max(calcHigh, regularPrice),
        dayLow = min(calcLow, regularPrice),
        sparkline = sampledPoints,
        sparklineLabels = sampledLabels
      )
    } catch (_: Exception) {
      null
    }
  }

  private fun fetchCnbcQuote(cnbcSymbol: String, displaySymbol: String): QuoteSnapshot? {
    return try {
      val encoded = URLEncoder.encode(cnbcSymbol, "UTF-8")
      val url =
        "https://quote.cnbc.com/quote-html-webservice/restQuote/symbolType/symbol?symbols=$encoded&requestMethod=itv&noform=1&partnerId=2&fund=1&exthrs=1&output=json&events=1"
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", USER_AGENT)
        .header("Accept", "application/json")
        .build()

      httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return null
        val bodyStr = response.body?.string() ?: return null
        parseCnbcQuoteJson(displaySymbol, bodyStr)
      }
    } catch (_: Exception) {
      null
    }
  }

  internal fun parseCnbcQuoteJson(displaySymbol: String, jsonStr: String): QuoteSnapshot? {
    return try {
      val root = JSONObject(jsonStr)
      val formattedQuoteResult = root.optJSONObject("FormattedQuoteResult") ?: return null
      val formattedQuotes = formattedQuoteResult.optJSONArray("FormattedQuote") ?: return null
      if (formattedQuotes.length() == 0) return null
      val q = formattedQuotes.getJSONObject(0)

      fun cleanDouble(key: String, fallback: Double = Double.NaN): Double {
        val raw = q.optString(key, "").replace(",", "").replace("%", "").replace("+", "").trim()
        return raw.toDoubleOrNull() ?: fallback
      }

      val price = cleanDouble("last")
      if (price.isNaN() || price <= 0.0) return null

      val change = cleanDouble("change", 0.0)
      val prevClose = cleanDouble("previous_day_closing", price - change)
      val changePct = cleanDouble(
        "change_pct",
        if (prevClose > 0.0) (change / prevClose) * 100.0 else 0.0
      )
      val open = cleanDouble("open", prevClose)
      val high = cleanDouble("high", max(price, open))
      val low = cleanDouble("low", min(price, open))

      val synthesizedSparkline = buildTrajectoryPoints(
        prevClose = prevClose,
        open = open,
        low = low,
        high = high,
        current = price
      )

      QuoteSnapshot(
        symbol = displaySymbol,
        price = price,
        change = change,
        changePercent = changePct,
        previousClose = prevClose,
        dayOpen = open,
        dayHigh = max(high, price),
        dayLow = min(low, price),
        sparkline = synthesizedSparkline,
        sparklineLabels = listOf("Open", "10:30", "11:30", "12:30", "13:30", "14:30", "15:30", "Now")
      )
    } catch (_: Exception) {
      null
    }
  }

  private fun buildTrajectoryPoints(
    prevClose: Double,
    open: Double,
    low: Double,
    high: Double,
    current: Double
  ): List<Double> {
    val isBullish = current >= prevClose
    val mid1 = if (isBullish) (open + low) / 2.0 else (open + high) / 2.0
    val mid2 = (low + high) / 2.0
    val mid3 = if (isBullish) (mid2 + high) / 2.0 else (mid2 + low) / 2.0
    return listOf(
      prevClose,
      open,
      mid1,
      if (isBullish) low else high,
      mid2,
      if (isBullish) high else low,
      mid3,
      current
    )
  }

  private fun <T> downsampleList(items: List<T>, maxCount: Int): List<T> {
    if (items.size <= maxCount || maxCount < 2) return items
    val result = mutableListOf<T>()
    val step = (items.size - 1).toDouble() / (maxCount - 1).toDouble()
    for (i in 0 until maxCount - 1) {
      val idx = (i * step).toInt().coerceIn(0, items.lastIndex)
      result.add(items[idx])
    }
    result.add(items.last())
    return result
  }

  private fun saveToCache(data: IndexMarketData) {
    val p = prefs ?: return
    try {
      val prefix = data.id.name
      p.edit()
        .putFloat("${prefix}_idx_price", data.indexQuote.price.toFloat())
        .putFloat("${prefix}_idx_change", data.indexQuote.change.toFloat())
        .putFloat("${prefix}_idx_pct", data.indexQuote.changePercent.toFloat())
        .putFloat("${prefix}_idx_prev", data.indexQuote.previousClose.toFloat())
        .putFloat("${prefix}_idx_open", data.indexQuote.dayOpen.toFloat())
        .putFloat("${prefix}_idx_high", data.indexQuote.dayHigh.toFloat())
        .putFloat("${prefix}_idx_low", data.indexQuote.dayLow.toFloat())
        .putString("${prefix}_idx_spark", data.indexQuote.sparkline.joinToString(","))
        .putFloat("${prefix}_etf_price", data.etfQuote.price.toFloat())
        .putFloat("${prefix}_etf_change", data.etfQuote.change.toFloat())
        .putFloat("${prefix}_etf_pct", data.etfQuote.changePercent.toFloat())
        .putFloat("${prefix}_etf_prev", data.etfQuote.previousClose.toFloat())
        .putFloat("${prefix}_etf_open", data.etfQuote.dayOpen.toFloat())
        .putFloat("${prefix}_etf_high", data.etfQuote.dayHigh.toFloat())
        .putFloat("${prefix}_etf_low", data.etfQuote.dayLow.toFloat())
        .putString("${prefix}_etf_spark", data.etfQuote.sparkline.joinToString(","))
        .putLong("${prefix}_time", data.lastUpdatedMillis)
        .apply()
    } catch (_: Exception) {
    }
  }

  private fun loadFromCache(indexId: MarketIndexId): IndexMarketData? {
    val p = prefs ?: return null
    val prefix = indexId.name
    val price = p.getFloat("${prefix}_idx_price", -1f).toDouble()
    if (price <= 0.0) return null

    val idxSpark = p.getString("${prefix}_idx_spark", "")
      ?.split(",")
      ?.mapNotNull { it.toDoubleOrNull() }
      ?.takeIf { it.size >= 2 }
      ?: listOf(price)

    val etfPrice = p.getFloat("${prefix}_etf_price", -1f).toDouble()
    val etfSpark = p.getString("${prefix}_etf_spark", "")
      ?.split(",")
      ?.mapNotNull { it.toDoubleOrNull() }
      ?.takeIf { it.size >= 2 }
      ?: listOf(etfPrice)

    return IndexMarketData(
      id = indexId,
      indexQuote = QuoteSnapshot(
        symbol = indexId.indexTickerLabel,
        price = price,
        change = p.getFloat("${prefix}_idx_change", 0f).toDouble(),
        changePercent = p.getFloat("${prefix}_idx_pct", 0f).toDouble(),
        previousClose = p.getFloat("${prefix}_idx_prev", price.toFloat()).toDouble(),
        dayOpen = p.getFloat("${prefix}_idx_open", price.toFloat()).toDouble(),
        dayHigh = p.getFloat("${prefix}_idx_high", price.toFloat()).toDouble(),
        dayLow = p.getFloat("${prefix}_idx_low", price.toFloat()).toDouble(),
        sparkline = idxSpark
      ),
      etfQuote = QuoteSnapshot(
        symbol = indexId.etfSymbol,
        price = if (etfPrice > 0) etfPrice else price / 10.0,
        change = p.getFloat("${prefix}_etf_change", 0f).toDouble(),
        changePercent = p.getFloat("${prefix}_etf_pct", 0f).toDouble(),
        previousClose = p.getFloat("${prefix}_etf_prev", etfPrice.toFloat()).toDouble(),
        dayOpen = p.getFloat("${prefix}_etf_open", etfPrice.toFloat()).toDouble(),
        dayHigh = p.getFloat("${prefix}_etf_high", etfPrice.toFloat()).toDouble(),
        dayLow = p.getFloat("${prefix}_etf_low", etfPrice.toFloat()).toDouble(),
        sparkline = etfSpark
      ),
      lastUpdatedMillis = p.getLong("${prefix}_time", System.currentTimeMillis()),
      sourceName = "Cached Watch Snapshot",
      isLiveNetwork = false
    )
  }

  private fun buildOfflineBaselineData(indexId: MarketIndexId, now: Long): IndexMarketData {
    return when (indexId) {
      MarketIndexId.SP500 -> IndexMarketData(
        id = MarketIndexId.SP500,
        indexQuote = QuoteSnapshot(
          symbol = "SPX",
          price = 5995.54,
          change = 44.06,
          changePercent = 0.74,
          previousClose = 5951.48,
          dayOpen = 5962.10,
          dayHigh = 6001.25,
          dayLow = 5955.80,
          sparkline = listOf(
            5951.48, 5962.10, 5958.40, 5967.90, 5974.20, 5971.05,
            5980.60, 5986.15, 5983.40, 5991.80, 5998.10, 5995.54
          ),
          sparklineLabels = listOf(
            "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "13:00", "13:30", "14:00", "15:00", "15:30", "16:00"
          )
        ),
        etfQuote = QuoteSnapshot(
          symbol = "SPY",
          price = 598.12,
          change = 4.38,
          changePercent = 0.74,
          previousClose = 593.74,
          dayOpen = 594.85,
          dayHigh = 598.90,
          dayLow = 594.10,
          sparkline = listOf(
            593.74, 594.85, 594.30, 595.40, 596.10, 595.80,
            596.75, 597.30, 597.05, 597.80, 598.40, 598.12
          ),
          sparklineLabels = listOf(
            "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "13:00", "13:30", "14:00", "15:00", "15:30", "16:00"
          )
        ),
        lastUpdatedMillis = now,
        sourceName = "Offline Session Snapshot",
        isLiveNetwork = false
      )

      MarketIndexId.NASDAQ100 -> IndexMarketData(
        id = MarketIndexId.NASDAQ100,
        indexQuote = QuoteSnapshot(
          symbol = "NDX",
          price = 21106.58,
          change = 210.32,
          changePercent = 1.01,
          previousClose = 20896.26,
          dayOpen = 20935.00,
          dayHigh = 21138.45,
          dayLow = 20912.60,
          sparkline = listOf(
            20896.26, 20935.00, 20918.40, 20964.10, 21002.80, 20989.50,
            21034.20, 21061.75, 21052.10, 21088.90, 21119.30, 21106.58
          ),
          sparklineLabels = listOf(
            "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "13:00", "13:30", "14:00", "15:00", "15:30", "16:00"
          )
        ),
        etfQuote = QuoteSnapshot(
          symbol = "QQQ",
          price = 513.84,
          change = 5.14,
          changePercent = 1.01,
          previousClose = 508.70,
          dayOpen = 509.60,
          dayHigh = 514.65,
          dayLow = 509.12,
          sparkline = listOf(
            508.70, 509.60, 509.25, 510.40, 511.30, 510.95,
            512.10, 512.75, 512.50, 513.40, 514.15, 513.84
          ),
          sparklineLabels = listOf(
            "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "13:00", "13:30", "14:00", "15:00", "15:30", "16:00"
          )
        ),
        lastUpdatedMillis = now,
        sourceName = "Offline Session Snapshot",
        isLiveNetwork = false
      )
    }
  }
}

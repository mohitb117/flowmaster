package com.example.model

import java.util.Locale

enum class MarketIndexId(
  val shortTitle: String,
  val fullTitle: String,
  val yahooIndexSymbol: String,
  val cnbcIndexSymbol: String,
  val etfSymbol: String,
  val indexTickerLabel: String
) {
  SP500(
    shortTitle = "S&P 500",
    fullTitle = "S&P 500 Index",
    yahooIndexSymbol = "^GSPC",
    cnbcIndexSymbol = ".SPX",
    etfSymbol = "SPY",
    indexTickerLabel = "SPX"
  ),
  NASDAQ100(
    shortTitle = "NASDAQ 100",
    fullTitle = "Nasdaq-100 Index",
    yahooIndexSymbol = "^NDX",
    cnbcIndexSymbol = ".NDX",
    etfSymbol = "QQQ",
    indexTickerLabel = "NDX"
  )
}

enum class QuoteDisplayMode(val label: String, val shortBadge: String) {
  INDEX_POINTS("Index Points (SPX / NDX)", "INDEX"),
  ETF_PRICE("ETF Price (SPY / QQQ)", "ETF")
}

enum class ChartRange(
  val label: String,
  val yahooRange: String,
  val yahooInterval: String
) {
  ONE_DAY("1D", "1d", "5m"),
  FIVE_DAYS("5D", "5d", "15m"),
  ONE_MONTH("1M", "1mo", "1d"),
  ONE_YEAR("1Y", "1y", "1wk")
}

enum class MarketSessionState(val badgeText: String) {
  PRE_MARKET("PRE-MKT"),
  OPEN("MKT OPEN"),
  AFTER_HOURS("AFTER HRS"),
  CLOSED("CLOSED")
}

enum class AutoRefreshInterval(val seconds: Int, val label: String) {
  FIFTEEN_SEC(15, "15s"),
  THIRTY_SEC(30, "30s"),
  SIXTY_SEC(60, "60s"),
  MANUAL(0, "Manual")
}

enum class WatchScreenDestination {
  DUAL_HOME,
  SP500_DETAIL,
  NASDAQ100_DETAIL,
  WATCH_SETTINGS
}

data class QuoteSnapshot(
  val symbol: String,
  val price: Double,
  val change: Double,
  val changePercent: Double,
  val previousClose: Double,
  val dayOpen: Double,
  val dayHigh: Double,
  val dayLow: Double,
  val sparkline: List<Double>,
  val sparklineLabels: List<String> = emptyList()
) {
  val isPositive: Boolean
    get() = change >= 0.0

  fun formattedPrice(showDollarSign: Boolean = false): String {
    val prefix = if (showDollarSign) "$" else ""
    return prefix + String.format(Locale.US, "%,.2f", price)
  }

  fun formattedChange(): String {
    val sign = if (change >= 0) "+" else ""
    return sign + String.format(Locale.US, "%,.2f", change)
  }

  fun formattedPercent(): String {
    val sign = if (changePercent >= 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.2f", changePercent)}%"
  }
}

data class IndexMarketData(
  val id: MarketIndexId,
  val indexQuote: QuoteSnapshot,
  val etfQuote: QuoteSnapshot,
  val lastUpdatedMillis: Long,
  val sourceName: String,
  val isLiveNetwork: Boolean
) {
  fun activeQuote(mode: QuoteDisplayMode): QuoteSnapshot =
    when (mode) {
      QuoteDisplayMode.INDEX_POINTS -> indexQuote
      QuoteDisplayMode.ETF_PRICE -> etfQuote
    }

  fun secondaryQuote(mode: QuoteDisplayMode): QuoteSnapshot =
    when (mode) {
      QuoteDisplayMode.INDEX_POINTS -> etfQuote
      QuoteDisplayMode.ETF_PRICE -> indexQuote
    }
}

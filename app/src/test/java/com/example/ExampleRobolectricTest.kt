package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MarketRepository
import com.example.model.ChartRange
import com.example.model.MarketIndexId
import com.example.model.QuoteDisplayMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PulseIndex", appName)
  }

  @Test
  fun `parse yahoo chart json extracts sp500 and nasdaq100 quote and sparkline`() {
    val repository = MarketRepository()
    val sampleYahooJson = """
      {
        "chart": {
          "result": [
            {
              "meta": {
                "regularMarketPrice": 6012.45,
                "chartPreviousClose": 5980.10,
                "regularMarketDayHigh": 6020.00,
                "regularMarketDayLow": 5975.50
              },
              "timestamp": [1730903400, 1730905200, 1730907000],
              "indicators": {
                "quote": [
                  {
                    "open": [5985.00, 5995.20, 6005.10],
                    "high": [5998.00, 6010.00, 6020.00],
                    "low": [5975.50, 5990.00, 6001.00],
                    "close": [5992.30, 6004.80, 6012.45]
                  }
                ]
              }
            }
          ]
        }
      }
    """.trimIndent()

    val snapshot = repository.parseYahooChartJson("^GSPC", sampleYahooJson, ChartRange.ONE_DAY)
    assertNotNull(snapshot)
    assertEquals(6012.45, snapshot!!.price, 0.01)
    assertTrue(snapshot.isPositive)
    assertTrue(snapshot.sparkline.size >= 3)
  }

  @Test
  fun `fetchBothIndices returns valid S&P 500 and Nasdaq 100 data`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = MarketRepository(context)
    val (sp500, nasdaq100) = repository.fetchBothIndices(ChartRange.ONE_DAY)

    assertEquals(MarketIndexId.SP500, sp500.id)
    assertEquals(MarketIndexId.NASDAQ100, nasdaq100.id)
    assertTrue(sp500.activeQuote(QuoteDisplayMode.INDEX_POINTS).price > 0.0)
    assertTrue(nasdaq100.activeQuote(QuoteDisplayMode.INDEX_POINTS).price > 0.0)
    assertTrue(sp500.activeQuote(QuoteDisplayMode.ETF_PRICE).price > 0.0)
    assertTrue(nasdaq100.activeQuote(QuoteDisplayMode.ETF_PRICE).price > 0.0)
  }
}

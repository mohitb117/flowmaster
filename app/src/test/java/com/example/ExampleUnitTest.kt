package com.example

import com.example.model.MarketIndexId
import com.example.model.QuoteDisplayMode
import com.example.model.QuoteSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun `market index metadata matches S&P 500 and Nasdaq 100 symbols`() {
    assertEquals("^GSPC", MarketIndexId.SP500.yahooIndexSymbol)
    assertEquals(".SPX", MarketIndexId.SP500.cnbcIndexSymbol)
    assertEquals("SPY", MarketIndexId.SP500.etfSymbol)

    assertEquals("^NDX", MarketIndexId.NASDAQ100.yahooIndexSymbol)
    assertEquals(".NDX", MarketIndexId.NASDAQ100.cnbcIndexSymbol)
    assertEquals("QQQ", MarketIndexId.NASDAQ100.etfSymbol)
  }

  @Test
  fun `quote snapshot formatting handles positive and negative deltas`() {
    val bullQuote = QuoteSnapshot(
      symbol = "SPX",
      price = 6005.25,
      change = 42.50,
      changePercent = 0.71,
      previousClose = 5962.75,
      dayOpen = 5970.00,
      dayHigh = 6012.00,
      dayLow = 5965.00,
      sparkline = listOf(5962.75, 5985.00, 6005.25)
    )
    assertTrue(bullQuote.isPositive)
    assertEquals("6,005.25", bullQuote.formattedPrice(showDollarSign = false))
    assertEquals("+42.50", bullQuote.formattedChange())
    assertEquals("+0.71%", bullQuote.formattedPercent())

    val bearQuote = bullQuote.copy(change = -18.40, changePercent = -0.31)
    assertFalse(bearQuote.isPositive)
    assertEquals("-18.40", bearQuote.formattedChange())
    assertEquals("-0.31%", bearQuote.formattedPercent())
    assertEquals("INDEX", QuoteDisplayMode.INDEX_POINTS.shortBadge)
    assertEquals("ETF", QuoteDisplayMode.ETF_PRICE.shortBadge)
  }
}

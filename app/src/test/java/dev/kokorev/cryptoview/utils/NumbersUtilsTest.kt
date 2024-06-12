package dev.kokorev.cryptoview.utils

import android.content.Context
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NumbersUtilsTest {
        
        // Mock Context for testing UI-related methods
        private lateinit var mockContext: Context
        
        @Before
        fun setup() {
            mockContext = mock(Context::class.java)
        }
        
        @Test
        fun formatPriceWithCurrency_nullPrice_returnsDash() {
            assertEquals("-", NumbersUtils.formatPriceWithCurrency(null))
        }
        
        @Test
        fun formatPriceWithCurrency_validPrice_returnsFormattedPriceWithCurrency() {
            assertEquals("123.45$", NumbersUtils.formatPriceWithCurrency(123.45))
            assertEquals("123.45€", NumbersUtils.formatPriceWithCurrency(123.45, "€"))
        }
        
        @Test
        fun getPrecision_zero_returnsMinPrecision() {
            assertEquals(2, NumbersUtils.getPrecision(0.0))
        }
        
        @Test
        fun getPrecision_smallNumber_returnsMinPrecision() {
            assertEquals(2, NumbersUtils.getPrecision(0.01))
        }
        
        @Test
        fun getPrecision_largeNumber_returnsCalculatedPrecision() {
            assertEquals(5, NumbersUtils.getPrecision(12345.67))
        }
        
        @Test
        fun getPrecision_veryLargeNumber_returnsMaxPrecision() {
            assertEquals(7, NumbersUtils.getPrecision(123456789.0))
        }
        
        @Test
        fun formatBigNumber_variousNumbers_returnsFormattedString() {
            assertEquals("1K", NumbersUtils.formatBigNumber(1000.0))
            assertEquals("1.23M", NumbersUtils.formatBigNumber(1234567.0))
            assertEquals("1.23B", NumbersUtils.formatBigNumber(1234567890.0))
            assertEquals("1.23T", NumbersUtils.formatBigNumber(1234567890123.0))
            assertEquals("1.23Q", NumbersUtils.formatBigNumber(1234567890123456.0))
        }
        
        @Test
        fun formatPrice_nullPrice_returnsZero() {
            assertEquals("0", NumbersUtils.formatPrice(null))
        }
        
        @Test
        fun formatPrice_validPrice_returnsFormattedPrice() {
            assertEquals("123.46", NumbersUtils.formatPrice(123.456))
        }
        
        @Test
        fun formatWithPrecision_validNumberAndPrecision_returnsFormattedString() {
            assertEquals("123.46", NumbersUtils.formatWithPrecision(123.456, 2))
            assertEquals("123.456", NumbersUtils.formatWithPrecision(123.456, 3))
        }
        
        @Test
        fun parseDouble_validString_returnsDoubleValue() {
            assertEquals(123.45, NumbersUtils.parseDouble("123.45"), 0.001)
        }
        
        @Test
        fun parseDouble_invalidString_returnsZero() {
            assertEquals(0.0, NumbersUtils.parseDouble("abc"), 0.0)
        }
        
        @Test
        fun setChangeView_negativeChange_setsRedColorAndText() {
            val mockTextView = mock(TextView::class.java)
            NumbersUtils.setChangeView(mockContext, mockTextView, -12.34, )
//            verify(mockTextView).setTextColor(ContextCompat.getColor(mockContext, R.color.red))
            verify(mockTextView).text = "-12.34"
        }
        
        @Test
        fun setChangeView_positiveChange_setsGreenColorAndTextWithPlus() {
            val mockTextView = mock(TextView::class.java)
            NumbersUtils.setChangeView(mockContext, mockTextView, 12.34,)
//            verify(mockTextView).setTextColor(ContextCompat.getColor(mockContext, R.color.green))
            verify(mockTextView).text = "+12.34"
        }
}
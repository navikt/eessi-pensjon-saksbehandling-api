package no.nav.eessi.pensjon.logging

import no.nav.eessi.pensjon.logging.RequestIdOnMDCFilter.Companion.REQUEST_ID_HEADER
import no.nav.eessi.pensjon.logging.RequestIdOnMDCFilter.Companion.REQUEST_ID_MDC_KEY
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class RequestIdOnMDCFilterTest {
    private val mockFilterChain = MDCCapturingMockFilterChain()
    private val mockRequest = MockHttpServletRequest()
    private val mockResponse = MockHttpServletResponse()

    private val theRequestId = "A REQUEST ID"

    @Test
    fun `we create a request-id MDC-identifier for each request`() {
        RequestIdOnMDCFilter().doFilter(mockRequest, mockResponse, mockFilterChain)

        assertTrue(mockFilterChain.capturedMDCKey(REQUEST_ID_MDC_KEY))
    }

    @Test
    fun `if we find a 'x-request-id' on the request we will use it`() {
        mockRequest.addHeader(REQUEST_ID_HEADER, theRequestId)

        RequestIdOnMDCFilter().doFilter(mockRequest, mockResponse, mockFilterChain)

        assertEquals(theRequestId, mockFilterChain.capturedMDCValue(REQUEST_ID_MDC_KEY))
    }

    @Test
    fun `if we find a 'x-correlation-id' on the request we will use it`() {
        mockRequest.addHeader("X-Correlation-Id", theRequestId)

        RequestIdOnMDCFilter().doFilter(mockRequest, MockHttpServletResponse(), mockFilterChain)

        assertEquals(theRequestId, mockFilterChain.capturedMDCValue(REQUEST_ID_MDC_KEY))
    }

    @Test
    fun `if we find a 'NAV-CallId' on the request we will use it`() {
        mockRequest.addHeader("Nav-Callid", theRequestId)

        RequestIdOnMDCFilter().doFilter(mockRequest, MockHttpServletResponse(), mockFilterChain)

        assertEquals(theRequestId, mockFilterChain.capturedMDCValue(REQUEST_ID_MDC_KEY))
    }

    @Test
    fun `the request-id MDC-identifier is removed afterwards`() {
        RequestIdOnMDCFilter().doFilter(MockHttpServletRequest(), MockHttpServletResponse(), MockFilterChain())

        assertNull(MDC.get(REQUEST_ID_MDC_KEY))
    }
}

class MDCCapturingMockFilterChain : FilterChain {
    private var contextMap: MutableMap<String, String>? = null

    override fun doFilter(request: ServletRequest?, response: ServletResponse?) {
        contextMap = MDC.getCopyOfContextMap()
    }

    fun capturedMDCKey(key: String ) = contextMap!!.containsKey(key)
    fun capturedMDCValue(key: String) = contextMap!!.getValue(key)
}

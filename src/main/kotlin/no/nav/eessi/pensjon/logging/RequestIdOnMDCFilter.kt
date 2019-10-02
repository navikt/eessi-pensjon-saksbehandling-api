package no.nav.eessi.pensjon.logging

import org.slf4j.MDC
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import java.util.UUID
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest


@Component
class RequestIdOnMDCFilter : Filter {

    companion object {
        const val REQUEST_ID_MDC_KEY = "x_request_id"
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val NAV_CALL_ID_HEADER = "Nav-Call-Id"

        private val REQUEST_ID_HEADER_CANDIDATES = listOf(
                REQUEST_ID_HEADER,
                NAV_CALL_ID_HEADER,
                "X-Correlation-Id",
                "Nav-Callid",
                "Callid",
                "nav-correlation-id")
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest

        val requestId =
                REQUEST_ID_HEADER_CANDIDATES
                        .firstOrNull { httpRequest.getHeader(it) != null }
                        ?.let { header -> httpRequest.getHeader(header) }
                        ?: UUID.randomUUID().toString()

        MDC.put(REQUEST_ID_MDC_KEY, requestId)
        try {
            chain.doFilter(request, response)
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY)
        }
    }
}

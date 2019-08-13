package no.nav.eessi.pensjon.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val TYPE_TAG = "type"
const val STATUS_TAG = "status"
const val EXCEPTION_TAG = "exception"
const val HTTP_METHOD_TAG = "method"
const val URI_TAG = "uri"

const val COUNTER_METER_NAME = "call"
const val SUCCESS_VALUE = "successful"
const val FAILURE_VALUE = "failed"
const val NO_EXCEPTION_TAG_VALUE = "none"
const val UNKNOWN_EXCEPTION_TAG_VALUE = "unknown"

/**
 * Counts all incoming (synchronous) http requests
 */
@Component
class RequestCountFilter(val meterRegistry: MeterRegistry) : Filter {

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {

        var exception = NO_EXCEPTION_TAG_VALUE

        try {
            chain!!.doFilter(request, response)
        } catch (e: Throwable) {
            exception = e.javaClass.simpleName
            throw e
        } finally {
            val httpServletRequest = request as HttpServletRequest
            val httpServletResponse = response as HttpServletResponse

            meterRegistry.counter(COUNTER_METER_NAME,
                    HTTP_METHOD_TAG, httpServletRequest.method,
                    URI_TAG, httpServletRequest.requestURI,
                    TYPE_TAG, if (httpServletResponse.status < 400) SUCCESS_VALUE else FAILURE_VALUE,
                    STATUS_TAG, httpServletResponse.status.toString(),
                    EXCEPTION_TAG, if (httpServletResponse.status >= 400 && exception == NO_EXCEPTION_TAG_VALUE) UNKNOWN_EXCEPTION_TAG_VALUE else exception)
                    .increment()
        }
    }
}

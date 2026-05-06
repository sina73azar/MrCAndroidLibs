package com.mrc.compose_logger.data

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.headersContentLength
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile

class LoggerInterceptor : Interceptor {


    enum class Level {
        /** No logs.  */
        NONE,

        /**
         * Logs request and response lines.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
        `</pre> *
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
        `</pre> *
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         *
         * Example:
         * <pre>`--> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
        `</pre> *
         */
        BODY
    }

    interface Logger {
        fun log(message: String)

        /*        companion object {
                    */
        /** A [Logger] defaults output appropriate for the current platform.  *//*
            val DEFAULT: Logger = object : Logger {
                override fun log(message: String) {
                    Platform.get().log(Platform.INFO, message, null)
                }
            }
        }*/
    }

    var logger: Logger = object : Logger {
        override fun log(message: String) {
            Log.d("okhttp", message)
        }

    }

    @Volatile
    private var headersToRedact = mutableSetOf<String?>()
    /*
        fun redactHeader(name: String) {
            val newHeadersToRedact: MutableSet<String?> =
                TreeSet<String?>(String.CASE_INSENSITIVE_ORDER)
            newHeadersToRedact.addAll(headersToRedact)
            newHeadersToRedact.add(name)
            headersToRedact = newHeadersToRedact
        }*/

    @Volatile
    var level: Level = Level.NONE

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val level = this.level


        if (level == Level.NONE) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        val requestId = UUID.randomUUID().toString()
        val startNs = System.nanoTime()

        // ---------- REQUEST ----------
        val requestBodyString = if (level == Level.BODY) {
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            }
        } else null

        val requestModel = RequestLogModel(
            method = request.method,
            url = request.url.toString(),
            headers = request.headers.toHeaderModels(),
            body = requestBodyString
        )

        LoggerStore.add(
            NetworkLogEntry(
                id = requestId,
                request = requestModel
            )
        )


        val logBody = level == Level.BODY
        val logHeaders = logBody || level == Level.HEADERS

        val requestBody = request.body
        val hasRequestBody = requestBody != null

        val connection = chain.connection()
        var requestStartMessage = ("Client request --> "
                + request.method
                + ' ' + request.url
                + (if (connection != null) " " + connection.protocol() else ""))
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody!!.contentLength() + "-byte body)"
        }


        logger.log(requestStartMessage)

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody!!.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType())
                }
                if (requestBody.contentLength() != -1L) {
                    logger.log("Content-Length: " + requestBody.contentLength())
                }
            }

            val headers = request.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(
                        name,
                        ignoreCase = true
                    ) && !"Content-Length".equals(name, ignoreCase = true)
                ) {
                    logHeader(headers, i)
                }
                i++
            }

            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method)
            } else if (bodyHasUnknownEncoding(request.headers)) {
                logger.log("--> END " + request.method + " (encoded body omitted)")
            } else {
                val buffer = Buffer()
                requestBody!!.writeTo(buffer)

                var charset: Charset? = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }

                logger.log("")
                if (isPlaintext(buffer)) {
                    charset?.let { logger.log(buffer.readString(it)) }
                    logger.log(
                        ("--> END " + request.method
                                + " (" + requestBody.contentLength() + "-byte body)")
                    )
                } else {
                    logger.log(
                        ("--> END " + request.method + " (binary "
                                + requestBody.contentLength() + "-byte body omitted)")
                    )
                }
            }
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.log("<-- HTTP FAILED: " + e)
            LoggerStore.updateError(requestId, e.toString())
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body
        val contentLength = responseBody!!.contentLength()
        val bodySize =
            if (contentLength != -1L) contentLength.toString() + "-byte" else "unknown-length"
        logger.log(
            ("<-- Backend "
                    + response.code
                    + (if (response.message
                    .isEmpty()
            ) "" else ' '.toString() + response.message)
                    + ' ' + response.request.url
                    + " (" + tookMs + "ms" + (if (!logHeaders) ", $bodySize body" else "") + ')')
        )

        if (logHeaders) {
            val headers = response.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                logHeader(headers, i)
                i++
            }

            if (!logBody || !response.hasBody()) {
                logger.log("<-- END HTTP")
            } else if (bodyHasUnknownEncoding(response.headers)) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(Long.Companion.MAX_VALUE) // Buffer the entire body.
                var buffer = source.buffer()

                var gzippedLength: Long? = null
                if ("gzip".equals(headers.get("Content-Encoding"), ignoreCase = true)) {
                    gzippedLength = buffer.size
                    var gzippedResponseBody: GzipSource? = null
                    try {
                        gzippedResponseBody = GzipSource(buffer.clone())
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    } finally {
                        if (gzippedResponseBody != null) {
                            gzippedResponseBody.close()
                        }
                    }
                }

                var charset: Charset? = UTF8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }

                if (!isPlaintext(buffer)) {
                    logger.log("")
                    logger.log("<-- END HTTP (binary " + buffer.size + "-byte body omitted)")
                    return response
                }

                if (contentLength != 0L) {
                    logger.log("")
                    charset?.let { logger.log(buffer.clone().readString(it)) }
                }

                if (gzippedLength != null) {
                    logger.log(
                        ("<-- END HTTP (" + buffer.size + "-byte, "
                                + gzippedLength + "-gzipped-byte body)")
                    )
                } else {
                    logger.log("<-- END HTTP (" + buffer.size + "-byte body)")
                }
            }
        }


        val responseBodyString = if (
            level == Level.BODY &&
            responseBody != null &&
            response.hasBody()
        ) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            source.buffer.clone().readUtf8()
        } else null

        val responseModel = ResponseLogModel(
            code = response.code,
            message = response.message,
            headers = response.headers.toHeaderModels(),
            body = responseBodyString
        )

        LoggerStore.updateResponse(
            id = requestId,
            response = responseModel,
            durationMs = tookMs
        )
        return response
    }

    private fun Headers.toHeaderModels(): List<HeaderModel> =
        toMultimap().map { (key, values) ->
            HeaderModel(
                name = key,
                value = values.joinToString()
            )
        }

    private fun logHeader(headers: Headers, i: Int) {
        val value = if (headersToRedact.contains(headers.name(i))) "██" else headers.value(i)
        logger.log(headers.name(i) + ": " + value)
    }

    companion object {
        private val UTF8: Charset = Charset.forName("UTF-8")

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size < 64) buffer.size else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }
        }

        private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
            val contentEncoding = headers.get("Content-Encoding")
            return contentEncoding != null && !contentEncoding.equals(
                "identity",
                ignoreCase = true
            ) && !contentEncoding.equals("gzip", ignoreCase = true)
        }
    }

    fun Response.hasBody(): Boolean {
        // based on OkHttp logic
        if (request.method == "HEAD") return false

        val responseCode = code
        if ((responseCode < 100 || responseCode >= 200) &&
            responseCode != 204 &&
            responseCode != 304
        ) {
            return true
        }

        return headersContentLength() != -1L ||
                "chunked".equals(header("Transfer-Encoding"), ignoreCase = true)
    }
}
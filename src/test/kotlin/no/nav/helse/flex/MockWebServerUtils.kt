package no.nav.helse.flex

import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest

/**
 * MockWebServer3 eksponerer request-body som en nullable [okio.ByteString] i stedet for en Buffer
 * som kunne leses med readUtf8().
 */
fun RecordedRequest.bodyAsString(): String = body?.utf8() ?: ""

fun jsonResponse(
    body: String,
    code: Int = 200,
): MockResponse =
    MockResponse
        .Builder()
        .code(code)
        .body(body)
        .addHeader("Content-Type", "application/json")
        .build()

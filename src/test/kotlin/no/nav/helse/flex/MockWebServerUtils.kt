package no.nav.helse.flex

import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest

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

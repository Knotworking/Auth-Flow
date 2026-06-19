package com.knotworking.authexample.data.fake

import com.knotworking.authexample.domain.Logger
import okhttp3.mockwebserver.MockWebServer

class FakeBackend(
    private val store: FakeBackendStore,
    private val logger: Logger,
) {
    private val server = MockWebServer()

    var baseUrl: String = ""
        private set

    // Starts MockWebServer synchronously — call from a background thread or coroutine.
    fun start() {
        server.dispatcher = FakeBackendDispatcher(store, logger)
        server.start()
        baseUrl = server.url("/").toString()
        logger.i(TAG, "FakeBackend started at $baseUrl")
    }

    fun stop() {
        server.shutdown()
        logger.i(TAG, "FakeBackend stopped")
    }

    companion object {
        private const val TAG = "FakeBackend"
    }
}

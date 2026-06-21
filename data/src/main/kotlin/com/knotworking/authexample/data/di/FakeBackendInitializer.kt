package com.knotworking.authexample.data.di

import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.data.fake.FakeBackend
import com.knotworking.authexample.data.fake.FakeBackendStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class FakeBackendInitializer(
    private val store: FakeBackendStore,
    private val backend: FakeBackend,
    private val logger: Logger,
) {
    init {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            store.init()
            store.seedIfEmpty(DEMO_USERS)
            backend.start()
            logger.i(TAG, "FakeBackend ready at ${backend.baseUrl}")
        }
    }

    companion object {
        private const val TAG = "FakeBackendInitializer"
        private val DEMO_USERS = mapOf("alice" to "alice123", "bob" to "bob456")
    }
}

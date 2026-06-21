package com.knotworking.authexample

import android.app.Application
import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.core.di.coreModule
import com.knotworking.authexample.data.di.dataModule
import com.knotworking.authexample.data.fake.FakeBackend
import com.knotworking.authexample.data.fake.FakeBackendStore
import com.knotworking.authexample.domain.di.domainModule
import com.knotworking.authexample.presentation.di.presentationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AuthApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val koin = startKoin {
            androidContext(this@AuthApplication)
            modules(coreModule, domainModule, dataModule, presentationModule)
        }.koin

        val logger = koin.get<Logger>()
        val fakeBackendStore = koin.get<FakeBackendStore>()
        val fakeBackend = koin.get<FakeBackend>()

        logger.i(TAG, "AuthApplication starting")

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            fakeBackendStore.init()
            fakeBackendStore.seedIfEmpty(DEMO_USERS)
            fakeBackend.start()
            logger.i(TAG, "FakeBackend ready at ${fakeBackend.baseUrl}")
        }
    }

    companion object {
        private const val TAG = "AuthApplication"

        private val DEMO_USERS = mapOf(
            "alice" to "alice123",
            "bob" to "bob456",
        )
    }
}

package com.knotworking.authexample.data.di

import android.content.Context
import com.knotworking.authexample.data.fake.FakeBackend
import com.knotworking.authexample.data.fake.FakeBackendStore
import com.knotworking.authexample.data.network.api.AuthApi
import com.knotworking.authexample.data.network.api.ProtectedApi
import com.knotworking.authexample.data.network.authenticator.TokenAuthenticator
import com.knotworking.authexample.data.network.interceptor.AuthInterceptor
import com.knotworking.authexample.data.repository.AdminRepositoryImpl
import com.knotworking.authexample.data.repository.AuthRepositoryImpl
import com.knotworking.authexample.data.repository.ProtectedResourceRepositoryImpl
import com.knotworking.authexample.data.storage.AuthStateHolder
import com.knotworking.authexample.data.storage.CryptoManager
import com.knotworking.authexample.data.storage.SessionStoreImpl
import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.domain.repository.AdminRepository
import com.knotworking.authexample.domain.repository.AuthRepository
import com.knotworking.authexample.domain.repository.AuthStateObserver
import com.knotworking.authexample.domain.repository.ProtectedResourceRepository
import com.knotworking.authexample.domain.repository.SessionStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private val retrofitJson = Json { ignoreUnknownKeys = true }

val dataModule = module {

    // Fake backend
    single {
        FakeBackendStore(
            storeFile = get<Context>().filesDir.resolve("fake_backend/store.json"),
            logger = get(),
        )
    }
    single { FakeBackend(store = get(), logger = get()) }
    single(createdAtStart = true) { FakeBackendInitializer(store = get(), backend = get(), logger = get()) }

    // Encrypted session storage + auth state
    single { CryptoManager() }
    single<SessionStore> { SessionStoreImpl(context = get<Context>(), cryptoManager = get(), logger = get()) }
    single<AuthStateObserver> { AuthStateHolder(sessionStore = get(), logger = get()) }

    // Plain OkHttp client — used for auth + admin (no bearer header, so refresh loop is impossible)
    single(named("plain")) {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    // Auth + admin Retrofit / API
    single<AuthApi> {
        Retrofit.Builder()
            .baseUrl(get<FakeBackend>().baseUrl)
            .client(get(named("plain")))
            .addConverterFactory(retrofitJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
    }

    // Interceptor + authenticator wired to the authenticated client
    single { AuthInterceptor(sessionStore = get(), logger = get()) }
    single { TokenAuthenticator(authApi = get(), sessionStore = get(), authStateObserver = get(), logger = get()) }

    // Authenticated OkHttp client — used for protected resources
    single(named("authenticated")) {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    // Protected Retrofit / API
    single<ProtectedApi> {
        Retrofit.Builder()
            .baseUrl(get<FakeBackend>().baseUrl)
            .client(get(named("authenticated")))
            .addConverterFactory(retrofitJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ProtectedApi::class.java)
    }

    // Repository implementations
    single<AuthRepository> { AuthRepositoryImpl(authApi = get(), sessionStore = get(), logger = get()) }
    single<AdminRepository> { AdminRepositoryImpl(authApi = get(), logger = get()) }
    single<ProtectedResourceRepository> { ProtectedResourceRepositoryImpl(protectedApi = get(), logger = get()) }
}

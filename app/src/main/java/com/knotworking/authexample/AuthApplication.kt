package com.knotworking.authexample

import android.app.Application
import com.knotworking.authexample.core.di.coreModule
import com.knotworking.authexample.data.di.dataModule
import com.knotworking.authexample.domain.di.domainModule
import com.knotworking.authexample.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AuthApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@AuthApplication)
            modules(coreModule, domainModule, dataModule, presentationModule)
        }
    }
}

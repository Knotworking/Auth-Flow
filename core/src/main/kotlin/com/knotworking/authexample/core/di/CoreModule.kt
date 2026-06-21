package com.knotworking.authexample.core.di

import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.core.logging.AndroidLogger
import org.koin.dsl.module

val coreModule = module {
    single<Logger> { AndroidLogger() }
}

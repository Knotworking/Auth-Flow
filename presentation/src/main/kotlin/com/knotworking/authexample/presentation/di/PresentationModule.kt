package com.knotworking.authexample.presentation.di

import com.knotworking.authexample.presentation.AppViewModel
import com.knotworking.authexample.presentation.debug.DebugViewModel
import com.knotworking.authexample.presentation.home.HomeViewModel
import com.knotworking.authexample.presentation.login.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::DebugViewModel)
    viewModelOf(::HomeViewModel)
}

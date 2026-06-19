package com.knotworking.authexample.domain.di

import com.knotworking.authexample.domain.usecase.AddUserUseCase
import com.knotworking.authexample.domain.usecase.GetCurrentUserUseCase
import com.knotworking.authexample.domain.usecase.GetTokensUseCase
import com.knotworking.authexample.domain.usecase.GetUsersUseCase
import com.knotworking.authexample.domain.usecase.LoginUseCase
import com.knotworking.authexample.domain.usecase.LogoutUseCase
import com.knotworking.authexample.domain.usecase.ObserveAuthStateUseCase
import com.knotworking.authexample.domain.usecase.PerformProtectedOperationUseCase
import com.knotworking.authexample.domain.usecase.RemoveUserUseCase
import com.knotworking.authexample.domain.usecase.RevokeTokenUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::PerformProtectedOperationUseCase)
    factoryOf(::GetUsersUseCase)
    factoryOf(::AddUserUseCase)
    factoryOf(::RemoveUserUseCase)
    factoryOf(::GetTokensUseCase)
    factoryOf(::RevokeTokenUseCase)
}

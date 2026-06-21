package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow

class ObserveUsersUseCase(private val adminRepository: AdminRepository) {
    operator fun invoke(): Flow<AppResult<List<Credentials>>> = adminRepository.usersFlow
}

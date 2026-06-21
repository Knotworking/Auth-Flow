package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow

class GetUsersFlowUseCase(private val adminRepository: AdminRepository) {
    operator fun invoke(): Flow<Result<List<Credentials>>> = adminRepository.usersFlow
}

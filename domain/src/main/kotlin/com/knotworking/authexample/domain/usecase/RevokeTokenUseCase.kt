package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.repository.AdminRepository

class RevokeTokenUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(token: String): Result<Unit> =
        adminRepository.revokeToken(token)
}

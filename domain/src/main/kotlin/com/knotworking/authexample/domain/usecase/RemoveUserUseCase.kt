package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.repository.AdminRepository

class RemoveUserUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(username: String): Result<Unit> =
        adminRepository.removeUser(username)
}

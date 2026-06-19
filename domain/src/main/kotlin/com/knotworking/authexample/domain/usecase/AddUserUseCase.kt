package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.AdminRepository

class AddUserUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(credentials: Credentials): AppResult<Unit> =
        adminRepository.addUser(credentials)
}

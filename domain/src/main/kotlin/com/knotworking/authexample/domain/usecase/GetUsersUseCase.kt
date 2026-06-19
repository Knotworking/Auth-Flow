package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.AdminRepository

class GetUsersUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(): AppResult<List<Credentials>> = adminRepository.getUsers()
}

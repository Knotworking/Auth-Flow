package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.AdminRepository

class GetUsersUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(): Result<List<Credentials>> = adminRepository.getUsers()
}

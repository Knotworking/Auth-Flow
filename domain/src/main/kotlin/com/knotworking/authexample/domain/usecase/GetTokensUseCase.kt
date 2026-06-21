package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.model.TokenInfo
import com.knotworking.authexample.domain.repository.AdminRepository

class GetTokensUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(): Result<List<TokenInfo>> = adminRepository.getTokens()
}

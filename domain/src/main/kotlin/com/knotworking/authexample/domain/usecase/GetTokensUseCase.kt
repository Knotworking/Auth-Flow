package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.TokenInfo
import com.knotworking.authexample.domain.repository.AdminRepository

class GetTokensUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(): AppResult<List<TokenInfo>> = adminRepository.getTokens()
}

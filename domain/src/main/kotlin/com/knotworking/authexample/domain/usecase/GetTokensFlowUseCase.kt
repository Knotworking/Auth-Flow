package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.model.TokenInfo
import com.knotworking.authexample.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow

class GetTokensFlowUseCase(private val adminRepository: AdminRepository) {
    operator fun invoke(): Flow<Result<List<TokenInfo>>> = adminRepository.tokensFlow
}

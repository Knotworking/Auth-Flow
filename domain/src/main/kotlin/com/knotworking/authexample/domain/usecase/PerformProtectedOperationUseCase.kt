package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.repository.ProtectedResourceRepository

class PerformProtectedOperationUseCase(
    private val protectedResourceRepository: ProtectedResourceRepository,
) {
    suspend operator fun invoke(): Result<String> =
        protectedResourceRepository.performOperation()
}

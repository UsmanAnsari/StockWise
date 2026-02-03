package com.uansari.stockwise.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Base interface for Use Cases that return a Flow (reactive).
 */
interface FlowUseCase<in P, R> {
    operator fun invoke(params: P): Flow<R>
}

/**
 * Base interface for Use Cases with no parameters.
 */
interface NoParamFlowUseCase<R> {
    operator fun invoke(): Flow<R>
}

/**
 * Base interface for suspending Use Cases (one-shot).
 */
interface SuspendUseCase<in P, R> {
    suspend operator fun invoke(params: P): R
}

/**
 * Base interface for suspending Use Cases with no parameters.
 */
interface NoParamSuspendUseCase<R> {
    suspend operator fun invoke(): R
}
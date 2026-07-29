package com.forgeflow.core.common.result

sealed interface AppError {
    data object LocalDataUnavailable : AppError
    data object WriteFailed : AppError
}

sealed interface DataResult<out T> {
    data class Success<T>(val value: T) : DataResult<T>
    data class Failure(val error: AppError) : DataResult<Nothing>
}

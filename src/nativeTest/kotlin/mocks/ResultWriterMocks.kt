package mocks

import IResultWriter
import arrow.core.Either
import types.ApiError

class DummyResultWriter : IResultWriter {
    var lastResult: String? = null
    var lastError: ApiError? = null

    override fun write(result: Either<ApiError, String>) {
        when (result) {
            is Either.Right -> lastResult = result.value
            is Either.Left -> lastError = result.value
        }
    }
}
import arrow.core.Either
import types.ApiError

interface IResultWriter {
    fun write(result: Either<ApiError, String>)
}

class ConsoleResultWriter : IResultWriter {
    override fun write(result: Either<ApiError, String>) {
        when (result) {
            is Either.Right -> println(result.value)
            is Either.Left -> println("error: ${result.value.message} (code=${result.value.code})")
        }

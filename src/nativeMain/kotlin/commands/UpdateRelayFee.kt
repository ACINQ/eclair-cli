package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import types.ApiError

class UpdateRelayFeeCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "updaterelayfee",
    "Updates the fee policy for the specified nodeId."
) {
    private val nodeId by option(
        ArgType.String,
        description = "The nodeId of the peer you want to update"
    )
    private val nodeIds by option(
        ArgType.String,
        description = "The nodeIds of the peers you want to update"
    )
    private val feeBaseMsat by option(
        ArgType.Int,
        description = "The new base fee to use"
    )
    private val feeProportionalMillionths by option(
        ArgType.Int,
        description = "The new proportional fee to use"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.updaterelayfee(
            nodeId,
            nodeIds?.split(","),
            feeBaseMsat!!,
            feeProportionalMillionths!!
        ).flatMap { apiResponse ->
            try {
                Either.Right(format.decodeFromString<Map<String, String>>(apiResponse))
            } catch (e: Throwable) {
                Either.Left(ApiError(1, "api response could not be parsed: $apiResponse"))
            }
        }
            .map { decoded ->
                format.encodeToString(decoded)
            }
        resultWriter.write(result)
    }
}
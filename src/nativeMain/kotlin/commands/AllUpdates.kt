package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.Either
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import types.AllUpdates
import types.ApiError
import types.Node

class AllUpdatesCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "allupdates",
    "Returns detailed information about all public channels in the network; the information is mostly taken from the channel_update network messages."
) {
    private val nodeId by option(
        ArgType.String,
        description = "The nodeId of the node to be used as filter for the updates"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.allupdates(
            nodeId = nodeId
        )
            .flatMap { apiResponse ->
                try {
                    Either.Right(format.decodeFromString<List<AllUpdates>>(apiResponse))
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
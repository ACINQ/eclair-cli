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

class ForceCloseCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "forceclose",
    "Initiates a unilateral close for given channels that belong to this eclair node."
) {
    private val channelId by option(
        ArgType.String,
        description = "The channelId of the channel you want to close"
    )
    private val shortChannelId by option(
        ArgType.String,
        description = "The shortChannelId of the channel you want to close"
    )
    private val channelIds by option(
        ArgType.String,
        description = "List of channelIds to close"
    )
    private val shortChannelIds by option(
        ArgType.String,
        description = "List of shortChannelIds to close"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.forceclose(
            channelId = channelId!!,
            shortChannelId = shortChannelId,
            channelIds = channelIds?.split(","),
            shortChannelIds = shortChannelIds?.split(",")
        )
            .flatMap { apiResponse ->
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
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

class CloseCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "close",
    "Initiates a cooperative close for given channels that belong to this eclair node."
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
    private val scriptPubKey by option(
        ArgType.String,
        description = "A serialized scriptPubKey that you want to use to close the channel"
    )
    private val preferredFeerateSatByte by option(
        ArgType.Int,
        description = "Preferred feerate (sat/byte) for the closing transaction"
    )
    private val minFeerateSatByte by option(
        ArgType.Int,
        description = "Minimum feerate (sat/byte) for the closing transaction"
    )
    private val maxFeerateSatByte by option(
        ArgType.Int,
        description = "Maximum feerate (sat/byte) for the closing transaction"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val result = eclairClient.close(
            channelId = channelId!!,
            shortChannelId = shortChannelId,
            channelIds = channelIds?.split(","),
            shortChannelIds = shortChannelIds?.split(","),
            scriptPubKey = scriptPubKey,
            preferredFeerateSatByte = preferredFeerateSatByte,
            minFeerateSatByte = minFeerateSatByte,
            maxFeerateSatByte = maxFeerateSatByte
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
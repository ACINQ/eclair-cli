package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.CloseResponse
import types.Serialization

class CloseCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "close",
    "Initiates a cooperative close for given channels that belong to this eclair node."
) {
    var channelId by option(
        ArgType.String,
        description = "The channelId of the channel you want to close"
    )
    var shortChannelId by option(
        ArgType.String,
        description = "The shortChannelId of the channel you want to close"
    )
    var channelIds by option(
        ArgType.String,
        description = "List of channelIds to close"
    )
    var shortChannelIds by option(
        ArgType.String,
        description = "List of shortChannelIds to close"
    )
    var scriptPubKey by option(
        ArgType.String,
        description = "A serialized scriptPubKey that you want to use to close the channel"
    )
    var preferredFeerateSatByte by option(
        ArgType.Int,
        description = "Preferred feerate (sat/byte) for the closing transaction"
    )
    var minFeerateSatByte by option(
        ArgType.Int,
        description = "Minimum feerate (sat/byte) for the closing transaction"
    )
    var maxFeerateSatByte by option(
        ArgType.Int,
        description = "Maximum feerate (sat/byte) for the closing transaction"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
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
            .flatMap { apiResponse -> Serialization.decode<CloseResponse>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}
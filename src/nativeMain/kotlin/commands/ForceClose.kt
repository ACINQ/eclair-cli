package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.CloseResponse
import types.Serialization

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
        val result = eclairClient.forceclose(
            channelId = channelId!!,
            shortChannelId = shortChannelId,
            channelIds = channelIds?.split(","),
            shortChannelIds = shortChannelIds?.split(",")
        )
            .flatMap { apiResponse -> Serialization.decode<CloseResponse>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}
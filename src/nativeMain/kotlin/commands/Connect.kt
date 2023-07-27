package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking

class ConnectCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "connect",
    "Connect to another lightning node. This will perform a connection but no channels will be opened. "
) {
    var uri by option(
        ArgType.String,
        description = "If the uri to the target node is not provided, eclair will use one of the addresses published by the remote peer in its node_announcement messages if it can be found."
    )

    var nodeId by option(
        ArgType.String,
        description = "Connect to another lightning node. This does not require a target address. Instead, eclair will use one of the addresses published by the remote peer in its node_announcement messages."
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = when {
            uri != null -> eclairClient.connectUri(uri!!)
            nodeId != null -> eclairClient.connectNodeId(nodeId!!)
            else -> throw IllegalArgumentException("Either URI or nodeId must be provided.")
        }
        resultWriter.write(result)
    }
}
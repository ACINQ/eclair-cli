package commands

import IResultWriter
import api.ConnectionTarget
import api.IEclairClientBuilder
import arrow.core.Either
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.ApiError

class ConnectCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "connect",
    "Connect to another lightning node. This will perform a connection but no channels will be opened. "
) {
    private val uri by option(
        ArgType.String,
        description = "If the uri to the target node is not provided, eclair will use one of the addresses published by the remote peer in its node_announcement messages if it can be found."
    )
    private val nodeId by option(
        ArgType.String,
        description = "Connect to another lightning node. This does not require a target address. Instead, eclair will use one of the addresses published by the remote peer in its node_announcement messages."
    )
    private val address by option(
        ArgType.String,
        description = "The IPv4 host address of the node."
    )
    private val port by option(
        ArgType.Int,
        description = "The port of the node (default: 9735)"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = when {
            uri != null -> eclairClient.connect(ConnectionTarget.Uri(uri!!))
            nodeId != null && address == null -> eclairClient.connect(ConnectionTarget.NodeId(nodeId!!))
            nodeId != null && address != null -> eclairClient.connect(
                ConnectionTarget.Manual(
                    nodeId!!,
                    address!!,
                    port
                )
            )
            else -> Either.Left(ApiError(400, "invalid request parameters"))
        }
        resultWriter.write(result)
    }
}
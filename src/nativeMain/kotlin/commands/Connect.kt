package commands

import IResultWriter
import api.IEclairClient
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
    var address by option(
        ArgType.String,
        description = "The IPv4 host address of the node."
    )
    var port by option(
        ArgType.Int,
        description = "The port of the node(default: 9735)"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = when {
            uri != null -> eclairClient.connect(IEclairClient.ConnectionTarget.Uri(uri!!))
            nodeId != null && address == null -> eclairClient.connect(IEclairClient.ConnectionTarget.NodeId(nodeId!!))
            nodeId != null && address != null -> eclairClient.connect(
                IEclairClient.ConnectionTarget.Manual(
                    nodeId!!,
                    address!!,
                    port
                )
            )

            else -> throw IllegalArgumentException("Either URI or nodeId must be provided.")
        }
        resultWriter.write(result)
    }
}
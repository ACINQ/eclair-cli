package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.NodeCommandResponse
import types.Serialization

class NodeCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "node",
    "Returns information about a specific node on the lightning network, including its node_announcement and some channel statistics."
) {
    private val nodeId by option(
        ArgType.String,
        description = "The nodeId of the requested node"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.node(
            nodeId = nodeId!!,
        )
            .flatMap { apiResponse -> Serialization.decode<NodeCommandResponse>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}
package commands

import ConsoleResultWriter
import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import types.NodeInfo
import types.Serialization

@OptIn(ExperimentalCli::class)
class ConnectCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : Subcommand(
    "connect",
    "Connect to another lightning node. This will perform a connection but no channels will be opened. "
) {
    private val password by option(ArgType.String, shortName = "p", description = "Password for the Eclair API (can be found in eclair.conf)").required()
    private val host by option(ArgType.String, description = "Host URL for the Eclair API (can be found in eclair.conf)").default("http://localhost:8080")
    private val nodeId by argument(ArgType.String, description = " This API does not require a target address. Instead, eclair will use one of the addresses published by the remote peer in its node_announcement messages.")
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.connect(nodeId)
            .flatMap { apiResponse -> Serialization.decode<NodeInfo>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}
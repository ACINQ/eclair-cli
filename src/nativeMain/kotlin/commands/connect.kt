package commands

import IEclairClient
import IResultWriter
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCli::class)
class ConnectCommand(
    private val resultWriter: IResultWriter,
    private val eclairClient: IEclairClient
) : Subcommand("connect", "Connect to another lightning node") {
    private val password by option(
        ArgType.String, shortName = "p", description = "Password for the Eclair API"
    ).required()
    private val host by option(
        ArgType.String, description = "Host URL for the Eclair API"
    ).default("http://localhost:8080")
    private val uri by argument(ArgType.String, description = "The URI in format 'nodeId@host:port'").optional()
    private val nodeId by argument(ArgType.String, description = "The nodeId of the node you want to connect to").optional()
    private val manualHost by argument(ArgType.String, description = "The IPv4 host address of the node").optional()

    override fun execute() {
        runBlocking {
            val result = when {
                uri != null -> eclairClient.connectUri(password, host, uri!!)
                nodeId != null && manualHost != null -> eclairClient.connectManual(password, host, nodeId!!, manualHost!!)
                nodeId != null -> eclairClient.connectNodeId(password, host, nodeId!!)
                else -> throw IllegalArgumentException("You must specify either a uri, or a nodeId and optional host")
            }
            result.fold(
                onSuccess = {
                    resultWriter.writeSuccess("Connected to node")
                },
                onFailure = {
                    resultWriter.writeError("Failed to connect: ${it.message}")
                }
            )
        }
    }
}
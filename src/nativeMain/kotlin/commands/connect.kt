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
    private var password by option(
        ArgType.String, shortName = "p", description = "Password for the Eclair API"
    ).required()
    private var host by option(
        ArgType.String, description = "Host URL for the Eclair API"
    ).default("http://localhost:8080")
    private var uri by option(
        ArgType.String, shortName = "u", description = "The URI in format 'nodeId@host:port'"
    ).default("")
    private var nodeId by option(
        ArgType.String, shortName = "n", description = "The nodeId of the node you want to connect to"
    ).default("")
    private var manualHost by option(
        ArgType.String, shortName = "m", description = "The IPv4 host address of the node (for manual connect)"
    ).default("")

    override fun execute() {
        runBlocking {
            val result = when {
                uri.isNotEmpty() -> eclairClient.connectUri(password, host, uri)
                nodeId.isNotEmpty() && manualHost.isNotEmpty() -> eclairClient.connectManual(password, host, nodeId, manualHost)
                nodeId.isNotEmpty() -> eclairClient.connectNodeId(password, host, nodeId)
                else -> error("Please provide either uri or nodeId and host or nodeId only")
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
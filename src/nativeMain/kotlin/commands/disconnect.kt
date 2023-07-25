package commands

import ConsoleResultWriter
import EclairClient
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCli::class)
class DisconnectCommand(
    private val resultWriter: ConsoleResultWriter,
    private val eclairClient: EclairClient
) : Subcommand("disconnect", "Disconnect from another lightning node") {
    private val password by option(
        ArgType.String, shortName = "p", description = "Password for the Eclair API"
    ).required()
    private val host by option(
        ArgType.String, description = "Host URL for the Eclair API"
    ).default("http://localhost:8080")
    private val nodeId by argument(ArgType.String, description = "The nodeId of the node you want to connect to")

    override fun execute() {
        runBlocking {
            val result = eclairClient.disconnect(password, host, nodeId)
            result.fold(
                onSuccess = {
                    resultWriter.writeSuccess("Disconnected from node")
                },
                onFailure = {
                    resultWriter.writeError("Failed to disconnect: ${it.message}")
                }
            )
        }
    }
}
package commands

import IResultWriter
import api.IEclairClientBuilder
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.DisconnectionResult
import types.Serialization

class DisconnectCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "disconnect",
    "Disconnect from a peer."
) {
    private val nodeId by option(ArgType.String, description = "Disconnect from a connected peer.")
    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.disconnect(nodeId!!).map { response ->
            val result = if (response.contains("disconnecting")) {
                DisconnectionResult(true, response)
            } else {
                DisconnectionResult(false, response)
            }
            Serialization.encode(result)
        }
        resultWriter.write(result)
    }
}
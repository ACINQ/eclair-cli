package commands

import IEclairClient
import IResultWriter
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCli::class)
class GetInfoCommand(
    private val resultWriter: IResultWriter, private val eclairClient: IEclairClient
) : Subcommand(
    "getinfo",
    "Get information about this instance such as version, features, nodeId and current block height as seen by the eclair."
) {
    private var password by option(
        ArgType.String, shortName = "p", description = "Password for the Eclair API"
    ).required()
    private var host by option(
        ArgType.String, shortName = "l", description = "Host URL for the Eclair API"
    ).default("http://localhost:8080")

    override fun execute() = runBlocking {
        val infoResult = eclairClient.getInfo(password, host)
        infoResult.fold(onSuccess = {
            resultWriter.writeSuccess(infoResult.getOrThrow())
        }, onFailure = {
            resultWriter.writeError("Error fetching information: ${infoResult.exceptionOrNull()?.message}")
        })
    }
}
package commands

import kotlinx.cli.*

@OptIn(ExperimentalCli::class)
abstract class BaseCommand(commandName: String, commandDescription: String): Subcommand(commandName, commandDescription) {
    protected val password by option(ArgType.String, shortName = "p", description = "Password for the Eclair API (can be found in eclair.conf)").required()
    protected val host by option(ArgType.String, description = "Host URL for the Eclair API (can be found in eclair.conf)").default("http://localhost:8080")
}
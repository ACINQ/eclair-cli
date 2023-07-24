import api.EclairClientBuilder
import commands.GetInfoCommand
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("eclair-cli")
    val resultWriter = ConsoleResultWriter()
    val apiClientBuilder = EclairClientBuilder()
    parser.subcommands(
        GetInfoCommand(resultWriter, apiClientBuilder)
    )
    parser.parse(args)
}
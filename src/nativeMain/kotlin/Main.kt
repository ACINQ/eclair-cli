import api.EclairClientBuilder
import commands.ConnectCommand
import commands.DisconnectCommand
import commands.GetInfoCommand
import commands.OpenCommand
import commands.RbfOpenCommand
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("eclair-cli")
    val resultWriter = ConsoleResultWriter()
    val apiClientBuilder = EclairClientBuilder()
    parser.subcommands(
        GetInfoCommand(resultWriter, apiClientBuilder),
        ConnectCommand(resultWriter, apiClientBuilder),
        DisconnectCommand(resultWriter, apiClientBuilder),
        OpenCommand(resultWriter, apiClientBuilder),
        RbfOpenCommand(resultWriter, apiClientBuilder)
    )
    parser.parse(args)
}
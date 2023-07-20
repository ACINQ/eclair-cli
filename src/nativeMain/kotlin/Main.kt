import commands.GetInfoCommand
import commands.HelloWorld
import commands.ConnectCommand
import commands.DisconnectCommand
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("eclair-cli")
    val resultWriter = ConsoleResultWriter()
    val apiClient = EclairClient()
    parser.subcommands(
        HelloWorld(),
        GetInfoCommand(resultWriter, apiClient),
        ConnectCommand(resultWriter, apiClient),
        DisconnectCommand(resultWriter, apiClient)
    )
    parser.parse(args)
}
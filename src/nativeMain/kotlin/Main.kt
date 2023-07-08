import commands.GetInfoCommand
import commands.HelloWorld
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val resultWriter = ConsoleResultWriter()
    val apiClient = EclairClient()
    val parser = ArgParser("eclair-cli")
    parser.subcommands(
        HelloWorld(),
        GetInfoCommand(resultWriter, apiClient)
    )
    parser.parse(args)
}
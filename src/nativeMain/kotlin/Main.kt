import commands.GetInfoCommand
import commands.HelloWorld
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    println("Enter your password: ")
    val password = readLine()
    if (password.isNullOrEmpty()) {
        println("Password can't be empty.")
        return
    }

    val resultWriter = ConsoleResultWriter()
    val apiClient = EclairClient(password = password)
    val parser = ArgParser("eclair-cli")
    parser.subcommands(
        HelloWorld(),
        GetInfoCommand(resultWriter, apiClient)
    )
    parser.parse(args)
}
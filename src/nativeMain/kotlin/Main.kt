import commands.HelloWorld
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("eclair-cli")
    parser.subcommands(
        HelloWorld(),
    )
    parser.parse(args)
}
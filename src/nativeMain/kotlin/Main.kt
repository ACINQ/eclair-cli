import kotlinx.cli.ArgParser

fun main(args: Array<String>) {
    val parser = ArgParser("eclair-cli")
    parser.parse(args)
}
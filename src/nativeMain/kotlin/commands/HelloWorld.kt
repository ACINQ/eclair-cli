package commands

import arrow.core.Either
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required

@OptIn(ExperimentalCli::class)
class HelloWorld() : Subcommand("say-hello", "Greet user") {

    private val userName by option(ArgType.String, shortName = "n", description = "User name").required()

    override fun execute() {
        when (val message = greetUser(userName)) {
            is Either.Left -> println(message.value)
            is Either.Right -> println(message.value)
        }
    }

    private fun greetUser(user: String): Either<String, String> {
        return if (user == "eve") {
            Either.Left("Eve doesn't deserve to be greeted")
        } else {
            Either.Right("Hello $user!")
        }
    }
}

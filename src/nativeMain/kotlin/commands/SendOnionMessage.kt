package commands

import IResultWriter
import api.IEclairClientBuilder
import arrow.core.flatMap
import kotlinx.cli.ArgType
import kotlinx.coroutines.runBlocking
import types.SendOnionMessageResult
import types.Serialization

class SendOnionMessageCommand(
    private val resultWriter: IResultWriter,
    private val eclairClientBuilder: IEclairClientBuilder
) : BaseCommand(
    "sendonionmessage",
    "Send an onion message to a remote recipient."
) {
    private val content by option(
        ArgType.String,
        description = "Message sent to the recipient(encoded as a tlv stream)"
    )
    private val recipientNode by option(
        ArgType.String,
        description = "NodeId of the recipient, if known"
    )
    private val recipientBlindedRoute by option(
        ArgType.String,
        description = "Blinded route provided by the recipient(encoded as a tlv)"
    )
    private val intermediateNodes by option(
        ArgType.String,
        description = "Intermediate nodes to insert before the recipient"
    )
    private val replyPath by option(
        ArgType.String,
        description = "Reply path that must be used if a response is expected"
    )

    override fun execute() = runBlocking {
        val eclairClient = eclairClientBuilder.build(host, password)
        val result = eclairClient.sendonionmessage(
            content = content!!,
            recipientNode = recipientNode,
            recipientBlindedRoute = recipientBlindedRoute,
            intermediateNodes = intermediateNodes?.split(","),
            replyPath = replyPath?.split(",")
        ).flatMap { apiResponse -> Serialization.decode<SendOnionMessageResult>(apiResponse) }
            .map { decoded -> Serialization.encode(decoded) }
        resultWriter.write(result)
    }
}
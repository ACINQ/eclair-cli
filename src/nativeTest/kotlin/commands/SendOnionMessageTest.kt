package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.SendOnionMessageResult
import kotlin.test.*

@OptIn(ExperimentalCli::class)
class SendOnionMessageCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = SendOnionMessageCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "sendonionmessage",
                "-p",
                "password",
                "--content",
                "2b03ffffff",
                "--recipientNode",
                "02e33c55738832506284c40d60cecc4e7f7a7f32de97fc0def1ba2ac8f29d27917"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter =
            runTest(DummyEclairClient(sendonionmessageSuccessWithReplyPath = DummyEclairClient.validSendOnionMessageWithReplyPath))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(
                SendOnionMessageResult.serializer(),
                DummyEclairClient.validSendOnionMessageWithReplyPath
            ),
            format.decodeFromString(SendOnionMessageResult.serializer(), resultWriter.lastResult!!),
        )
    }

    @Test
    fun `api error`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(FailingEclairClient(error))
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }

    @Test
    fun `serialization error`() {
        val resultWriter = runTest(DummyEclairClient(sendonionmessageSuccessWithReplyPath = "{invalidJson}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}
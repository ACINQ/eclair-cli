package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.ReceivedInfoResponse
import kotlin.test.*

class GetReceivedInfoCommandTest {
    @OptIn(ExperimentalCli::class)
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = GetReceivedInfoCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "getreceivedinfo",
                "-p",
                "password",
                "--paymentHash",
                "a482c4d3a232d028881aee9ccd27c5f7b554824138c927baf40b8d84d38d72ce"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter =
            runTest(DummyEclairClient(getreceivedinfoResponse = DummyEclairClient.validGetReceivedInfoResponse))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(ReceivedInfoResponse.serializer(), DummyEclairClient.validGetReceivedInfoResponse),
            format.decodeFromString(ReceivedInfoResponse.serializer(), resultWriter.lastResult!!),
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
        val resultWriter = runTest(DummyEclairClient(getreceivedinfoResponse = "{invalidJson}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}
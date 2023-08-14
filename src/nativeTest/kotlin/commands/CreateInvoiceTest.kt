package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.CreateInvoiceResponse
import kotlin.test.*

class CreateInvoiceCommandTest {
    @OptIn(ExperimentalCli::class)
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = CreateInvoiceCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "createinvoice",
                "-p",
                "password",
                "--description",
                "BroWantsCoffee",
                "--amountMsat",
                "1000"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter =
            runTest(DummyEclairClient(createInvoiceResponse = DummyEclairClient.validCreateInvoiceResponse))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(CreateInvoiceResponse.serializer(), DummyEclairClient.validCreateInvoiceResponse),
            format.decodeFromString(CreateInvoiceResponse.serializer(), resultWriter.lastResult!!),
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
        val resultWriter = runTest(DummyEclairClient(createInvoiceResponse = "{invalidJson}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}
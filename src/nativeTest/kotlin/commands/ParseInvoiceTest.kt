package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.Invoice
import kotlin.test.*

class ParseInvoiceCommandTest {
    @OptIn(ExperimentalCli::class)
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = ParseInvoiceCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "parseinvoice",
                "-p",
                "password",
                "--invoice",
                "lnbcrt10n1pjd4rpdpp5mj9vgxxw7cr39cyk8czcycswavhyx5xhg2tgtulp82wtkx9vg90qdqhgfex74mpde68xsm0venx2egsp5ph8nceurv5sp5528f2cqucpk2z8vf024n3r7446tfqw67flstj5smqz9gxqrrsscqp79q7sqqqqqqqqqqqqqqqqqqqsqqqqqysgqq4g0l9xms94tskmm7hkzarpdrdfgwjmgs2rzg3xc904vtqju08a9536hhn5224733a55htd4r3wa8e3fcj483zx2vq342j4wru9pwjsqrkyqpw"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter =
            runTest(DummyEclairClient(parseInvoiceResponse = DummyEclairClient.validParseInvoiceResponse))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(Invoice.serializer(), DummyEclairClient.validParseInvoiceResponse),
            format.decodeFromString(Invoice.serializer(), resultWriter.lastResult!!),
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
        val resultWriter = runTest(DummyEclairClient(parseInvoiceResponse = "{invalidJson}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}
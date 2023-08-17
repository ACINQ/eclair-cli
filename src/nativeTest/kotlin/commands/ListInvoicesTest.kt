package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import kotlin.test.*

class ListInvoicesCommandTest {
    @OptIn(ExperimentalCli::class)
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = ListInvoicesCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(arrayOf("listinvoices", "-p", "password"))
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter =
            runTest(DummyEclairClient(listinvoicesResponse = DummyEclairClient.validListInvoicesResponse))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        assertEquals(
            format.parseToJsonElement(DummyEclairClient.validListInvoicesResponse),
            format.decodeFromString(resultWriter.lastResult!!),
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
        val resultWriter = runTest(DummyEclairClient(listinvoicesResponse = "{invalidJson}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}

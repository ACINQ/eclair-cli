package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.ApiError
import types.InvoiceResult
import types.Serialization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCli::class)
class PayInvoiceCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = PayInvoiceCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "payinvoice",
                "-p",
                "password",
                "--invoice",
                "lnbcrt9990p1pjd4wz0pp5j4vn0usn95n2rapus3ltxytr6sum5lrah2hq6yhphsxnaujz9m3qdq5f9nx2etv2dkx2ets09ussp58wudf374k2ntpyel7kmhnfyklnfrfep977ax75z9g50rnsfa2hpsmqz9gxqrrsscqp79q7sqqqqqqqqqqqqqqqqqqqsqqqqqysgq79wmpuhf20ha3z6f55sg0dje7k62w8vyn84zne5k9v8lm2vf6k2q792v7kgf4ch7rs2an3q33yks2gyfavdl7pejhqklr48mwar8l2cpnzek3g"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter = runTest(DummyEclairClient())
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val expectedOutput =
            Serialization.encode(InvoiceResult(true, DummyEclairClient.validPayInvoiceResponse))
        assertEquals(expectedOutput, resultWriter.lastResult)
    }

    @Test
    fun `api error`() {
        val error = ApiError(42, "test failure message")
        val resultWriter = runTest(FailingEclairClient(error))
        assertNull(resultWriter.lastResult)
        assertEquals(error, resultWriter.lastError)
    }
}
package commands

import api.IEclairClientBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.serialization.json.Json
import mocks.DummyEclairClient
import mocks.DummyResultWriter
import mocks.FailingEclairClient
import types.*
import kotlin.test.*

@OptIn(ExperimentalCli::class)
class SendToRouteCommandTest {
    private fun runTest(eclairClient: IEclairClientBuilder): DummyResultWriter {
        val resultWriter = DummyResultWriter()
        val command = SendToRouteCommand(resultWriter, eclairClient)
        val parser = ArgParser("test")
        parser.subcommands(command)
        parser.parse(
            arrayOf(
                "sendtoroute",
                "-p",
                "password",
                "--invoice",
                "lnbcrt1pjdkk2fpp5keyzpk6c8les7hydfs38a3xaqy43mpum56lwrszugac905wxs6rqdq523jhxapq2pshjmt9de6qsp5nlxmqucnz797rvrx4sx7gk2z0y00wyy4zh2rs0tnsnfekzcchl0qmqz9gxqrrsscqp79q7sqqqqqqqqqqqqqqqqqqqsqqqqqysgqckp6gsqa0s9vas592myu33a8m7tjrw2cjml9p2s5apwdchv5lcv5elq70j0lxxwmcpggnryara0yd3vd4e3zkrak3a3dj3z37zvmx8qpecw09x",
                "--nodeIds",
                "02fe677ac8cd61399d097535a3e8a51a0849e57cdbab9b34796c86f3e33568cbe2,028e2403fbfddb3d787843361f91adbda64c6f622921b19fb48f5766508bcadb29",
                "--amountMsat",
                "1000000",
                "--paymentHash",
                "b64820db583ff30f5c8d4c227ec4dd012b1d879ba6bee1c05c477057d1c68686",
                "--finalCltvExpiry",
                "144"
            )
        )
        return resultWriter
    }

    @Test
    fun `successful request`() {
        val resultWriter = runTest(DummyEclairClient(sendToRouteResponse = DummyEclairClient.validSendToRouteResponse))
        assertNull(resultWriter.lastError)
        assertNotNull(resultWriter.lastResult)
        val format = Json { ignoreUnknownKeys = true }
        assertEquals(
            format.decodeFromString(RouteResult.serializer(), DummyEclairClient.validSendToRouteResponse),
            format.decodeFromString(RouteResult.serializer(), resultWriter.lastResult!!),
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
        val resultWriter = runTest(DummyEclairClient(sendToRouteResponse = "{invalid json}"))
        assertNull(resultWriter.lastResult)
        assertNotNull(resultWriter.lastError)
        assertTrue(resultWriter.lastError!!.message.contains("api response could not be parsed"))
    }
}
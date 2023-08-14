import api.EclairClientBuilder
import commands.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("eclair-cli")
    val resultWriter = ConsoleResultWriter()
    val apiClientBuilder = EclairClientBuilder()
    parser.subcommands(
        GetInfoCommand(resultWriter, apiClientBuilder),
        ConnectCommand(resultWriter, apiClientBuilder),
        DisconnectCommand(resultWriter, apiClientBuilder),
        OpenCommand(resultWriter, apiClientBuilder),
        RbfOpenCommand(resultWriter, apiClientBuilder),
        CpfpBumpFeesCommand(resultWriter, apiClientBuilder),
        CloseCommand(resultWriter, apiClientBuilder),
        ForceCloseCommand(resultWriter, apiClientBuilder),
        UpdateRelayFeeCommand(resultWriter, apiClientBuilder),
        PeersCommand(resultWriter, apiClientBuilder),
        UpdateRelayFeeCommand(resultWriter, apiClientBuilder),
        NodesCommand(resultWriter, apiClientBuilder),
        NodeCommand(resultWriter, apiClientBuilder),
        AllChannelsCommand(resultWriter, apiClientBuilder),
        AllUpdatesCommand(resultWriter, apiClientBuilder),
        CreateInvoiceCommand(resultWriter, apiClientBuilder),
        DeleteInvoiceCommand(resultWriter, apiClientBuilder),
        ParseInvoiceCommand(resultWriter, apiClientBuilder),
    )
    parser.parse(args)
}
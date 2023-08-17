package types

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

data class ApiError(val code: Int, val message: String)

@Serializable
sealed class EclairApiType

@Serializable
data class Features(val activated: Map<String, String>, val unknown: List<Int>)

@Serializable
data class NodeInfo(
    val version: String,
    val nodeId: String,
    val alias: String,
    val color: String,
    val features: Features,
    val chainHash: String,
    val network: String,
    val blockHeight: Int,
    val publicAddresses: List<String>,
    val instanceId: String,
) : EclairApiType()

@Serializable
data class ConnectionResult(val success: Boolean) : EclairApiType()

@Serializable
data class DisconnectionResult(val success: Boolean, val message: String) : EclairApiType()

@Serializable
data class Peer(
    val nodeId: String,
    val state: String,
    val address: String? = null,
    val channels: Int
)

@Serializable
data class Node(
    val signature: String,
    val features: Features,
    val timestamp: Timestamp,
    val nodeId: String,
    val rgbColor: String,
    val alias: String,
    val addresses: List<String>,
    val tlvStream: Map<String, @Contextual Any>
)

@Serializable
data class Timestamp(
    val iso: String,
    val unix: Long
)

@Serializable
data class NodeCommandResponse(
    val announcement: Node
) : EclairApiType()

@Serializable
data class AllChannels(
    val shortChannelId: String,
    val a: String,
    val b: String
)

@Serializable
data class AllUpdates(
    val signature: String,
    val chainHash: String,
    val shortChannelId: String,
    val timestamp: Timestamp,
    val messageFlags: Map<String, Boolean>,
    val channelFlags: ChannelFlags,
    val cltvExpiryDelta: Int,
    val htlcMinimumMsat: Int,
    val feeBaseMsat: Int,
    val feeProportionalMillionths: Int,
    val htlcMaximumMsat: Long,
    val tlvStream: Map<String, @Contextual Any>
)

@Serializable
data class ChannelFlags(
    val isEnabled: Boolean,
    val isNode1: Boolean
)

@Serializable
data class Invoice(
    val prefix: String,
    val timestamp: Long,
    val nodeId: String,
    val serialized: String,
    val description: String,
    val paymentHash: String,
    val paymentMetadata: String,
    val expiry: Int,
    val minFinalCltvExpiry: Int,
    val amount: Long? = null,
    val features: Features,
    val routingInfo: List<@Contextual Any>
) : EclairApiType()

@Serializable
data class DeleteInvoiceResult(val success: Boolean, val message: String) : EclairApiType()

@Serializable
data class InvoiceResult(val success: Boolean, val message: String) : EclairApiType()

@Serializable
data class RouteResult(
    val paymentId: String,
    val parentId: String
) : EclairApiType()

@Serializable
data class SentInfoResponse(
    val id: String,
    val parentId: String,
    val paymentHash: String,
    val paymentType: String,
    val amount: Long,
    val recipientAmount: Long,
    val recipientNodeId: String,
    val createdAt: Timestamp,
    val invoice: Invoice,
    val status: PaymentStatus
)

@Serializable
data class PaymentStatus(
    val type: String,
    val paymentPreimage: String,
    val feesPaid: Long,
    val route: List<Route>,
    val completedAt: Timestamp
)

@Serializable
data class Route(
    val nodeId: String,
    val nextNodeId: String,
    val shortChannelId: String
)

@Serializable
data class ReceivedInfoResponse(
    val invoice: Invoice,
    val paymentPreimage: String,
    val paymentType: String,
    val createdAt: Timestamp,
    val status: ReceivePaymentStatus
) : EclairApiType()

@Serializable
data class ReceivePaymentStatus(
    val type: String,
    val amount: Long? = null,
    val receivedAt: Timestamp? = null
)

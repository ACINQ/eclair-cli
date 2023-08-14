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
data class CreateInvoiceResponse(
    val prefix: String,
    val timestamp: Long,
    val nodeId: String,
    val serialized: String,
    val description: String,
    val paymentHash: String,
    val paymentMetadata: String,
    val expiry: Int,
    val minFinalCltvExpiry: Int,
    val amount: Long,
    val features: Features,
    val routingInfo: List<@Contextual Any>
) : EclairApiType()
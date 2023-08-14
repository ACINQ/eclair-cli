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
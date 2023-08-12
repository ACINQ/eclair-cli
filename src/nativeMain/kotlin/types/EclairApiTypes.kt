package types

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
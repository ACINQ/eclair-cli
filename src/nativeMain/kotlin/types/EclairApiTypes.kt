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

package org.example.project.domain.returns

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReturnStatus(val value: String) {
    @SerialName("requested")
    REQUESTED("requested"),

    @SerialName("approved")
    APPROVED("approved"),

    @SerialName("rejected")
    REJECTED("rejected"),

    @SerialName("received")
    RECEIVED("received"),

    @SerialName("refunded")
    REFUNDED("refunded");

    companion object {
        fun fromValue(value: String?): ReturnStatus {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: REQUESTED
        }
    }
}

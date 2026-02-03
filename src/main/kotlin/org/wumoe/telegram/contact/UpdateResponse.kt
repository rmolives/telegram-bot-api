package org.wumoe.telegram.contact

import kotlinx.serialization.Serializable

@Serializable
data class UpdateResponse(
    val ok: Boolean,
    val result: List<Update> = emptyList()
)
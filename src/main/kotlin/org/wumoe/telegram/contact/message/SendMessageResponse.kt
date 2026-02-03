package org.wumoe.telegram.contact.message

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResponse(
    val ok: Boolean,
    val result: Message
)
package org.wumoe.telegram.contact.callback

import kotlinx.serialization.Serializable
import org.wumoe.telegram.contact.User
import org.wumoe.telegram.contact.message.Message

@Serializable
data class CallbackQuery(
    val id: String,
    val data: String? = null,
    val message: Message? = null,
    val from: User
)
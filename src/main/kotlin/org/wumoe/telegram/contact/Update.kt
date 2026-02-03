package org.wumoe.telegram.contact

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.wumoe.telegram.contact.callback.CallbackQuery
import org.wumoe.telegram.contact.message.Message

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)
package org.wumoe.telegram.contact.inlinekeyboard

import kotlinx.serialization.Serializable

@Serializable
data class InlineKeyboardButton(
    val text: String,
    val callback_data: String? = null,
    val url: String? = null
)
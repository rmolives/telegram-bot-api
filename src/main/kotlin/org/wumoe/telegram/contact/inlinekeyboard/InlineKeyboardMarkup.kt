package org.wumoe.telegram.contact.inlinekeyboard

import kotlinx.serialization.Serializable

@Serializable
data class InlineKeyboardMarkup(
    val inline_keyboard: List<List<InlineKeyboardButton>>
)
package org.wumoe.telegram.contact.message

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: Long,
    val type: ChatType
)
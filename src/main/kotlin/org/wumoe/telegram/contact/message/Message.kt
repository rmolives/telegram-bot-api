package org.wumoe.telegram.contact.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.wumoe.telegram.contact.User
import org.wumoe.telegram.file.PhotoSize

@Serializable
data class Message(
    val message_id: Long,
    val chat: Chat,
    val from: User,
    val text: String? = null,
    val caption: String? = null,
    val photo: List<PhotoSize>? = null,
    @SerialName("new_chat_members")
    val newChatMembers: List<User>? = null,
    @SerialName("left_chat_member")
    val leftChatMember: User? = null
)
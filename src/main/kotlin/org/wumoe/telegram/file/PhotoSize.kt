package org.wumoe.telegram.file

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoSize(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    val width: Int,
    val height: Int,
    @SerialName("file_size")
    val fileSize: Int? = null
)
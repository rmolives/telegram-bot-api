package org.wumoe.telegram.file

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileInfo(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_size")
    val fileSize: Int? = null,
    @SerialName("file_path")
    val filePath: String
)
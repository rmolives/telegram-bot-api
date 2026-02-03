package org.wumoe.telegram.file

import kotlinx.serialization.Serializable

@Serializable
data class GetFileResponse(
    val ok: Boolean,
    val result: FileInfo
)
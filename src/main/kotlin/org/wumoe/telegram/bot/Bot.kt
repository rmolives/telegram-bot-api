package org.wumoe.telegram.bot

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.wumoe.telegram.contact.Update
import org.wumoe.telegram.contact.UpdateResponse
import org.wumoe.telegram.contact.User
import org.wumoe.telegram.contact.inlinekeyboard.InlineKeyboardMarkup
import org.wumoe.telegram.contact.message.ChatType
import org.wumoe.telegram.contact.message.Message
import org.wumoe.telegram.contact.message.ParseMode
import org.wumoe.telegram.contact.message.SendMessageResponse
import org.wumoe.telegram.file.GetFileResponse
import java.io.File
import java.nio.file.Files

typealias Handler = suspend BotContext.() -> Unit

class Bot(
    private val token: String,
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    val json = Json {
        ignoreUnknownKeys = true
    }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 70_000
            socketTimeoutMillis = 70_000
            connectTimeoutMillis = 10_000
        }
    }

    private val baseUrl = "https://api.telegram.org/bot$token"

    suspend fun getUpdates(
        offset: Long? = null,
        timeout: Int = 30
    ): List<Update> {
        val response: UpdateResponse =
            client.get("$baseUrl/getUpdates") {
                parameter("offset", offset)
                parameter("timeout", timeout)
            }.body()
        return response.result
    }

    suspend fun deleteMessage(chatId: Long, messageId: Long) {
        client.post("https://api.telegram.org/bot$token/deleteMessage") {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("chat_id", chatId.toString())
                        append("message_id", messageId.toString())
                    }
                )
            )
        }
    }

    suspend fun sendMessage(
        chatId: Long, text: String, replyToMessageId: Long? = null,
        keyboard: InlineKeyboardMarkup? = null, parseMode: ParseMode = ParseMode.MARKDOWN_V2
    ): Message {
        val response: SendMessageResponse = client.post("https://api.telegram.org/bot$token/sendMessage") {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("chat_id", chatId.toString())
                        append("text", text)
                        append("parse_mode", parseMode.value)
                        if (replyToMessageId != null)
                            append("reply_to_message_id", replyToMessageId.toString())
                        if (keyboard != null) {
                            append(
                                "reply_markup",
                                json.encodeToString(keyboard)
                            )
                        }
                    }
                )
            )
        }.body()
        return response.result
    }

    suspend fun sendPhoto(
        chatId: Long, file: File, caption: String? = null,
        keyboard: InlineKeyboardMarkup? = null, replyToMessageId: Long? = null,
        parseMode: ParseMode = ParseMode.MARKDOWN_V2
    ): Message {
        val response: SendMessageResponse = client.submitFormWithBinaryData(
            url = "https://api.telegram.org/bot$token/sendPhoto",
            formData = formData {
                append("chat_id", chatId.toString())
                if (caption != null)
                    append("caption", caption)
                if (replyToMessageId != null)
                    append("reply_to_message_id", replyToMessageId.toString())
                if (keyboard != null) {
                    append(
                        "reply_markup",
                        json.encodeToString(keyboard)
                    )
                }
                append(
                    "photo",
                    file.readBytes(),
                    Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${file.name}\""
                        )
                        append(
                            HttpHeaders.ContentType,
                            Files.probeContentType(file.toPath()) ?: "application/octet-stream"
                        )
                    }
                )
                append("parse_mode", parseMode.value)
            }
        ).body()
        return response.result
    }

    suspend fun answerCallback(callbackId: String, text: String? = null) {
        client.post("https://api.telegram.org/bot$token/answerCallbackQuery") {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("callback_query_id", callbackId)
                        if (text != null) append("text", text)
                    }
                )
            )
        }
    }

    suspend fun downloadPhoto(fileId: String): ByteArray {
        val res = client.get("https://api.telegram.org/bot$token/getFile") {
            parameter("file_id", fileId)
        }.body<GetFileResponse>()
        val filePath = res.result.filePath
        val url = "https://api.telegram.org/file/bot$token/$filePath"
        return client.get(url).body<ByteArray>()
    }

    private val handlers = mutableListOf<Handler>()
    private var pollingJob: Job? = null

    fun handle(block: Handler) {
        handlers += block
    }

    @Synchronized
    fun start() {
        if (pollingJob?.isActive == true) return
        runBlocking {
            pollingJob = scope.launch {
                pollingLoop()
            }
            awaitCancellation()
        }
    }

    @Synchronized
    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        scope.cancel()
    }

    private suspend fun pollingLoop() {
        var offset: Long? = null

        while (currentCoroutineContext().isActive) {
            val updates = getUpdates(offset)
            for (u in updates) {
                offset = u.updateId + 1
                dispatch(u)
            }
        }
    }

    private fun dispatch(update: Update) {
        val ctx = BotContext(this, update)
        for (handler in handlers) {
            scope.launch {
                handler(ctx)
            }
        }
    }

    fun onCallback(block: Handler) {
        handle {
            if (update.callbackQuery?.data != null)
                block()
        }
    }

    fun onGroupCallback(block: Handler) {
        handle {
            if ((chat?.type == ChatType.GROUP || chat?.type == ChatType.SUPERGROUP)
                && update.callbackQuery?.data != null
            )
                block()
        }
    }

    fun onPrivateCallback(block: Handler) {
        handle {
            if (chat?.type == ChatType.PRIVATE && update.callbackQuery?.data != null)
                block()
        }
    }

    fun onGroupText(block: Handler) {
        handle {
            if ((chat?.type == ChatType.GROUP || chat?.type == ChatType.SUPERGROUP) && text != null)
                block()
        }
    }

    fun onPrivateText(block: Handler) {
        handle {
            if (chat?.type == ChatType.PRIVATE && text != null)
                block()
        }
    }

    fun onGroup(block: Handler) {
        handle {
            if (chat?.type == ChatType.GROUP || chat?.type == ChatType.SUPERGROUP)
                block()
        }
    }

    fun onPrivate(block: Handler) {
        handle {
            if (chat?.type == ChatType.PRIVATE)
                block()
        }
    }

    fun onText(block: Handler) {
        handle {
            if (text != null)
                block()
        }
    }

    fun onPhoto(block: Handler) {
        handle {
            if (message?.photo?.isNotEmpty() == true)
                block()
        }
    }

    fun onGroupPhoto(block: Handler) {
        handle {
            if ((chat?.type == ChatType.GROUP || chat?.type == ChatType.SUPERGROUP)
                && message?.photo?.isNotEmpty() == true
            )
                block()
        }
    }

    fun onPrivatePhoto(block: Handler) {
        handle {
            if (chat?.type == ChatType.PRIVATE && message?.photo?.isNotEmpty() == true)
                block()
        }
    }

    fun onMemberJoin(block: suspend (User) -> Unit) {
        handle {
            message?.newChatMembers?.forEach { user ->
                block(user)
            }
        }
    }

    fun onMemberLeave(block: suspend (User) -> Unit) {
        handle {
            message?.leftChatMember?.let { user ->
                block(user)
            }
        }
    }

    fun on(block: Handler) {
        handle {
            block()
        }
    }
}

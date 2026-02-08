package org.wumoe.telegram.bot

import org.wumoe.telegram.contact.Update
import org.wumoe.telegram.contact.inlinekeyboard.InlineKeyboardBuilder
import org.wumoe.telegram.contact.inlinekeyboard.InlineKeyboardMarkup
import org.wumoe.telegram.contact.message.Message
import org.wumoe.telegram.contact.message.ParseMode
import java.io.File

class BotContext(
    val bot: Bot,
    val update: Update
) {
    val messageId get() = message?.message_id
    val message get() = update.message
    val chat get() = message?.chat
    val text get() = message?.text
    val callback get() = update.callbackQuery
    val caption get() = message?.caption
    val from get() = message?.from

    fun inlineKeyboard(block: InlineKeyboardBuilder.() -> Unit): InlineKeyboardMarkup {
        val builder = InlineKeyboardBuilder()
        builder.block()
        return builder.build()
    }

    suspend fun delete(message: Message) {
        bot.deleteMessage(message.message_id, message.chat.id)
    }

    suspend fun reply(
        text: String, chatId: Long? = chat?.id ?: callback?.message?.chat?.id,
        quote: Boolean = false, quoteId: Long? = messageId,
        keyboard: InlineKeyboardMarkup? = null, parseMode: ParseMode = ParseMode.PLAIN
    ) {
        val id = chatId ?: return
        bot.sendMessage(
            chatId = id,
            text = if (parseMode == ParseMode.PLAIN) escape(text) else text,
            replyToMessageId = if (quote) quoteId else null,
            keyboard = keyboard,
            parseMode = parseMode
        )
    }

    suspend fun replyPhoto(
        file: File, chatId: Long? = chat?.id ?: callback?.message?.chat?.id,
        caption: String? = null, quote: Boolean = false, quoteId: Long? = messageId,
        keyboard: InlineKeyboardMarkup? = null,
        parseMode: ParseMode = ParseMode.PLAIN
    ) {
        val id = chatId ?: return
        bot.sendPhoto(
            chatId = id, file = file, caption =
                if (caption == null) null
                else if (parseMode == ParseMode.PLAIN) escape(caption) else caption,
            replyToMessageId = if (quote) quoteId else null,
            keyboard = keyboard,
            parseMode = parseMode
        )
    }

    fun escape(text: String) =
        text.replace(Regex("""([_*\[\]()~`>#+\-=|{}.!\\])""")) { "\\${it.value}" }

    suspend fun answerCallback(text: String? = null) {
        val id = update.callbackQuery?.id ?: return
        bot.answerCallback(id, text)
    }

    suspend fun downloadPhoto(): ByteArray {
        val info = message?.photo?.last() ?: return byteArrayOf()
        return bot.downloadPhoto(info.fileId)
    }
}
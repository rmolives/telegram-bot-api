package org.wumoe.telegram.contact.inlinekeyboard

class InlineKeyboardBuilder {
    private val rows = mutableListOf<List<InlineKeyboardButton>>()

    fun row(block: RowBuilder.() -> Unit) {
        rows += RowBuilder().apply(block).build()
    }

    fun column(block: ColumnBuilder.() -> Unit) {
        rows += ColumnBuilder().apply(block).build()
    }

    fun build(): InlineKeyboardMarkup =
        InlineKeyboardMarkup(rows)

    class RowBuilder {
        private val buttons = mutableListOf<InlineKeyboardButton>()
        fun button(text: String, callback: String) {
            buttons += InlineKeyboardButton(text, callback)
        }

        fun buttonUrl(text: String, url: String) {
            buttons += InlineKeyboardButton(text, url = url)
        }

        fun build(): List<InlineKeyboardButton> = buttons
    }

    class ColumnBuilder {
        private val buttons = mutableListOf<List<InlineKeyboardButton>>()
        fun button(text: String, callback: String) {
            buttons += listOf(InlineKeyboardButton(text, callback_data = callback))
        }

        fun buttonUrl(text: String, url: String) {
            buttons += listOf(InlineKeyboardButton(text, url = url))
        }

        fun build(): List<List<InlineKeyboardButton>> = buttons
    }
}
package org.wumoe.telegram.contact.message

enum class ParseMode(val value: String) {
    MARKDOWN("Markdown"),
    MARKDOWN_V2("MarkdownV2"),
    PLAIN("MarkdownV2"),
    HTML("HTML")
}
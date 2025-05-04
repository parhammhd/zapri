package dev.parham.zapri.protocol.text

sealed class TextElement {
    data class Text(val text: String) : TextElement()
    data class Link(val url: String, val text: String = url) : TextElement()
    object EmptyLine : TextElement()
}


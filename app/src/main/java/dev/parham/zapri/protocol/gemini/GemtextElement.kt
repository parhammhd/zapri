package dev.parham.zapri.protocol.gemini

sealed class GemtextElement {
    data class Link(val url: String, val description: String) : GemtextElement()
    data class Heading(val text: String, val level: Int) : GemtextElement()
    data class Preformatted(val text: String, val altText: String, val lang: String?) : GemtextElement()
    data class ListItem(val text: String) : GemtextElement()
    data class Quote(val text: String) : GemtextElement()
    data class Text(val text: String) : GemtextElement()
    object EmptyLine : GemtextElement()
}
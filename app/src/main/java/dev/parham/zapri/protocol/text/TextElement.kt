package dev.parham.zapri.protocol.text

sealed class TextElement {
    data class Text(val text: String) : TextElement()
    data class Link(
        val url: String, 
        val text: String = url,
        val rel: String? = null  // Add rel attribute support
    ) : TextElement()
    data class MetadataInfo(val text: String) : TextElement()  // For ℹ lines
    data class StatusInfo(val text: String) : TextElement()    // For ✔ lines
    object EmptyLine : TextElement()
    object HorizontalRule : TextElement()
    object Divider : TextElement()  // For —
}


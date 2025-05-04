package dev.parham.zapri.protocol.finger

sealed class FingerElement {
    data class Text(val text: String) : FingerElement()
    data class Link(val url: String, val text: String = url) : FingerElement()
    object EmptyLine : FingerElement()
}


package dev.parham.zapri.protocol

import androidx.compose.runtime.Composable
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.protocol.gemini.GeminiClient
import dev.parham.zapri.protocol.gemini.GeminiParser
import dev.parham.zapri.protocol.gemini.GeminiRenderer
import dev.parham.zapri.protocol.gemini.GemtextElement
import dev.parham.zapri.protocol.finger.FingerClient
import dev.parham.zapri.protocol.finger.FingerParser
import dev.parham.zapri.protocol.finger.FingerElement
import dev.parham.zapri.protocol.finger.FingerRenderer
import dev.parham.zapri.protocol.text.TextClient
import dev.parham.zapri.protocol.text.TextParser
import dev.parham.zapri.protocol.text.TextElement
import dev.parham.zapri.protocol.text.TextRenderer
import dev.parham.zapri.utils.UrlUtils

object ProtocolHandler {

    fun fetch(url: String, context: android.content.Context): PageData {
        val parsedUrl = UrlUtils.parse(url)
            ?: return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Invalid URL. Please check the format.",
                statusMessage = "Invalid URL"
            )

        return when (parsedUrl.protocol) {
            "gemini" -> GeminiClient.fetch(url, context)
            "finger" -> FingerClient.fetch(url, context)
            "text" -> TextClient.fetch(url, context)
            "gopher", "scroll", "nex", "spartan" -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "${parsedUrl.protocol.capitalize()} protocol is not yet implemented.",
                statusMessage = "Protocol Not Implemented"
            )
            else -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Unsupported protocol '${parsedUrl.protocol}'.",
                statusMessage = "Unsupported Protocol"
            )
        }
    }

    fun parseContent(pageData: PageData, baseUrl: String): List<Any> {
        val parsedUrl = UrlUtils.parse(baseUrl)
            ?: return emptyList()

        return when (parsedUrl.protocol) {
            "gemini" -> pageData.content?.let { GeminiParser.parse(it, baseUrl) } ?: emptyList()
            "finger" -> pageData.content?.let { FingerParser.parse(it) } ?: emptyList()
            "text" -> {
                val elements = mutableListOf<TextElement>()
                // Add metadata info
                pageData.meta.takeIf { it.isNotEmpty() }?.let {
                    elements.add(TextElement.MetadataInfo(it))
                    elements.add(TextElement.HorizontalRule)
                }
                // Add content
                elements.addAll(pageData.content?.let { TextParser.parse(it) } ?: emptyList())
                elements
            }
            else -> emptyList()
        }
    }

    fun resolveRelativeUrl(baseUrl: String, relativeUrl: String): String {
        val parsedUrl = UrlUtils.parse(baseUrl) ?: return baseUrl
        
        return when (parsedUrl.protocol) {
            "gemini" -> UrlUtils.resolveGeminiUrl(baseUrl, relativeUrl)
            else -> {
                // Default handling for other protocols
                if (baseUrl.endsWith("/")) baseUrl + relativeUrl
                else "$baseUrl/$relativeUrl"
            }
        }
    }

    @Composable
    fun RenderContent(
        elements: List<Any>,
        onLinkClick: (String) -> Unit
    ) {
        elements.forEach { element ->
            when (element) {
                is GemtextElement -> GeminiRenderer.RenderGemtextElement(element, onLinkClick)
                is FingerElement -> FingerRenderer.RenderFingerElement(element, onLinkClick)
                is TextElement -> TextRenderer.RenderTextElement(element, onLinkClick)
            }
        }
    }
}

package dev.parham.zapri.protocol

import androidx.compose.runtime.Composable
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.protocol.gemini.GeminiClient
import dev.parham.zapri.protocol.gemini.GeminiParser
import dev.parham.zapri.protocol.gemini.GeminiRenderer
import dev.parham.zapri.protocol.gemini.GemtextElement
import dev.parham.zapri.utils.UrlParser

object ProtocolHandler {

    fun fetch(url: String, context: android.content.Context): PageData {
        val parsedUrl = UrlParser.parse(url)
            ?: return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Invalid URL. Please check the format.",
                statusMessage = "Invalid URL"
            )

        return when (parsedUrl.protocol) {
            "gemini" -> GeminiClient.fetch(url, context)
            "gopher", "finger", "scroll", "nex", "spartan", "text" -> PageData(
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

    fun parseContent(pageData: PageData, baseUrl: String): List<GemtextElement> {
        val parsedUrl = UrlParser.parse(baseUrl)
            ?: return emptyList()

        return when (parsedUrl.protocol) {
            "gemini" -> pageData.content?.let { GeminiParser.parse(it, baseUrl) } ?: emptyList()
            else -> emptyList() // Later, other protocol parsers can be added here
        }
    }

    @Composable
    fun renderContent(
        elements: List<GemtextElement>,
        onLinkClick: (String) -> Unit
    ) {
        val parsedUrl = elements.firstOrNull()
        // Currently assume it's Gemini (later: expand if different types)
        elements.forEach { element ->
            GeminiRenderer.RenderGemtextElement(element, onLinkClick)
        }
    }
}

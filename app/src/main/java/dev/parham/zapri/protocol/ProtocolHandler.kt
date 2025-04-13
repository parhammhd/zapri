package dev.parham.zapri.protocol

import android.content.Context
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.protocol.gemini.GeminiClient
import dev.parham.zapri.utils.UrlParser

object ProtocolHandler {

    fun fetch(url: String, context: Context): PageData {
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
            "gopher" -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Gopher protocol is not yet implemented.",
                statusMessage = "Protocol Not Implemented"
            )
            "finger" -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Finger protocol is not yet implemented.",
                statusMessage = "Protocol Not Implemented"
            )
            "scroll" -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Scroll protocol is not yet implemented.",
                statusMessage = "Protocol Not Implemented"
            )
            "nex" -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Nex protocol is not yet implemented.",
                statusMessage = "Protocol Not Implemented"
            )
            "spartan" -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Spartan protocol is not yet implemented.",
                statusMessage = "Protocol Not Implemented"
            )
            "text" -> PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Text protocol is not yet implemented.",
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
}


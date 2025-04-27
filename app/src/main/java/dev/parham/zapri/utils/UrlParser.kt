package dev.parham.zapri.utils

data class ParsedUrl(
    val protocol: String,
    val host: String,
    val fullUrl: String
)

object UrlParser {

    fun parse(url: String): ParsedUrl? {
        val protocolEndIndex = url.indexOf("://")
        if (protocolEndIndex == -1) {
            return null // Invalid URL: Missing protocol
        }

        val protocol = url.substring(0, protocolEndIndex)
        val remainingUrl = url.substring(protocolEndIndex + 3)

        val hostEndIndex = remainingUrl.indexOf("/")
        val host = if (hostEndIndex == -1) remainingUrl else remainingUrl.substring(0, hostEndIndex)
        
        if (host.isBlank()) {
            return null // Invalid URL: Missing host
        }

        // Handle paths for Gemini protocol
        val path = if (hostEndIndex == -1) {
            "/"
        } else {
            val rawPath = remainingUrl.substring(hostEndIndex)
            if (protocol == "gemini" && rawPath.contains("~")) {
                // For user paths, ensure they start at root level
                val parts = rawPath.split("~", limit = 2)
                if (parts.size > 1) {
                    "/~${parts[1]}" // Ensure ~ paths start at root
                } else {
                    rawPath
                }
            } else {
                rawPath
            }
        }

        val fullUrl = "$protocol://$host$path"

        return ParsedUrl(protocol, host, fullUrl)
    }
}

package dev.parham.zapri.utils

data class ParsedUrl(
    val protocol: String,
    val host: String,
    val path: String // This will now represent the full URL
)

object UrlParser {

    /**
     * Parses a URL into its protocol, host, and path components.
     * @param url The URL to parse.
     * @return A ParsedUrl object containing the protocol, host, and path, or null if invalid.
     */
    fun parse(url: String): ParsedUrl? {
        val protocolEndIndex = url.indexOf("://")
        if (protocolEndIndex == -1) {
            return null // Invalid URL: Missing protocol
        }

        val protocol = url.substring(0, protocolEndIndex)
        val remainingUrl = url.substring(protocolEndIndex + 3)

        val hostEndIndex = remainingUrl.indexOf("/")
        val host = if (hostEndIndex == -1) remainingUrl else remainingUrl.substring(0, hostEndIndex)
        val path = if (hostEndIndex == -1) "$protocol://$host/" else "$protocol://$remainingUrl/"

        if (host.isBlank()) {
            return null // Invalid URL: Missing host
        }

        return ParsedUrl(protocol, host, path)
    }
}

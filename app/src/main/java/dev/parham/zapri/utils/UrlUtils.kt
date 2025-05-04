package dev.parham.zapri.utils

import java.net.URI

data class ParsedUrl(
    val protocol: String,
    val host: String,
    val fullUrl: String
)

object UrlUtils {

    fun parse(url: String): ParsedUrl? {
        return try {
            val uri = URI(url)

            val scheme = uri.scheme ?: return null
            val host = uri.host ?: uri.authority ?: return null // IPv6 support

            // Normalize the URI (removes ../ etc)
            val normalizedUrl = uri.normalize().toString()
            ParsedUrl(scheme, host, normalizedUrl)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resolves a Gemini-specific relative URL against a base.
     * Handles ~ and // as absolute-from-root cases.
     */
    fun resolveGeminiUrl(base: String, link: String): String {
        val trimmedLink = link.trim()

        val result = when {
            // Already absolute Gemini URL
            trimmedLink.startsWith("gemini://") -> trimmedLink

            // Root-level ~user → gemini://host/~user
            trimmedLink.startsWith("~") -> {
                val baseUri = URI(base)
                "gemini://${baseUri.host}/~${trimmedLink.removePrefix("~")}"
            }

            // Root-absolute path: //something → /something
            trimmedLink.startsWith("//") -> {
                val baseUri = URI(base)
                "gemini://${baseUri.host}/${trimmedLink.removePrefix("//")}"
            }

            else -> {
                val baseUri = URI(base)
                val resolved = baseUri.resolve(trimmedLink)
                resolved.toString()
            }
        }

        // Add trailing slash for paths that don't end with file extensions
        return if (!result.endsWith("/") && !result.contains(".")) {
            "$result/"
        } else {
            result
        }
    }
}

package dev.parham.zapri.utils

import java.net.URL

object GemtextParser {

    fun parse(content: String, baseUrl: String): List<GemtextElement> {
        val lines = content.lines()
        val elements = mutableListOf<GemtextElement>()

        // Normalize baseUrl to ensure it ends with a "/"
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        for (line in lines) {
            when {
                line.startsWith("=>") -> {
                    // Replace tabs with spaces and then split
                    val parts = line.substring(2).trim().replace("\t", " ").split(" ", limit = 2)
                    val rawUrl = parts[0].trim() // Ensure the raw URL is trimmed
                    val description = parts.getOrNull(1)?.trim() ?: rawUrl // Trim the description as well

                    // Resolve relative URLs to absolute URLs
                    val resolvedUrl = try {
                        val resolved = URL(URL(normalizedBaseUrl), rawUrl).toString()
                        resolved
                    } catch (e: Exception) {
                        rawUrl
                    }

                    elements.add(GemtextElement.Link(resolvedUrl, description))
                }
                line.startsWith("### ") -> elements.add(GemtextElement.Heading(line.substring(4), level = 3))
                line.startsWith("## ") -> elements.add(GemtextElement.Heading(line.substring(3), level = 2))
                line.startsWith("# ") -> elements.add(GemtextElement.Heading(line.substring(2), level = 1))
                line.startsWith("```") -> elements.add(GemtextElement.Preformatted(line.substring(3)))
                line.isBlank() -> elements.add(GemtextElement.EmptyLine)
                else -> elements.add(GemtextElement.Text(line))
            }
        }

        return elements
    }
}

sealed class GemtextElement {
    data class Link(val url: String, val description: String) : GemtextElement()
    data class Heading(val text: String, val level: Int) : GemtextElement()
    data class Preformatted(val text: String) : GemtextElement()
    data class Text(val text: String) : GemtextElement()
    object EmptyLine : GemtextElement()
}
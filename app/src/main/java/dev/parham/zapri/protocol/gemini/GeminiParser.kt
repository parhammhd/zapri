package dev.parham.zapri.protocol.gemini

import java.net.URL

object GeminiParser {

    fun parse(content: String, baseUrl: String): List<GemtextElement> {
        val lines = content.replace("\r\n", "\n").lines()
        val elements = mutableListOf<GemtextElement>()

        val base = try {
            URL(baseUrl)
        } catch (e: Exception) {
            null
        }

        val linesIterator = lines.iterator()
        while (linesIterator.hasNext()) {
            val line = linesIterator.next()
            when {
                line.startsWith("=>") -> {
                    val parts = line.substring(2).trim().replace("\t", " ").split(" ", limit = 2)
                    val rawUrl = parts[0].trim()
                    val description = parts.getOrNull(1)?.trim() ?: rawUrl

                    val resolvedUrl = try {
                        base?.let { URL(it, rawUrl).toString() } ?: rawUrl
                    } catch (e: Exception) {
                        rawUrl
                    }

                    elements.add(GemtextElement.Link(resolvedUrl, description))
                }
                line.startsWith("### ") -> elements.add(GemtextElement.Heading(line.substring(4), level = 3))
                line.startsWith("## ") -> elements.add(GemtextElement.Heading(line.substring(3), level = 2))
                line.startsWith("# ") -> elements.add(GemtextElement.Heading(line.substring(2), level = 1))
                line.startsWith("```") -> {
                    val preformattedHeader = line.substring(3).trim()
                    val lang = preformattedHeader.takeIf { it.isNotBlank() }
                    val preformattedContent = StringBuilder()
                    while (linesIterator.hasNext()) {
                        val nextLine = linesIterator.next()
                        if (nextLine.startsWith("```")) break
                        preformattedContent.appendLine(nextLine)
                    }
                    elements.add(GemtextElement.Preformatted(preformattedContent.toString().trim(), preformattedHeader, lang))
                }
                line.startsWith("* ") -> elements.add(GemtextElement.ListItem(line.substring(2).trim()))
                line.startsWith("> ") -> elements.add(GemtextElement.Quote(line.substring(2).trim()))
                line.isBlank() -> elements.add(GemtextElement.EmptyLine)
                line.length > 1024 -> elements.add(GemtextElement.Text(line.substring(0, 1024)))
                else -> elements.add(GemtextElement.Text(line))
            }
        }

        return elements
    }
}

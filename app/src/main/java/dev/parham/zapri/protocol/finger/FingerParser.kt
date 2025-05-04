package dev.parham.zapri.protocol.finger

object FingerParser {
    // Regex for URL detection (supports http, https, gemini, finger protocols)
    private val URL_PATTERN = Regex("""((?:https?|gemini|finger|text|nex|spartan|scroll|gopher|local|browser)://[^\s<>]+)""")

    fun parse(content: String): List<FingerElement> {
        return content.lines().flatMap { line ->
            if (line.isBlank()) {
                listOf(FingerElement.EmptyLine)
            } else {
                // Find all URLs in the line
                val matches = URL_PATTERN.findAll(line)
                if (matches.none()) {
                    // No URLs, return as plain text
                    listOf(FingerElement.Text(line))
                } else {
                    // Split line into text and links
                    val elements = mutableListOf<FingerElement>()
                    var lastEnd = 0
                    
                    matches.forEach { match ->
                        // Add text before the URL if any
                        if (match.range.first > lastEnd) {
                            elements.add(FingerElement.Text(line.substring(lastEnd, match.range.first)))
                        }
                        // Add the URL as a link
                        elements.add(FingerElement.Link(match.value))
                        lastEnd = match.range.last + 1
                    }
                    
                    // Add remaining text after last URL if any
                    if (lastEnd < line.length) {
                        elements.add(FingerElement.Text(line.substring(lastEnd)))
                    }
                    
                    elements
                }
            }
        }
    }
}


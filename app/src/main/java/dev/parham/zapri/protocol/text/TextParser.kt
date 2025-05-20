package dev.parham.zapri.protocol.text

object TextParser {
    private val URL_PATTERN = Regex("""((?:https?|gemini|finger|text|nex|spartan|scroll|gopher|local|browser|geo|tag)://[^\s<>]+)""")
    private val ARROW_LINK_PATTERN = Regex("""=>[\s]+([^\s]+)(?:[\s]+([^r]+))?(?:\s+rel=(\w+))?""")
    private val DIVIDER_PATTERN = Regex("""^[—-]$""")
    private val HR_PATTERN = Regex("""^[✂︎・]{10,}$""")

    fun parse(content: String): List<TextElement> {
        return content.lines().flatMap { line ->
            when {
                line.isBlank() -> listOf(TextElement.EmptyLine)
                line.startsWith("ℹ ") -> listOf(TextElement.MetadataInfo(line.substring(2)))
                line.startsWith("✔ ") -> listOf(TextElement.StatusInfo(line.substring(2)))
                HR_PATTERN.matches(line.trim()) -> listOf(TextElement.HorizontalRule)
                DIVIDER_PATTERN.matches(line) -> listOf(TextElement.Divider)
                else -> {
                    // First check for arrow-style links
                    val arrowMatch = ARROW_LINK_PATTERN.find(line)
                    if (arrowMatch != null) {
                        val url = arrowMatch.groupValues[1]
                        val description = arrowMatch.groupValues.getOrNull(2) ?: url
                        val rel = arrowMatch.groupValues.getOrNull(3)
                        listOf(TextElement.Link(url, description, rel))
                    } else {
                        // Fall back to regular URL parsing
                        val matches = URL_PATTERN.findAll(line)
                        if (matches.none()) {
                            listOf(TextElement.Text(line))
                        } else {
                            val elements = mutableListOf<TextElement>()
                            var lastEnd = 0
                            
                            matches.forEach { match ->
                                if (match.range.first > lastEnd) {
                                    elements.add(TextElement.Text(line.substring(lastEnd, match.range.first)))
                                }
                                elements.add(TextElement.Link(match.value))
                                lastEnd = match.range.last + 1
                            }
                            
                            if (lastEnd < line.length) {
                                elements.add(TextElement.Text(line.substring(lastEnd)))
                            }
                            
                            elements
                        }
                    }
                }
            }
        }
    }
}


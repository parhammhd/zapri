package dev.parham.zapri.protocol.text

object TextParser {
    private val URL_PATTERN = Regex("""((?:https?|gemini|finger|text|nex|spartan|scroll|gopher|local|browser)://[^\s<>]+)""")

    fun parse(content: String): List<TextElement> {
        return content.lines().flatMap { line ->
            if (line.isBlank()) {
                listOf(TextElement.EmptyLine)
            } else {
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


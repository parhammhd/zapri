package dev.parham.zapri.protocol.gemini

import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

object GeminiRenderer {

    @Composable
    fun RenderGemtextElement(
        element: GemtextElement,
        onLinkClick: (String) -> Unit
    ) {
        when (element) {
            is GemtextElement.Link -> {
                val annotatedString = buildAnnotatedString {
                    pushStringAnnotation(tag = "URL", annotation = element.url)
                    withStyle(
                        style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(element.description)
                    }
                    pop()
                }

                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                onLinkClick(annotation.item)
                            }
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            is GemtextElement.Heading -> {
                Text(
                    text = element.text,
                    style = when (element.level) {
                        1 -> MaterialTheme.typography.headlineLarge
                        2 -> MaterialTheme.typography.headlineMedium
                        else -> MaterialTheme.typography.headlineSmall
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            is GemtextElement.Preformatted -> {
                Text(
                    text = element.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
            is GemtextElement.ListItem -> {
                Text(
                    text = "• ${element.text}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            is GemtextElement.Quote -> {
                Text(
                    text = "> ${element.text}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            is GemtextElement.Text -> {
                Text(
                    text = element.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            is GemtextElement.EmptyLine -> {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

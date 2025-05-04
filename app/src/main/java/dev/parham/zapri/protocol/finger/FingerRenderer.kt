package dev.parham.zapri.protocol.finger

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

object FingerRenderer {

    @Composable
    fun RenderFingerElement(
        element: FingerElement,
        onLinkClick: (String) -> Unit
    ) {
        when (element) {
            is FingerElement.Link -> {
                val linkColor = when {
                    // Internal links (finger or relative)
                    element.url.startsWith("finger://") || !element.url.contains("://") ->
                        MaterialTheme.colorScheme.primary
                    // Supported external protocols
                    element.url.startsWith("gemini://") ||
                    element.url.startsWith("gopher://") ||
                    element.url.startsWith("nex://") ||
                    element.url.startsWith("scroll://") ||
                    element.url.startsWith("spartan://") ||
                    element.url.startsWith("text://") ||
                    element.url.startsWith("browser://") ||
                    element.url.startsWith("local://") ->
                        MaterialTheme.colorScheme.secondary
                    // Unsupported external protocols (http, https, etc)
                    else -> MaterialTheme.colorScheme.tertiary
                }

                val annotatedString = buildAnnotatedString {
                    pushStringAnnotation(tag = "URL", annotation = element.url)
                    withStyle(
                        style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(element.text)
                    }
                    pop()
                }

                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                onLinkClick(annotation.item)
                            }
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            is FingerElement.Text -> {
                Text(
                    text = element.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            is FingerElement.EmptyLine -> {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

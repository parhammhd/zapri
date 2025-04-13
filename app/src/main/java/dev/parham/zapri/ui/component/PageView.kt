package dev.parham.zapri.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.utils.GemtextElement
import dev.parham.zapri.utils.GemtextParser

@Composable
fun PageView(
    pageData: PageData,
    baseUrl: String, // Base URL for resolving relative links
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit // Callback for link clicks
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        when {
            pageData.errorMessage != null -> {
                // Display error message
                Text(
                    text = pageData.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            pageData.content != null -> {
                // Parse and display Gemtext content
                val elements = GemtextParser.parse(pageData.content, baseUrl) // Pass baseUrl here
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()) // Enable scrolling
                ) {
                    elements.forEach { element ->
                        RenderGemtextElement(element, onLinkClick)
                    }
                }
            }
            else -> {
                // Display status message
                Text(
                    text = pageData.statusMessage ?: "Unknown status",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun RenderGemtextElement(
    element: GemtextElement,
    onLinkClick: (String) -> Unit // Callback for link clicks
) {
    when (element) {
        is GemtextElement.Link -> {
            // Render clickable links
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
                            onLinkClick(annotation.item) // Trigger the callback with the resolved URL
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

@Preview(showBackground = true)
@Composable
fun PageViewPreview() {
    val samplePageData = PageData(
        statusCode = 20,
        meta = "text/gemini",
        content = """
            # Welcome to Zapri!
            ## This is a Gemini Capsule
            => gemini://example.com Example Link
            Here is some plain text.
            ```
            Preformatted text block
            ```
        """.trimIndent(),
        errorMessage = null,
        statusMessage = "Success"
    )
    PageView(pageData = samplePageData, baseUrl = "gemini://geminiprotocol.net", onLinkClick = { url -> println("Clicked URL: $url") })
}

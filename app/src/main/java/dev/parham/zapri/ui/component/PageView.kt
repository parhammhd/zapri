package dev.parham.zapri.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.protocol.ProtocolHandler

@Composable
fun PageView(
    pageData: PageData,
    baseUrl: String,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        when {
            pageData.errorMessage != null -> {
                Text(
                    text = pageData.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            pageData.content != null -> {
                val elements = ProtocolHandler.parseContent(pageData, baseUrl)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    ProtocolHandler.RenderContent(elements, onLinkClick)
                }
            }
            else -> {
                Text(
                    text = pageData.statusMessage ?: "Unknown status",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

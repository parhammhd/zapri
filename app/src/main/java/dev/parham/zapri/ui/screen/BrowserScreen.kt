package dev.parham.zapri.ui.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.parham.zapri.protocol.ProtocolHandler
import dev.parham.zapri.data.history.HistoryRepository
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.ui.component.PageView
import dev.parham.zapri.ui.component.ProtocolIcon
import dev.parham.zapri.utils.UrlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

@Composable
fun BrowserScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val historyRepository = remember { HistoryRepository() }
    var currentUrl by remember { mutableStateOf(TextFieldValue("gemini://geminiprotocol.net/")) }
    var pageData by remember {
        mutableStateOf(
            PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = null,
                statusMessage = "Welcome to Zapri! Enter a URL to begin."
            )
        )
    }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = !historyRepository.isEmpty()) {
        val previousUrl = historyRepository.pop()
        if (previousUrl != null) {
            currentUrl = TextFieldValue(previousUrl)
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    ProtocolHandler.fetch(previousUrl, context)
                }
                pageData = result
            }
        } else {
            navController.popBackStack()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = currentUrl,
            onValueChange = { currentUrl = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            placeholder = { Text("Enter URL...") },
            leadingIcon = {
                ProtocolIcon(
                    protocol = UrlUtils.parse(currentUrl.text)?.protocol ?: "unknown",
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            historyRepository.push(currentUrl.text)
                            val result = withContext(Dispatchers.IO) {
                                ProtocolHandler.fetch(currentUrl.text, context)
                            }
                            pageData = result
                        }
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Go")
                }
            }
        )

        PageView(
            pageData = pageData,
            baseUrl = currentUrl.text,
            onLinkClick = { url ->
                coroutineScope.launch {
                    val processedUrl = url.substringBefore(" ").trim()
                    
                    // Check if URL is http/https and open in default browser
                    if (processedUrl.startsWith("http://") || processedUrl.startsWith("https://")) {
                        val intent = Intent(Intent.ACTION_VIEW, processedUrl.toUri())
                        context.startActivity(intent)
                        return@launch
                    }

                    // Handle other protocols normally
                    historyRepository.push(currentUrl.text)
                    val appendedUrl = ProtocolHandler.resolveRelativeUrl(currentUrl.text, processedUrl)
                    currentUrl = TextFieldValue(appendedUrl)
                    val result = withContext(Dispatchers.IO) {
                        ProtocolHandler.fetch(appendedUrl, context)
                    }
                    pageData = result
                }
            }
        )
    }
}
package dev.parham.zapri.protocol.text

import android.content.Context
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.utils.UrlUtils
import java.io.*
import java.net.Socket
import java.net.URI
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

object TextClient {
    private const val TEXT_PORT = 1961
    private const val TIMEOUT_MS = 30000

    fun fetch(url: String, context: Context): PageData {
        val parsedUrl = UrlUtils.parse(url) ?: return PageData(
            statusCode = -1,
            meta = "",
            content = null,
            errorMessage = "Error: Invalid URL format.",
            statusMessage = "Invalid URL"
        )

        return try {
            val socket = Socket(parsedUrl.host, TEXT_PORT).apply {
                soTimeout = TIMEOUT_MS
            }
            
            // Get path or use "/" if no path specified
            val path = URI(url).path.takeIf { it.isNotEmpty() } ?: "/"
            
            // Send request
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
            writer.write("$path\r\n")
            writer.flush()
            
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))

            // Read response
            val content = buildString {
                var line: String? = null
                while (reader.readLine()?.also { line = it } != null) {
                    appendLine(line)
                }
            }

            socket.close()

            PageData(
                statusCode = 0,
                meta = "text/plain; charset=utf-8",
                content = content,
                errorMessage = null,
                statusMessage = "Text Response"
            )
        } catch (e: UnknownHostException) {
            PageData(
                statusCode = 0,
                meta = "",
                content = null,
                errorMessage = "Error: Unknown host '${parsedUrl.host}'",
                statusMessage = "Connection Failed"
            )
        } catch (e: Exception) {
            PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: ${e.message ?: "An unknown error occurred."}",
                statusMessage = "Connection Error"
            )
        }
    }
}


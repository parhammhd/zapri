package dev.parham.zapri.protocol.text

import android.content.Context
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.utils.UrlUtils
import java.io.*
import java.net.Socket
import java.net.URI
import java.nio.charset.StandardCharsets

object TextClient {
    private const val PLAINTEXT_PORT = 1961
    private const val TIMEOUT_MS = 30000

    fun fetch(url: String, context: Context): PageData {
        val parsedUrl = UrlUtils.parse(url) ?: return PageData(
            statusCode = -1,
            meta = "",
            content = null,
            errorMessage = "Error: Invalid URL format.",
            statusMessage = "Invalid URL"
        )

        // Validate URL format according to server requirements
        if (!url.lowercase().startsWith("text://")) {
            return PageData(
                statusCode = 40,
                meta = "",
                content = null,
                errorMessage = "Invalid protocol. Must be text://",
                statusMessage = "NOK"
            )
        }

        var socket: Socket? = null
        return try {
            socket = Socket(parsedUrl.host, PLAINTEXT_PORT).apply {
                soTimeout = TIMEOUT_MS
            }

            // Create URI to properly parse the path
            val uri = URI(url)
            
            // Process path according to server rules:
            // - If path is empty or ends with /, request index.txt
            // - Must be lowercase
            // - No spaces allowed
            // - Max 1024 chars
            val path = when {
                uri.path.isEmpty() || uri.path == "/" -> "/index.txt"
                !uri.path.contains(".") -> "${uri.path}.txt"
                else -> uri.path.lowercase()
            }.let { 
                if (it.length > 1024) throw IllegalArgumentException("Path too long")
                if (it.contains(" ")) throw IllegalArgumentException("Path cannot contain spaces")
                it
            }

            // Send request: <path>\n (not CRLF)
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
            writer.write("$path\n")
            writer.flush()

            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            val statusLine = reader.readLine()

            // Parse status line: <status> <description>
            if (statusLine == null) throw IOException("Empty response")
            
            val (status, description) = statusLine.split(" ", limit = 2)
            val statusCode = status.toIntOrNull() ?: 40

            when (statusCode) {
                20 -> PageData(  // OK with content
                    statusCode = 20,
                    meta = description,
                    content = buildString {
                        var line: String? = null
                        while (reader.readLine()?.also { line = it } != null) {
                            appendLine(line)
                        }
                    }.trim(),
                    errorMessage = null,
                    statusMessage = "OK"
                )
                30 -> PageData(  // Redirect
                    statusCode = 30,
                    meta = description,
                    content = null,
                    errorMessage = null,
                    statusMessage = "Redirect"
                )
                else -> PageData(  // NOK or any other response
                    statusCode = 40,
                    meta = "",
                    content = null,
                    errorMessage = description,
                    statusMessage = "NOK"
                )
            }
        } catch (e: Exception) {
            PageData(
                statusCode = 40,
                meta = "",
                content = null,
                errorMessage = e.message ?: "Unknown error",
                statusMessage = "NOK"
            )
        } finally {
            socket?.close()
        }
    }
}


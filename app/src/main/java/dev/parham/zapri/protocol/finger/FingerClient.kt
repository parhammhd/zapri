package dev.parham.zapri.protocol.finger

import android.content.Context
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.utils.UrlUtils
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

object FingerClient {
    private const val FINGER_PORT = 79
    private const val TIMEOUT_MS = 30000 // 30 seconds timeout as recommended

    fun fetch(url: String, context: Context): PageData {
        val parsedUrl = UrlUtils.parse(url) ?: return PageData(
            statusCode = -1,
            meta = "",
            content = null,
            errorMessage = "Error: Invalid URL. Please check the format.",
            statusMessage = "Invalid URL"
        )

        val host = parsedUrl.host
        val query = parsedUrl.fullUrl.substringAfter("finger://")
                                   .substringAfter(host)
                                   .trim('/')

        return try {
            val socket = Socket(host, FINGER_PORT).apply {
                soTimeout = TIMEOUT_MS
            }
            
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII))

            // Send query followed by CRLF, or just CRLF for empty query
            if (query.isNotEmpty()) {
                writer.write(query)
            }
            writer.write("\r\n")
            writer.flush()

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
                meta = "text/plain; charset=us-ascii",
                content = content,
                errorMessage = null,
                statusMessage = "Finger Response"
            )

        } catch (e: UnknownHostException) {
            PageData(
                statusCode = 0,
                meta = "",
                content = null,
                errorMessage = "Error: Unknown host '$host'.",
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


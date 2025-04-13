package dev.parham.zapri.protocol.gemini

import android.content.Context
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.utils.CertificateStorage
import dev.parham.zapri.utils.UrlParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.UnknownHostException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

object GeminiClient {

    private const val GEMINI_PORT = 1965
    private const val MAX_CONTENT_SIZE = 10 * 1024 * 1024 // 10 MB limit for content

    fun fetch(url: String, context: Context): PageData {
        val parsedUrl = UrlParser.parse(url)
            ?: return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Invalid URL. Please check the format.",
                statusMessage = "Invalid URL"
            )

        if (parsedUrl.protocol != "gemini") {
            return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Unsupported protocol '${parsedUrl.protocol}'.",
                statusMessage = "Unsupported Protocol"
            )
        }

        val host = parsedUrl.host
        val path = parsedUrl.path

        return try {
            InetAddress.getByName(host)

            val sslContext = SSLContext.getInstance("TLS")
            val certificateStorage = CertificateStorage(context)
            sslContext.init(null, arrayOf(SelfSignedTrustManager(certificateStorage)), SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            val socket = sslSocketFactory.createSocket(host, GEMINI_PORT) as SSLSocket
            socket.soTimeout = 5000
            socket.startHandshake()

            val writer = OutputStreamWriter(socket.getOutputStream())
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            writer.write("$path\r\n")
            writer.flush()

            val statusLine = reader.readLine() ?: return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Empty response from server.",
                statusMessage = "Empty Response"
            )

            if (statusLine.length < 3) {
                return PageData(
                    statusCode = -1,
                    meta = "",
                    content = null,
                    errorMessage = "Error: Malformed response from server.",
                    statusMessage = "Malformed Response"
                )
            }

            val statusCode = statusLine.substring(0, 2).toIntOrNull() ?: -1
            val meta = statusLine.substring(3)

            when (statusCode) {
                20 -> {
                    val content = buildString {
                        var line: String?
                        var totalSize = 0
                        while (reader.readLine().also { line = it } != null) {
                            totalSize += line!!.length
                            if (totalSize > MAX_CONTENT_SIZE) {
                                return PageData(
                                    statusCode = -1,
                                    meta = "",
                                    content = null,
                                    errorMessage = "Error: Content size exceeds limit.",
                                    statusMessage = "Content Too Large"
                                )
                            }
                            appendLine(line)
                        }
                    }
                    PageData(
                        statusCode = statusCode,
                        meta = meta,
                        content = content,
                        errorMessage = null,
                        statusMessage = "Success"
                    )
                }
                30 -> PageData(
                    statusCode = statusCode,
                    meta = meta,
                    content = null,
                    errorMessage = null,
                    statusMessage = "Redirect: $meta"
                )
                else -> PageData(
                    statusCode = statusCode,
                    meta = meta,
                    content = null,
                    errorMessage = "Error: $meta",
                    statusMessage = "Error"
                )
            }
        } catch (e: UnknownHostException) {
            PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Unknown host '$host'.",
                statusMessage = "Unknown Host"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: ${e.message ?: "An unknown error occurred."}",
                statusMessage = "Connection Error"
            )
        }
    }

    private class SelfSignedTrustManager(private val certificateStorage: CertificateStorage) : X509TrustManager {

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            if (chain == null || chain.isEmpty()) {
                throw CertificateException("No certificates provided by the server.")
            }

            val serverCertificate = chain[0]
            val fingerprint = computeSHA256Fingerprint(serverCertificate)
            val host = serverCertificate.subjectDN.name

            val storedFingerprint = certificateStorage.getFingerprint(host)
            if (storedFingerprint == null) {
                certificateStorage.saveFingerprint(host, fingerprint)
            } else if (storedFingerprint != fingerprint) {
                throw CertificateException("Certificate mismatch for $host. Possible MITM attack.")
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        private fun computeSHA256Fingerprint(certificate: X509Certificate): String {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val encoded = certificate.encoded
            val hash = digest.digest(encoded)
            return hash.joinToString(":") { "%02x".format(it) }
        }
    }
}
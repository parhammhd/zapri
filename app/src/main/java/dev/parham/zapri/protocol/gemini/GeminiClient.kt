package dev.parham.zapri.protocol.gemini

import android.content.Context
import dev.parham.zapri.data.model.PageData
import dev.parham.zapri.utils.UrlUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

object GeminiClient {

    private const val GEMINI_PORT = 1965
    private const val MAX_CONTENT_SIZE = 10 * 1024 * 1024 // 10 MB limit for content
    private const val MAX_REDIRECTS = 5 // Maximum number of redirects to follow

    fun fetch(url: String, context: Context, redirectCount: Int = 0): PageData {
        if (redirectCount >= MAX_REDIRECTS) {
            return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Too many redirects",
                statusMessage = "Redirect Loop"
            )
        }

        val parsedUrl = UrlUtils.parse(url)
            ?: return PageData(
                statusCode = -1,
                meta = "",
                content = null,
                errorMessage = "Error: Invalid URL. Please check the format.",
                statusMessage = "Invalid URL"
            )

        val host = parsedUrl.host
        val fullUrl = parsedUrl.fullUrl

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

            writer.write("$fullUrl\r\n")
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
                11 -> PageData(
                    statusCode = statusCode,
                    meta = meta,
                    content = null,
                    errorMessage = null,
                    statusMessage = "Sensitive Input Required: $meta"
                )
                in 10..19 -> PageData(
                    statusCode = statusCode,
                    meta = meta,
                    content = null,
                    errorMessage = null,
                    statusMessage = "Input Required: $meta"
                )
                in 20..29 -> {
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
                in 30..39 -> {
                    val targetMeta = meta.trim()

                    val redirectUrl = try {
                        val parsedRedirect = when {
                            targetMeta.isEmpty() -> fullUrl // empty meta = reload same page
                            targetMeta.startsWith("/") -> {
                                // normalize path, remove duplicate slashes
                                val cleanPath = targetMeta.replace(Regex("/{2,}"), "/")
                                val base = URL(fullUrl)
                                "gemini://${base.host}$cleanPath"
                            }
                            !targetMeta.contains("://") -> {
                                // no scheme provided, assume gemini://
                                "gemini://${targetMeta}"
                            }
                            else -> {
                                // absolute URL
                                targetMeta
                            }
                        }

                        // ensure no trailing slashes if needed (optional)
                        parsedRedirect.trimEnd('/')

                    } catch (e: Exception) {
                        return PageData(
                            statusCode = -1,
                            meta = meta,
                            content = null,
                            errorMessage = "Error: Invalid redirect URL",
                            statusMessage = "Invalid Redirect"
                        )
                    }

                    val normalizedRedirectUrl = redirectUrl.trimEnd('/')
                    val normalizedCurrentUrl = fullUrl.trimEnd('/')

                    if (normalizedRedirectUrl == normalizedCurrentUrl && redirectCount > 0) {
                        // only error if we are already in a redirect cycle
                        return PageData(
                            statusCode = -1,
                            meta = meta,
                            content = null,
                            errorMessage = "Error: Redirect to same URL repeatedly.",
                            statusMessage = "Redirect Loop"
                        )
                    }

                    return fetch(redirectUrl, context, redirectCount + 1)
                }

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
            PageData(
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

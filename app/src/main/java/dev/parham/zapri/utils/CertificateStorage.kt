package dev.parham.zapri.utils

import android.content.Context
import java.io.File

class CertificateStorage(private val context: Context) {

    private val storageFile: File
        get() = File(context.filesDir, "trusted_certificates.txt")

    // Save a fingerprint for a host
    fun saveFingerprint(host: String, fingerprint: String) {
        val entry = "$host:$fingerprint\n"
        storageFile.appendText(entry)
    }

    // Get the fingerprint for a host
    fun getFingerprint(host: String): String? {
        if (!storageFile.exists()) return null
        return storageFile.readLines()
            .firstOrNull { it.startsWith("$host:") }
            ?.substringAfter(":")
    }
}


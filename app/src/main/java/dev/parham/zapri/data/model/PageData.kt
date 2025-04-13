package dev.parham.zapri.data.model

data class PageData(
    val statusCode: Int,       // The status code returned by the server (e.g., 20 for success)
    val meta: String,          // Metadata or additional information (e.g., content type, redirect URL)
    val content: String?,      // The actual content of the page (null if not applicable)
    val errorMessage: String?, // Error message if the page couldn't be fetched (null if no error)
    val statusMessage: String? // Human-readable status message (e.g., "Success", "Redirect", "Not Found")
)


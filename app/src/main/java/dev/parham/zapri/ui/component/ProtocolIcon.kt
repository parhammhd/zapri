package dev.parham.zapri.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProtocolIcon(protocol: String, modifier: Modifier = Modifier) {
    val icon = when (protocol) {
        "gemini" -> Icons.Default.Language // Example icon for Gemini
        "gopher" -> Icons.Default.Explore // Example icon for Gopher
        "finger" -> Icons.Default.Person // Example icon for Finger
        "scroll" -> Icons.Default.Description // Example icon for Scroll
        "nex" -> Icons.Default.Subway // Example icon for Nex
        "spartan" -> Icons.Default.Shield // Example icon for Spartan
        "text" -> Icons.AutoMirrored.Filled.TextSnippet // Example icon for Text
        else -> Icons.AutoMirrored.Filled.Help // Default icon for unknown protocols
    }

    Icon(
        imageVector = icon,
        contentDescription = "$protocol protocol icon",
        modifier = modifier
    )
}


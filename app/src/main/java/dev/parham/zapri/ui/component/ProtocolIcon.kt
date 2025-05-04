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
        "http", "https" -> Icons.Default.Public // Globe icon for web
        "gemini" -> Icons.Default.RocketLaunch // Rocket for Gemini
        "gopher" -> Icons.Default.Explore // Compass icon for Gopher
        "finger" -> Icons.Default.Person // Person icon for Finger
        "scroll" -> Icons.Default.Description // Description icon for Scroll
        "nex" -> Icons.Default.Subway // Subway icon for Nex
        "spartan" -> Icons.Default.Shield // Shield icon for Spartan
        "text" -> Icons.AutoMirrored.Filled.TextSnippet // Text snippet icon for Text
        "local" -> Icons.Default.Folder // Folder icon for local files
        "browser" -> Icons.Default.Window // Window icon for browser internal pages
        else -> Icons.AutoMirrored.Filled.Help // Default icon for unknown protocols
    }

    Icon(
        imageVector = icon,
        contentDescription = "$protocol protocol icon",
        modifier = modifier
    )
}


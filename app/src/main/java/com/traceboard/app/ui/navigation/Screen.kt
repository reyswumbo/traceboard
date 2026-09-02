package com.traceboard.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dasbor", Icons.Filled.Dashboard)
    data object Clipboard : Screen("clipboard", "Clipboard", Icons.Filled.ContentPaste)
    data object Writing : Screen("writing", "Menulis", Icons.Filled.Edit)
    data object Usage : Screen("usage", "Penggunaan", Icons.Filled.Insights)
}
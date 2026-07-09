package com.nhockool1002.costoftrips.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nhockool1002.costoftrips.R

private val topCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun AppBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(
        modifier = Modifier
            .shadow(elevation = 16.dp, shape = topCornerShape)
            .clip(topCornerShape),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        BottomBarItem(
            emoji = "🧳",
            label = stringResource(R.string.nav_trips),
            selected = currentRoute == Screen.TripList.route,
            onClick = {
                if (currentRoute != Screen.TripList.route) {
                    navController.navigate(Screen.TripList.route) {
                        popUpTo(Screen.TripList.route) { inclusive = true }
                    }
                }
            }
        )
        BottomBarItem(
            emoji = "📊",
            label = stringResource(R.string.nav_statistics),
            selected = currentRoute == Screen.Statistics.route,
            onClick = {
                if (currentRoute != Screen.Statistics.route) {
                    navController.navigate(Screen.Statistics.route) {
                        popUpTo(Screen.TripList.route)
                    }
                }
            }
        )
    }
}

@Composable
private fun RowScope.BottomBarItem(emoji: String, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Text(emoji, fontSize = 22.sp) },
        label = {
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

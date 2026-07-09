package com.nhockool1002.costoftrips.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.ui.theme.GradientEnd
import com.nhockool1002.costoftrips.ui.theme.GradientStart

@Composable
fun CategoryIcon(category: ExpenseCategory, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(category.badgeColor(), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(category.emoji(), fontSize = (size.value * 0.45f).sp)
    }
}

@Composable
fun GradientStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                RoundedCornerShape(28.dp)
            )
            .padding(28.dp)
    ) {
        Text(
            title,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            value,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 34.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (subtitle != null) {
            Text(
                subtitle,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

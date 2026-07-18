package com.nhockool1002.costoftrips.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.ui.theme.GradientEnd
import com.nhockool1002.costoftrips.ui.theme.GradientStart
import com.nhockool1002.costoftrips.util.CurrencyFormatter
import com.nhockool1002.costoftrips.util.LocalCurrency
import com.nhockool1002.costoftrips.util.TripStatus

@Composable
fun CategoryIcon(category: ExpenseCategory, modifier: Modifier = Modifier, size: Dp = 52.dp) {
    EmojiBadge(emoji = category.emoji(), containerColor = category.badgeColor(), modifier = modifier, size = size)
}

@Composable
fun EmojiBadge(
    emoji: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.45f).sp)
    }
}

/**
 * A friendlier OutlinedTextField with a colored emoji badge as the leading icon.
 * When [onClick] is provided the field becomes tap-to-trigger (e.g. opening a date
 * picker) instead of directly editable — a plain `readOnly` field would still steal
 * the tap for its own focus/cursor handling, so it's disabled and the badge/border
 * colors are pinned to look identical to an enabled field.
 */
@Composable
fun CuteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    emoji: String,
    modifier: Modifier = Modifier,
    emojiContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    minLines: Int = 1,
    suffix: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(20.dp)
    val badge: @Composable () -> Unit = {
        EmojiBadge(emoji = emoji, containerColor = emojiContainerColor, size = 36.dp)
    }
    if (onClick != null) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            leadingIcon = badge,
            suffix = suffix,
            isError = isError,
            supportingText = supportingText,
            enabled = false,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = modifier.clickable(onClick = onClick)
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = badge,
            suffix = suffix,
            isError = isError,
            supportingText = supportingText,
            minLines = minLines,
            shape = shape,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            textStyle = textStyle ?: MaterialTheme.typography.bodyLarge,
            modifier = modifier
        )
    }
}

/** Small colored pill showing whether a trip is upcoming, ongoing, or already over. */
@Composable
fun TripStatusBadge(status: TripStatus, modifier: Modifier = Modifier) {
    val label: String
    val containerColor: Color
    val contentColor: Color
    when (status) {
        TripStatus.UPCOMING -> {
            label = stringResource(R.string.trip_status_upcoming)
            containerColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        }
        TripStatus.ONGOING -> {
            label = stringResource(R.string.trip_status_ongoing)
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        }
        TripStatus.COMPLETED -> {
            label = stringResource(R.string.trip_status_completed)
            containerColor = MaterialTheme.colorScheme.secondaryContainer
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        }
    }
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}

@Composable
fun GradientStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                RoundedCornerShape(if (compact) 20.dp else 24.dp)
            )
            .padding(if (compact) 14.dp else 20.dp)
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
            fontSize = if (compact) 26.sp else 34.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        if (subtitle != null) {
            Text(
                subtitle,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** A titled progress bar card that turns error-colored once [current] exceeds [limit]. */
@Composable
fun ProgressCard(
    title: String,
    current: Double,
    limit: Double,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val currency = LocalCurrency.current
    val remaining = limit - current
    val isOver = remaining < 0
    val containerColor = if (isOver) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
    val contentColor = if (isOver) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
    val progress = if (limit > 0) (current / limit).toFloat().coerceIn(0f, 1f) else 1f

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                trailingIcon?.invoke(contentColor)
            }
            LinearProgressIndicator(
                progress = { progress },
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = if (isOver) {
                    stringResource(R.string.trip_detail_budget_over, CurrencyFormatter.format(-remaining, currency))
                } else {
                    stringResource(R.string.trip_detail_budget_remaining, CurrencyFormatter.format(remaining, currency))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

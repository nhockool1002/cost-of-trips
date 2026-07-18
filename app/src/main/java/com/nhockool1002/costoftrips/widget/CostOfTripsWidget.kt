package com.nhockool1002.costoftrips.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nhockool1002.costoftrips.CostOfTripsApp
import com.nhockool1002.costoftrips.MainActivity
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.ui.theme.GradientStart
import com.nhockool1002.costoftrips.util.CurrencyFormatter
import com.nhockool1002.costoftrips.util.TripStatus
import com.nhockool1002.costoftrips.util.tripStatus
import kotlinx.coroutines.flow.first

class CostOfTripsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as CostOfTripsApp
        val trips = app.tripRepository.observeTrips().first()
        val expenses = app.tripRepository.observeAllExpenses().first()
        val currency = app.userPreferencesRepository.currency.first()

        val ongoing = trips.firstOrNull { tripStatus(it.startDate, it.endDate) == TripStatus.ONGOING }
        val byTrip = expenses.groupBy { it.tripId }
        val label = ongoing?.name ?: context.getString(R.string.widget_total_all_trips)
        val total = ongoing?.let { byTrip[it.id].orEmpty().sumOf { expense -> expense.amount } }
            ?: expenses.sumOf { it.amount }
        val formattedTotal = CurrencyFormatter.format(total, currency)

        provideContent {
            WidgetContent(label = label, total = formattedTotal)
        }
    }
}

@Composable
private fun WidgetContent(label: String, total: String) {
    val context = LocalContext.current
    val white = ColorProvider(day = Color.White, night = Color.White)
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(day = GradientStart, night = GradientStart))
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = label,
                maxLines = 1,
                style = TextStyle(color = white, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = total,
            style = TextStyle(color = white, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        )
    }
}

class CostOfTripsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CostOfTripsWidget()
}

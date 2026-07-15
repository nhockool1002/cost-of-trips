package com.nhockool1002.costoftrips.ui.screens.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import com.nhockool1002.costoftrips.data.preferences.AppLanguage
import com.nhockool1002.costoftrips.data.preferences.ThemeMode
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onAboutClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = appViewModelFactory(context))
    val themeMode by viewModel.themeMode.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderIntervalHours by viewModel.reminderIntervalHours.collectAsState()
    val scope = rememberCoroutineScope()

    val permissionDeniedMessage = stringResource(R.string.settings_reminders_permission_denied)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
        } else {
            Toast.makeText(context, permissionDeniedMessage, Toast.LENGTH_LONG).show()
        }
    }

    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLanguage: AppLanguage? = if (currentLocales.isEmpty) {
        null
    } else {
        AppLanguage.fromCode(currentLocales[0]?.language)
    }

    val importSuccessTemplate = stringResource(R.string.settings_import_success)
    val importFailureMessage = stringResource(R.string.settings_import_failure)
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            val result = json?.let { viewModel.importData(it) }
            val message = result?.getOrNull()?.let { count -> String.format(importSuccessTemplate, count) }
                ?: importFailureMessage
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(icon = "🌐", title = stringResource(R.string.settings_language_label)) {
                LanguageOptionRow(
                    label = stringResource(R.string.settings_language_system),
                    selected = currentLanguage == null,
                    onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()) }
                )
                LanguageOptionRow(
                    label = "Tiếng Việt",
                    selected = currentLanguage == AppLanguage.VIETNAMESE,
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(AppLanguage.VIETNAMESE.code)
                        )
                    }
                )
                LanguageOptionRow(
                    label = "English",
                    selected = currentLanguage == AppLanguage.ENGLISH,
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(AppLanguage.ENGLISH.code)
                        )
                    }
                )
            }

            SettingsSection(icon = "🎨", title = stringResource(R.string.settings_theme_label)) {
                val options = listOf(
                    ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                    ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                    ThemeMode.DARK to stringResource(R.string.settings_theme_dark)
                )
                options.forEach { (mode, label) ->
                    LanguageOptionRow(
                        label = label,
                        selected = mode == themeMode,
                        onClick = { viewModel.setThemeMode(mode) }
                    )
                }
            }

            SettingsSection(icon = "💱", title = stringResource(R.string.settings_currency_label)) {
                CurrencyDropdown(selected = currency, onSelect = viewModel::setCurrency)
            }

            SettingsSection(icon = "🔔", title = stringResource(R.string.settings_reminders_label)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.settings_reminders_enable),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                                    PackageManager.PERMISSION_GRANTED
                                if (needsPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setReminderEnabled(true)
                                }
                            } else {
                                viewModel.setReminderEnabled(false)
                            }
                        }
                    )
                }
                if (reminderEnabled) {
                    Text(
                        stringResource(R.string.settings_reminders_interval_label),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(listOf(3, 6, 12, 24)) { hours ->
                            FilterChip(
                                selected = reminderIntervalHours == hours,
                                onClick = { viewModel.setReminderIntervalHours(hours) },
                                label = { Text(stringResource(R.string.settings_reminders_interval_hours, hours)) }
                            )
                        }
                    }
                }
            }

            SettingsSection(icon = "📤", title = stringResource(R.string.settings_data_label)) {
                Button(
                    onClick = {
                        scope.launch {
                            val json = viewModel.exportData()
                            val uri = com.nhockool1002.costoftrips.data.export.DataExporter.exportToDownloads(context, json)
                            val messageRes = if (uri != null) R.string.settings_export_success else R.string.settings_export_failure
                            Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_export_data))
                }
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.settings_import_data))
                }
            }

            SettingsSection(icon = "🛟", title = stringResource(R.string.settings_support_label)) {
                val bodyTemplate = stringResource(R.string.bug_report_body_template)
                val noEmailAppMessage = stringResource(R.string.bug_report_no_email_app)
                Button(
                    onClick = { sendBugReportEmail(context, bodyTemplate, noEmailAppMessage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_report_bug))
                }
            }

            AboutRow(onClick = onAboutClick)
        }
    }
}

private fun sendBugReportEmail(context: Context, bodyTemplate: String, noEmailAppMessage: String) {
    val errorId = (1..6)
        .map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }
        .joinToString("")
    val recipient = "nhut.nguyenminh.it@gmail.com"
    val subject = "[#COT-$errorId] Error Report"

    val sendToIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, bodyTemplate)
    }
    val gmailAppIntent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        setPackage("com.google.android.gm")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, bodyTemplate)
    }
    val gmailWebIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(
            "https://mail.google.com/mail/?view=cm&fs=1" +
                "&to=${Uri.encode(recipient)}" +
                "&su=${Uri.encode(subject)}" +
                "&body=${Uri.encode(bodyTemplate)}"
        )
    }

    for (intent in listOf(sendToIntent, gmailAppIntent, gmailWebIntent)) {
        try {
            context.startActivity(intent)
            return
        } catch (e: ActivityNotFoundException) {
            // Try the next fallback.
        }
    }
    Toast.makeText(context, noEmailAppMessage, Toast.LENGTH_LONG).show()
}

@Composable
private fun SettingsSection(icon: String, title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, modifier = Modifier.padding(end = 10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(selected: AppCurrency, onSelect: (AppCurrency) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "${selected.code} (${selected.symbol})",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppCurrency.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${option.code} (${option.symbol})") },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun AboutRow(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("ℹ️", modifier = Modifier.padding(end = 12.dp))
            Text(
                stringResource(R.string.settings_about_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

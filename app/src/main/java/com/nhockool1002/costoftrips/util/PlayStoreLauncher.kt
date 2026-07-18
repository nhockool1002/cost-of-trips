package com.nhockool1002.costoftrips.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openPlayStoreListing(context: Context) {
    val packageName = context.packageName
    // Starting an activity from a non-Activity context (e.g. an Application context) requires
    // FLAG_ACTIVITY_NEW_TASK or Android throws AndroidRuntimeException; adding it unconditionally
    // is harmless when called from an Activity context too.
    fun intent(uri: String) = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent("market://details?id=$packageName"))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(intent("https://play.google.com/store/apps/details?id=$packageName"))
    }
}

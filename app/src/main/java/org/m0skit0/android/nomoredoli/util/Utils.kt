package org.m0skit0.android.nomoredoli.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.StringRes
import android.widget.Toast

internal fun Activity.toast(@StringRes id: Int?) {
    Toast.makeText(this, id!!, Toast.LENGTH_SHORT).show()
}

internal fun Activity.toast(message: String?) {
    Toast.makeText(this, message ?: "<Empty message>", Toast.LENGTH_SHORT).show()
}

internal fun <T: Activity> Context.launchActivityClearTopNewTask(activityClass: Class<T>) {
    Intent(this, activityClass).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    }.run { startActivity(this) }
}

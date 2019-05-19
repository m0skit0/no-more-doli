package org.m0skit0.android.nomoredoli.util

import android.app.Activity
import android.support.annotation.StringRes
import android.widget.Toast

internal fun Activity.toast(@StringRes id: Int?) {
    Toast.makeText(this, id!!, Toast.LENGTH_SHORT).show()
}

internal fun Activity.toast(message: String?) {
    Toast.makeText(this, message ?: "<Empty message>", Toast.LENGTH_SHORT).show()
}

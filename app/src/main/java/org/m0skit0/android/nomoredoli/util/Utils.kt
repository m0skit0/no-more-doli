package org.m0skit0.android.nomoredoli.util

import android.app.Activity
import android.support.annotation.StringRes
import android.util.Log
import android.widget.Toast

internal fun Activity.toast(@StringRes id: Int?) {
    Toast.makeText(this, id!!, Toast.LENGTH_SHORT).show()
}

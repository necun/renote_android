package com.renote.renoteai.utils

import android.content.Context
import android.widget.Toast

object CommonUtils {


    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
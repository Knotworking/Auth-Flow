package com.knotworking.authexample.core.logging

import android.util.Log
import com.knotworking.authexample.core.Logger

class AndroidLogger : Logger {
    override fun d(tag: String, message: String) = Log.d(tag, message).let {}
    override fun i(tag: String, message: String) = Log.i(tag, message).let {}
    override fun w(tag: String, message: String) = Log.w(tag, message).let {}
    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
    }
}

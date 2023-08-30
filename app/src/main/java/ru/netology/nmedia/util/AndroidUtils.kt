package ru.netology.nmedia.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("SimpleDateFormat")
    fun dateFormatter(time: Long?): String{
        val formatter = SimpleDateFormat("y-MM-d'T'hh:mm", Locale.ENGLISH)
        return formatter.format(time)
    }

    fun dateShortFormatter(time: Long?): String{
        val formatter = SimpleDateFormat("y-MM-d", Locale.ENGLISH)
        return formatter.format(time)
    }

    fun dateFormatter(date: String): Long{
        val formatter = SimpleDateFormat("y-MM-d'T'hh:mm", Locale.ENGLISH)
        return formatter.parse(date)?.time ?: 0
    }

    @SuppressLint("SimpleDateFormat")
    fun dateFormatter1(date: String): String{
        val parser =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val formatter = SimpleDateFormat("dd.MM.yyyy")
        return parser.parse(date).let { formatter.format(it) }
    }
}
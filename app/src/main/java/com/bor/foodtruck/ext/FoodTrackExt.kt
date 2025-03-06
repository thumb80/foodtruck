package com.bor.foodtruck.ext

import android.content.res.Resources
import android.os.SystemClock
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun List<String>.isInItemsList(value: String): Boolean {
    this.forEach {
        if (it.contains(value.getOrderItemName()))
            return true
    }
    return false
}

fun List<String>.isItemZutaten(value: String): Boolean {
    this.forEach {
        if (value.getOrderItemName().contains(it))
            return true
    }
    return false
}

fun Date.isPreviousMonth(): Boolean {
    val sdf = SimpleDateFormat("MM", Locale.getDefault())
    return sdf.format(this).toInt() < sdf.format(Date(System.currentTimeMillis())).toInt()
}

fun String.getOrderItemName(): String {
    val regex = Regex("^(.*?)\\sx[0-9]")
    val matchResult = regex.find(this)

    return if (matchResult != null) {
        // Extract part before the matched regex
        matchResult.groups[1]?.value ?: ""
    } else {
        // If no match is found, return the whole string
        this
    }
}

fun String.getItemName(): String {
    val regex = Regex("^(.*?)\\sx[0-9]")
    val matchResult = regex.find(this)

    return if (matchResult != null) {
        // Extract part before the matched regex
        matchResult.groups[1]?.value ?: ""
    } else {
        // If no match is found, return the whole string
        this
    }
}

fun String.getPartBeforeRegex(): String {
    val regex = Regex("^.*?(?<=x[0-9])")
    val matchResult = regex.find(this)

    return if (matchResult != null) {
        // Extract part before the matched regex
        matchResult.groups[0]?.value ?: ""
    } else {
        // If no match is found, return the whole string
        this
    }
}

fun String.removeWhiteSpaces(): String {
    val regex = Regex("[0-9]\\s\\s[0-9]")
    val matchResult = regex.find(this)

    return if (matchResult != null) {
        // Extract part before the matched regex
        this.replace(
            matchResult.groups[0]?.value ?: "",
            matchResult.groups[0]?.value?.replace("\\s\\s".toRegex(), " ") ?: ""
        )
    } else {
        // If no match is found, return the whole string
        this
    }
}

class SafeClickListener(
    private var defaultInterval: Int = 100,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}
package com.example.mytvapp.extension

import android.content.Context
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

@Suppress("MemberVisibilityCanBePrivate")
object Util {

    const val PATTER_HTTP_URL =
        "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,4}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)\$"

    const val PATTERN_MAGNET_URL = "^magnet:\\?xt=urn:[a-z0-9]+:[a-z0-9]{32,40}&dn=.+&tr=.+\$"


    //#region String extensions

    fun String.isHttpUrl(): Boolean =
        Regex(PATTER_HTTP_URL).matches(this)


    fun String.isMagnetUrl(): Boolean =
        Regex(PATTERN_MAGNET_URL, RegexOption.IGNORE_CASE).matches(this)

    //endregion

    //#region String extensions

    fun Int.fromDpToPx(context: Context): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (this.toFloat() * density).roundToInt()
    }

    //endregion


    //#region Others

    fun genRandomGuid(): String = UUID.randomUUID().toString()

    fun genRandomId(): Long = Random.nextLong(until = 999999999L)

    //endregion
}
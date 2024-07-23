package com.programminghut.realtime_object

fun Float.format(digits: Int): String {
    return "%.${digits}f".format(this)
}

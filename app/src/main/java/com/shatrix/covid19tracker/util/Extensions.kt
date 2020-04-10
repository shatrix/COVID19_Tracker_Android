package com.shatrix.covid19tracker.util

import android.util.Log
import com.shatrix.covid19tracker.datalayer.model.CountryItem


fun String.toInt(): Int {
    "data $this^^".log()
    val newSt = this.split("\n")[0]
    "newSt ,$newSt,".log()
    return Integer.parseInt(newSt.replace(",", "").replace(",", ""))
}

fun List<CountryItem>.sortByCases(): List<CountryItem> {
    return this.sortedByDescending { it.casesInt }
}

fun String.log() {
    Log.d("TestApp", this)
}


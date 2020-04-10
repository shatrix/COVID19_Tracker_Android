package com.shatrix.covid19tracker.util

import com.shatrix.covid19tracker.datalayer.model.CountryItem


fun String.toInt(): Int {
    return Integer.parseInt(this.replace(",", "").replace(",", ""))
}

fun List<CountryItem>.sortByCases(): List<CountryItem> {
    return this.sortedByDescending { it.casesInt }
}

fun List<CountryItem>.sortByNewCases(): List<CountryItem> {
    return this.sortedByDescending { it.newCasesInt }
}

fun List<CountryItem>.sortByRecover(): List<CountryItem> {
    return this.sortedByDescending { it.recoveredInt }
}

fun List<CountryItem>.sortByDeaths(): List<CountryItem> {
    return this.sortedByDescending { it.deathsInt }
}

fun List<CountryItem>.sortByNewDeaths(): List<CountryItem> {
    return this.sortedByDescending { it.newDeathsInt }
}






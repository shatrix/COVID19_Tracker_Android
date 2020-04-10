package com.shatrix.covid19tracker.datalayer.model

import com.shatrix.covid19tracker.util.toInt

data class CountryItem(val countryName: String, var cases: String, var recovered: String, var deaths: String, var newCases: String, var newDeaths: String) {
    val casesInt = cases.toInt()
    val recoveredInt = recovered.toInt()
    val deathsInt = deaths.toInt()
    val newCasesInt = newCases.toInt()
    val newDeathsInt = newDeaths.toInt()

    val recoveredIntPercentage = (recoveredInt / casesInt.toDouble()) * 100.0
    val deathsIntPercentage = (deathsInt / casesInt.toDouble()) * 100.0


}



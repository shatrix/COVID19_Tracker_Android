package com.shatrix.covid19tracker.util

import com.shatrix.covid19tracker.datalayer.model.CountryItem


fun String.toInt(): Int {
    return Integer.parseInt(this.replace(",", "").replace(",", ""))
}

fun List<CountryItem>.sortByCases(desecending: Boolean = true): List<CountryItem> {
    return if (desecending) this.sortedByDescending { it.casesInt } else this.sortedBy { it.casesInt }
}

fun List<CountryItem>.sortByNewCases(desecending: Boolean = true): List<CountryItem> {
    return if (desecending) this.sortedByDescending { it.newCasesInt } else this.sortedBy { it.newCasesInt }
}

fun List<CountryItem>.sortByRecover(desecending: Boolean = true): List<CountryItem> {
    return if (desecending) this.sortedByDescending { it.recoveredInt } else this.sortedBy { it.recoveredInt }
}

fun List<CountryItem>.sortByDeaths(desecending: Boolean = true): List<CountryItem> {
    return if (desecending) this.sortedByDescending { it.deathsInt } else this.sortedBy { it.deathsInt }
}

fun List<CountryItem>.sortByNewDeaths(desecending: Boolean = true): List<CountryItem> {
    return if (desecending) this.sortedByDescending { it.newDeathsInt } else this.sortedBy { it.newDeathsInt }
}




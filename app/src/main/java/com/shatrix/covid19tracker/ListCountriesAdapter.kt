package com.shatrix.covid19tracker

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.shatrix.covid19tracker.datalayer.model.CountryItem

class ListCountriesAdapter(var context: Activity, var allCountriesResults: List<CountryItem>) : BaseAdapter() {
    var inflater: LayoutInflater
    override fun getCount(): Int {
        return allCountriesResults.size
    }

    override fun getItem(position: Int): Any {
        return allCountriesResults[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private inner class ViewHolder {
        var colCountryName: TextView? = null
        var colCases: TextView? = null
        var colNewCases: TextView? = null
        var colRecovered: TextView? = null
        var colDeaths: TextView? = null
        var colNewDeaths: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.countries_list_adapter, null)
            holder = ViewHolder()
            holder.colCountryName = view.findViewById<View>(R.id.colCountryName) as TextView
            holder.colCases = view.findViewById<View>(R.id.colCases) as TextView
            holder.colRecovered = view.findViewById<View>(R.id.colRecovered) as TextView
            holder.colDeaths = view.findViewById<View>(R.id.colDeaths) as TextView
            holder.colNewCases = view.findViewById<View>(R.id.colNewCases) as TextView
            holder.colNewDeaths = view.findViewById<View>(R.id.colNewDeaths) as TextView
            view.tag = holder
        } else {
            holder = view?.tag as ViewHolder
        }
        val item = allCountriesResults[position]
        holder.colCountryName!!.text = item.countryName
        holder.colCases!!.text = item.cases
        holder.colNewCases!!.text = item.newCases
        holder.colRecovered!!.text = String.format("%s\n%4.2f%s", item.recovered, item.recoveredIntPercentage, "%")
        holder.colDeaths!!.text = String.format("%s\n%4.2f%s", item.deaths, item.deathsIntPercentage, "%")
        holder.colNewDeaths!!.text = item.newDeaths
        return view!!
    }

    init {
        inflater = context.layoutInflater
    }
}
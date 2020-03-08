package com.shatrix.coronatracker;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;

public class ListCountriesAdapter extends BaseAdapter {

    Activity context;
    ArrayList<CountryLine> allCountriesResults;
    LayoutInflater inflater;

    public ListCountriesAdapter(Activity context, ArrayList<CountryLine> allCountriesResults) {
        super();
        this.context = context;
        this.allCountriesResults = allCountriesResults;
        inflater =  context.getLayoutInflater();
    }


    @Override
    public int getCount() {
        return allCountriesResults.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView colCountryName;
        TextView colCases;
        TextView colRecovered;
        TextView colDeaths;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.countries_list_adapter, null);
            holder = new ViewHolder();
            holder.colCountryName = (TextView) convertView.findViewById(R.id.colCountryName);
            holder.colCases = (TextView) convertView.findViewById(R.id.colCases);
            holder.colRecovered = (TextView) convertView.findViewById(R.id.colRecovered);
            holder.colDeaths = (TextView) convertView.findViewById(R.id.colDeaths);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.colCountryName.setText(allCountriesResults.get(position).countryName);
        holder.colCases.setText(allCountriesResults.get(position).cases);
        holder.colRecovered.setText(allCountriesResults.get(position).recovered);
        holder.colDeaths.setText(allCountriesResults.get(position).deaths);

        return convertView;
    }


}

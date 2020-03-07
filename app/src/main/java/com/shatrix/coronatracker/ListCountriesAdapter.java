package com.shatrix.coronatracker;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListCountriesAdapter extends BaseAdapter {

    Activity context;
    String countriesNames[];
    String numberCases[];
    String numberRecovered[];
    String numberDeaths[];

    public ListCountriesAdapter(Activity context, String[] countriesNames, String[] numberCases, String[] numberRecovered, String[] numberDeaths) {
        super();
        this.context = context;
        this.countriesNames = countriesNames;
        this.numberCases = numberCases;
        this.numberRecovered = numberRecovered;
        this.numberDeaths = numberDeaths;
    }

    @Override
    public int getCount() {
        return countriesNames.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
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
        LayoutInflater inflater =  context.getLayoutInflater();

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

        holder.colCountryName.setText(countriesNames[position]);
        holder.colCases.setText(numberCases[position]);
        holder.colRecovered.setText(numberRecovered[position]);
        holder.colDeaths.setText(numberDeaths[position]);

        return convertView;
    }


}

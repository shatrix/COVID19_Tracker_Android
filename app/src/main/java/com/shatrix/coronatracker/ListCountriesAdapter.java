package com.shatrix.coronatracker;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListCountriesAdapter extends BaseAdapter implements Filterable {

    Activity context;
    ArrayList<CountryLine> allCountriesResults;
    ArrayList<CountryLine> mDisplayedValues;
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

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                mDisplayedValues = (ArrayList<CountryLine>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<CountryLine> FilteredArrList = new ArrayList<CountryLine>();

                if (allCountriesResults == null) {
                    allCountriesResults = new ArrayList<CountryLine>(mDisplayedValues); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = allCountriesResults.size();
                    results.values = allCountriesResults;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < allCountriesResults.size(); i++) {
                        String data = allCountriesResults.get(i).countryName;
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(new CountryLine(
                                    allCountriesResults.get(i).countryName,
                                    allCountriesResults.get(i).cases,
                                    allCountriesResults.get(i).recovered,
                                    allCountriesResults.get(i).deaths));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
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

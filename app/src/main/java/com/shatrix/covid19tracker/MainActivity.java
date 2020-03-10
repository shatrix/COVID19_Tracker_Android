package com.shatrix.covid19tracker;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle, textViewRecoveredTitle ;
    EditText textSearchBox;
    Handler handler;
    String url = "https://www.worldometers.info/coronavirus/";
    String tmpCountry, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage;
    Document doc;
    Element countriesTable, row;
    Elements countriesRows, cols;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Calendar myCalender;
    SimpleDateFormat myFormat;
    double tmpNumber;
    DecimalFormat generalDecimalFormat;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    ListView listViewCountries;
    ListCountriesAdapter listCountriesAdapter;
    ArrayList<CountryLine> allCountriesResults, FilteredArrList;
    Intent sharingIntent;
    int colNumCountry, colNumCases, colNumRecovered, colNumDeaths;
    SwipeRefreshLayout mySwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // All initial definitions
        textViewCases = (TextView)findViewById(R.id.textViewCases);
        textViewRecovered = (TextView)findViewById(R.id.textViewRecovered);
        textViewDeaths = (TextView)findViewById(R.id.textViewDeaths);
        textViewDate = (TextView)findViewById(R.id.textViewDate);
        textViewRecoveredTitle = (TextView)findViewById(R.id.textViewRecoveredTitle);
        textViewDeathsTitle = (TextView)findViewById(R.id.textViewDeathsTitle);
        listViewCountries = (ListView)findViewById(R.id.listViewCountries);
        textSearchBox = (EditText)findViewById(R.id.textSearchBox);
        colNumCountry = 0; colNumCases = 1; colNumRecovered = 0; colNumDeaths = 0;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        myCalender = Calendar.getInstance();
        handler = new Handler() ;
        generalDecimalFormat = new DecimalFormat("0.00", symbols);
        allCountriesResults = new ArrayList<CountryLine>();

        // Implement Swipe to Refresh
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.coronaMainSwipeRefresh);
        mySwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshData();
                    }
                }
        );

        // fix interference between scrolling in listView & parent SwipeRefreshLayout
        listViewCountries.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        if (!listIsAtTop()) mySwipeRefreshLayout.setEnabled(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mySwipeRefreshLayout.setEnabled(true);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
            private boolean listIsAtTop()   {
                if(listViewCountries.getChildCount() == 0) return true;
                return listViewCountries.getChildAt(0).getTop() == 0;
            }
        });

        // fetch previously saved data in SharedPreferences, if any
        if(preferences.getString("textViewCases", null) != null ){
            textViewCases.setText(preferences.getString("textViewCases", null));
            textViewRecovered.setText(preferences.getString("textViewRecovered", null));
            textViewDeaths.setText(preferences.getString("textViewDeaths", null));
            textViewDate.setText(preferences.getString("textViewDate", null));
            calculate_percentages();
        }

        // Add Text Change Listener to textSearchBox to filter by Country
        textSearchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence searchSequense, int start, int before, int count) {
                ArrayList<CountryLine> FilteredArrList = new ArrayList<CountryLine>();
                if (searchSequense == null || searchSequense.length() == 0) {
                    // back to original
                    setListViewCountries(allCountriesResults);
                } else {
                    searchSequense = searchSequense.toString().toLowerCase();
                    for (int i = 0; i < allCountriesResults.size(); i++) {
                        String data = allCountriesResults.get(i).countryName;
                        if (data.toLowerCase().startsWith(searchSequense.toString())) {
                            FilteredArrList.add(new CountryLine(
                                    allCountriesResults.get(i).countryName,
                                    allCountriesResults.get(i).cases,
                                    allCountriesResults.get(i).recovered,
                                    allCountriesResults.get(i).deaths));
                        }
                    }
                    // set the Filtered result to return
                    setListViewCountries(FilteredArrList);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        textSearchBox.clearFocus();
        textSearchBox.setFocusableInTouchMode(true);
	// Call refreshData once the app is opened only one time, then user can request updates
	refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        textSearchBox.clearFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    void setListViewCountries(ArrayList<CountryLine> allCountriesResults) {
        listCountriesAdapter = new ListCountriesAdapter(this, allCountriesResults);
        listViewCountries.setAdapter(listCountriesAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                new AlertDialog.Builder(this)
                        .setTitle("COVID-19 Tracker")
                        .setCancelable(true)
                        .setMessage("Source:\nhttps://www.worldometers.info/coronavirus\n" +
                                "\n\n" +
                                "Developer: Sherif Mousa (Shatrix)" +
                                "\n" +
                                "\n" +
                                "GitHub,LinkedIn,Facebook,Twitter @shatrix")
                        .setPositiveButton("Close", null)
                        .setIcon(R.drawable.ic_info)
                        .show();
                return true;
            case R.id.action_refresh:
                refreshData();
                return true;
            case R.id.action_share:
                sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Install (COVID-19 Tracker) Android Application to get the latest global updates for Coronavirus Outbreak \n\nhttps://tinyurl.com/tsvjowr";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "COVID-19 Tracker");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share COVID-19 Tracker Link"));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    void calculate_percentages () {
        tmpNumber = Double.parseDouble(textViewRecovered.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100;
        textViewRecoveredTitle.setText("Total Recovered   " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewDeaths.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100 ;
        textViewDeathsTitle.setText("Total Deaths   " + generalDecimalFormat.format(tmpNumber) + "%");
    }

    void refreshData() {
        mySwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    doc = Jsoup.connect(url).timeout(10000).get();
                    // table id main_table_countries
                    countriesTable = doc.getElementById("main_table_countries");
                    countriesRows = countriesTable.select("tr");
                    //Log.e("TITLE", elementCases.text());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // get countries
                            Iterator<Element> rowIterator = countriesRows.iterator();
                            allCountriesResults = new ArrayList<CountryLine>();

                            // read table header and find correct column number for each category
                            row = rowIterator.next();
                            cols = row.select("th");
                            //Log.e("COLS: ", cols.text());
                            if (cols.get(0).text().contains("Country")) {
                                for(int i=1; i < cols.size(); i++){
                                    if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Cases"))
                                        {colNumCases = i; Log.e("Cases: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Recovered"))
                                        {colNumRecovered = i; Log.e("Recovered: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Deaths"))
                                        {colNumDeaths = i; Log.e("Deaths: ", cols.get(i).text());}
                                }
                            }

                            while (rowIterator.hasNext()) {
                                row = rowIterator.next();
                                cols = row.select("td");

                                if (cols.get(0).text().contains("Total")) {
                                    textViewCases.setText(cols.get(colNumCases).text());
                                    textViewRecovered.setText(cols.get(colNumRecovered).text());
                                    textViewDeaths.setText(cols.get(colNumDeaths).text());
                                    break;
                                }

                                if (cols.get(colNumCountry).hasText()) {tmpCountry = cols.get(0).text();}
                                else {tmpCountry = "NA";}

                                if (cols.get(colNumCases).hasText()) {tmpCases = cols.get(colNumCases).text();}
                                else {tmpCases = "0";}

                                if (cols.get(colNumRecovered).hasText()){
                                    tmpRecovered = cols.get(colNumRecovered).text();
                                    tmpPercentage = (generalDecimalFormat.format(Double.parseDouble(tmpRecovered.replaceAll(",", ""))
                                            / Double.parseDouble(tmpCases.replaceAll(",", ""))
                                            * 100)) + "%";
                                    tmpRecovered = tmpRecovered + "\n" + tmpPercentage;
                                }
                                else {tmpRecovered = "0";}

                                if(cols.get(colNumDeaths).hasText()) {
                                    tmpDeaths = cols.get(colNumDeaths).text();
                                    tmpPercentage = (generalDecimalFormat.format(Double.parseDouble(tmpDeaths.replaceAll(",", ""))
                                            / Double.parseDouble(tmpCases.replaceAll(",", ""))
                                            * 100)) + "%";
                                    tmpDeaths = tmpDeaths + "\n" + tmpPercentage;
                                }
                                else {tmpDeaths = "0";}

                                allCountriesResults.add(new CountryLine(tmpCountry, tmpCases, tmpRecovered, tmpDeaths));
                            }

                            setListViewCountries(allCountriesResults);
                            textSearchBox.setText(null);
                            textSearchBox.clearFocus();

                            // save results
                            editor.putString("textViewCases", textViewCases.getText().toString());
                            editor.putString("textViewRecovered", textViewRecovered.getText().toString());
                            editor.putString("textViewDeaths", textViewDeaths.getText().toString());
                            editor.putString("textViewDate", textViewDate.getText().toString());
                            editor.apply();

                            calculate_percentages();

                            myCalender = Calendar.getInstance();
                            textViewDate.setText("Last updated: " + myFormat.format(myCalender.getTime()));
                        }
                    });
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Network Connection Error!",
                                            Toast.LENGTH_LONG).show();
                        }
                    });
                }
                finally {
                    doc = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mySwipeRefreshLayout.setRefreshing(false);
                    }});
            }
        }).start();
    }
}

package com.shatrix.covid19tracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shatrix.covid19tracker.datalayer.model.CountryItem;
import com.shatrix.covid19tracker.util.ExtensionsKt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle,
            textViewRecoveredTitle, textViewActive, textViewActiveTitle, textViewNewDeaths,
            textViewNewCases, textViewNewDeathsTitle, textViewNewCasesTitle, tvColCases, tvColNewCases, tvColRecover, tvColDeaths, tvColNewDeaths;
    EditText textSearchBox;
    Handler handler;
    String url = "https://www.worldometers.info/coronavirus/";
    String tmpCountry, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage, germanResults, tmpNewCases, tmpNewDeaths;
    Document doc, germanDoc;
    Element countriesTable, row, germanTable;
    Elements countriesRows, cols, germanRows;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Calendar myCalender;
    SimpleDateFormat myFormat;
    double tmpNumber;
    DecimalFormat generalDecimalFormat;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    ListView listViewCountries;
    ListCountriesAdapter listCountriesAdapter;
    ArrayList<CountryItem> allCountriesResults, FilteredArrList;
    Intent sharingIntent;
    int colNumCountry, colNumCases, colNumRecovered, colNumDeaths, colNumActive, colNumNewCases, colNumNewDeaths;
    SwipeRefreshLayout mySwipeRefreshLayout;
    InputMethodManager inputMethodManager;
    Iterator<Element> rowIterator;
    ProgressBar countryProgressBar;

    Boolean caseDesending = true;
    Boolean newCasesDesending = false;
    Boolean recoverDesending = false;
    Boolean deathsDesending = false;
    Boolean newDaethsDesending = false;

    TextView[] headCols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // All initial definitions
        textViewCases = findViewById(R.id.textViewCases);
        textViewRecovered = findViewById(R.id.textViewRecovered);
        textViewDeaths = findViewById(R.id.textViewDeaths);
        textViewDate = findViewById(R.id.textViewDate);
        textViewRecoveredTitle = findViewById(R.id.textViewRecoveredTitle);
        textViewDeathsTitle = findViewById(R.id.textViewDeathsTitle);
        textViewActiveTitle = findViewById(R.id.textViewActiveTitle);
        textViewActive = findViewById(R.id.textViewActive);
        textViewNewDeaths = findViewById(R.id.textViewNewDeaths);
        textViewNewCases = findViewById(R.id.textViewNewCases);
        textViewNewCasesTitle = findViewById(R.id.textViewNewCasesTitle);
        textViewNewDeathsTitle = findViewById(R.id.textViewNewDeathsTitle);
        tvColCases = findViewById(R.id.colCasesHEAD);
        tvColNewCases = findViewById(R.id.colNewCasesHEAD);
        tvColRecover = findViewById(R.id.colRecoveredHEAD);
        tvColDeaths = findViewById(R.id.colDeathsHEAD);
        tvColNewDeaths = findViewById(R.id.colNewDeathsHEAD);

        Log.d("MainActivity", "" + tvColDeaths);

        headCols = new TextView[]{tvColCases, tvColNewCases, tvColRecover, tvColDeaths, tvColNewDeaths};


        listViewCountries = findViewById(R.id.listViewCountries);
        textSearchBox = findViewById(R.id.textSearchBox);
        countryProgressBar = findViewById(R.id.countryProgressBar);
        colNumCountry = 0;
        colNumCases = 1;
        colNumRecovered = 0;
        colNumDeaths = 0;
        colNumNewCases = 0;
        colNumNewDeaths = 0;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        myCalender = Calendar.getInstance();
        handler = new Handler();
        generalDecimalFormat = new DecimalFormat("0.00", symbols);
        allCountriesResults = new ArrayList<CountryItem>();

        // Implement Swipe to Refresh
        mySwipeRefreshLayout = findViewById(R.id.coronaMainSwipeRefresh);
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

            private boolean listIsAtTop() {
                if (listViewCountries.getChildCount() == 0) return true;
                return listViewCountries.getChildAt(0).getTop() == 0;
            }
        });

        listViewCountries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("CLICKED", allCountriesResults.get(position).getCountryName());
                if (allCountriesResults.get(position).getCountryName().contains("Germany")) {
                    countryProgressBar.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                germanDoc = null; // Fetches the HTML document
                                germanResults = "";
                                germanDoc = Jsoup.connect("https://www.rki.de/DE/Content/InfAZ/N/Neuartiges_Coronavirus/Fallzahlen.html").timeout(10000).get();
                                germanTable = germanDoc.select("table").get(0);
                                germanRows = germanTable.select("tbody").select("tr");
                                rowIterator = germanRows.iterator();
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        while (rowIterator.hasNext()) {
                                            row = rowIterator.next();
                                            cols = row.select("td");
                                            if (cols.get(0).text().contains("Gesamt")) {
                                                break;
                                            }
                                            germanResults = germanResults + cols.get(0).text() + " : " + cols.get(1).text().split("\\s")[0] + "\n";
                                            //Log.e("TABLE: ", cols.get(0).text() + " : " + cols.get(1).text().split("\\s")[0]);
                                        }
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Confirmed Cases in Germany")
                                                .setCancelable(true)
                                                .setMessage("Robert Koch Institut www.rki.de\n\n" +
                                                        germanResults)
                                                .setPositiveButton("Close", null)
                                                .setIcon(R.drawable.ic_info)
                                                .show();
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Network Connection Error!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } finally {
                                doc = null;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    countryProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        // fetch previously saved data in SharedPreferences, if any
        if (preferences.getString("textViewCases", null) != null) {
            textViewCases.setText(preferences.getString("textViewCases", null));
            textViewRecovered.setText(preferences.getString("textViewRecovered", null));
            textViewDeaths.setText(preferences.getString("textViewDeaths", null));
            textViewDate.setText(preferences.getString("textViewDate", null));
            textViewActive.setText(preferences.getString("textViewActive", null));
            //calculate_percentages();
        }

        // Add Text Change Listener to textSearchBox to filter by Country
        textSearchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence searchSequence, int start, int before, int count) {
                FilteredArrList = new ArrayList<CountryItem>();
                if (searchSequence == null || searchSequence.length() == 0) {
                    // back to original
                    setListViewCountries(allCountriesResults);
                } else {
                    searchSequence = searchSequence.toString().toLowerCase();
                    for (int i = 0; i < allCountriesResults.size(); i++) {
                        String data = allCountriesResults.get(i).getCountryName();
                        if (data.toLowerCase().startsWith(searchSequence.toString())) {
                            FilteredArrList.add(new CountryItem(
                                    allCountriesResults.get(i).getCountryName(),
                                    allCountriesResults.get(i).getCases(),
                                    allCountriesResults.get(i).getNewCases(),
                                    allCountriesResults.get(i).getRecovered(),
                                    allCountriesResults.get(i).getDeaths(),
                                    allCountriesResults.get(i).getNewDeaths()));
                        }
                    }
                    // set the Filtered result to return
                    setListViewCountries(FilteredArrList);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Hide keyboard after hitting done button
        textSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    textSearchBox.clearFocus();
                    return true;
                }
                return false;
            }
        });

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String filtered = "";
                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (!Character.isWhitespace(character)) {
                        filtered += character;
                    }
                }

                return filtered;
            }

        };

        textSearchBox.setFilters(new InputFilter[]{filter});
        textSearchBox.clearFocus();
        // Call refreshData once the app is opened only one time, then user can request updates
        refreshData();

        listeners();
    }

    private void listeners() {

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

    void setListViewCountries(List<CountryItem> allCountriesResults) {
        clearsAllStates();
        tvColCases.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
        setData(ExtensionsKt.sortByCases(allCountriesResults, true));
    }

    private void clearsAllStates() {
        for (TextView v : headCols) {
            v.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private void applyNewStates(TextView view, Boolean isDown) {
        clearsAllStates();
        int res = 0;
        if (isDown) res = R.drawable.ic_arrow_drop_down;
        else res = R.drawable.ic_arrow_drop_up;
        view.setCompoundDrawablesWithIntrinsicBounds(0, 0, res, 0);
    }

    //region menu selected
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
                String shareBody = "Install (COVID-19 Tracker) Android Application to get the latest " +
                        "global updates for Coronavirus Outbreak\nhttps://tinyurl.com/tsvjowr" +
                        "\n\n" +
                        "Source Code on GitHub\n" +
                        "https://tinyurl.com/qw378qo";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "COVID-19 Tracker");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share COVID-19 Tracker Link"));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    //endregion

    //region percentage
    void calculate_percentages() {
        tmpNumber = Double.parseDouble(textViewRecovered.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100;
        textViewRecoveredTitle.setText("Recovered   " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewDeaths.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100;
        textViewDeathsTitle.setText("Deaths   " + generalDecimalFormat.format(tmpNumber) + "%");

        tmpNumber = Double.parseDouble(textViewActive.getText().toString().replaceAll(",", ""))
                / Double.parseDouble(textViewCases.getText().toString().replaceAll(",", ""))
                * 100;
        textViewActiveTitle.setText("Active   " + generalDecimalFormat.format(tmpNumber) + "%");
    }
    //endregion

    //region refresh
    void refreshData() {
        mySwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    doc = Jsoup.connect(url).timeout(10000).get();
                    // table id main_table_countries
                    countriesTable = doc.select("table").get(0);
                    countriesRows = countriesTable.select("tr");
                    //Log.e("TITLE", elementCases.text());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // get countries
                            rowIterator = countriesRows.iterator();
                            allCountriesResults = new ArrayList<CountryItem>();

                            // read table header and find correct column number for each category
                            row = rowIterator.next();
                            cols = row.select("th");
                            //Log.e("COLS: ", cols.text());
                            if (cols.get(0).text().contains("Country")) {
                                for (int i = 1; i < cols.size(); i++) {
                                    if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Cases")) {
                                        colNumCases = i;
                                        Log.e("Cases: ", cols.get(i).text());
                                    } else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Recovered")) {
                                        colNumRecovered = i;
                                        Log.e("Recovered: ", cols.get(i).text());
                                    } else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Deaths")) {
                                        colNumDeaths = i;
                                        Log.e("Deaths: ", cols.get(i).text());
                                    } else if (cols.get(i).text().contains("Active") && cols.get(i).text().contains("Cases")) {
                                        colNumActive = i;
                                        Log.e("Active: ", cols.get(i).text());
                                    } else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Cases")) {
                                        colNumNewCases = i;
                                        Log.e("NewCases: ", cols.get(i).text());
                                    } else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Deaths")) {
                                        colNumNewDeaths = i;
                                        Log.e("NewDeaths: ", cols.get(i).text());
                                    }
                                }
                            }

                            while (rowIterator.hasNext()) {
                                row = rowIterator.next();
                                cols = row.select("td");

                                if (cols.get(0).text().contains("World")) {
                                    textViewCases.setText(cols.get(colNumCases).text());
                                    textViewRecovered.setText(cols.get(colNumRecovered).text());
                                    textViewDeaths.setText(cols.get(colNumDeaths).text());

                                    if (cols.get(colNumActive).hasText()) {
                                        textViewActive.setText(cols.get(colNumActive).text());
                                    } else {
                                        textViewActive.setText("0");
                                    }
                                    if (cols.get(colNumNewCases).hasText()) {
                                        textViewNewCases.setText(cols.get(colNumNewCases).text());
                                    } else {
                                        textViewNewCases.setText("0");
                                    }
                                    if (cols.get(colNumNewDeaths).hasText()) {
                                        textViewNewDeaths.setText(cols.get(colNumNewDeaths).text());
                                    } else {
                                        textViewNewDeaths.setText("0");
                                    }
                                    continue;
                                } else if (
                                        cols.get(0).text().contains("Total") ||
                                                cols.get(0).text().contains("Europe") ||
                                                cols.get(0).text().contains("North America") ||
                                                cols.get(0).text().contains("Asia") ||
                                                cols.get(0).text().contains("South America") ||
                                                cols.get(0).text().contains("Africa") ||
                                                cols.get(0).text().contains("Oceania")
                                ) {
                                    continue;
                                }

                                if (cols.get(colNumCountry).hasText()) {
                                    tmpCountry = cols.get(0).text();
                                } else {
                                    tmpCountry = "NA";
                                }

                                if (cols.get(colNumCases).hasText()) {
                                    tmpCases = cols.get(colNumCases).text();
                                } else {
                                    tmpCases = "0";
                                }

                                if (cols.get(colNumRecovered).hasText()) {
                                    tmpRecovered = cols.get(colNumRecovered).text();
                                } else {
                                    tmpRecovered = "0";
                                }

                                if (cols.get(colNumDeaths).hasText()) {
                                    tmpDeaths = cols.get(colNumDeaths).text();
                                } else {
                                    tmpDeaths = "0";
                                }

                                if (cols.get(colNumNewCases).hasText()) {
                                    tmpNewCases = cols.get(colNumNewCases).text();
                                } else {
                                    tmpNewCases = "0";
                                }

                                if (cols.get(colNumNewDeaths).hasText()) {
                                    tmpNewDeaths = cols.get(colNumNewDeaths).text();
                                } else {
                                    tmpNewDeaths = "0";
                                }

                                allCountriesResults.add(new CountryItem(tmpCountry, tmpCases, tmpNewCases, tmpRecovered, tmpDeaths, tmpNewDeaths));
                            }

                            setListViewCountries(allCountriesResults);
                            textSearchBox.setText(null);
                            textSearchBox.clearFocus();

                            // save results
                            editor.putString("textViewCases", textViewCases.getText().toString());
                            editor.putString("textViewRecovered", textViewRecovered.getText().toString());
                            editor.putString("textViewActive", textViewActive.getText().toString());
                            editor.putString("textViewDeaths", textViewDeaths.getText().toString());
                            editor.putString("textViewDate", textViewDate.getText().toString());
                            editor.apply();

                            calculate_percentages();

                            myCalender = Calendar.getInstance();
                            textViewDate.setText("Last updated: " + myFormat.format(myCalender.getTime()));
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Network Connection Error!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    doc = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mySwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }
    //endregion


    //// TODO: 4/10/20 check filter case

    public void sortByCases(View view) {
        caseDesending = !caseDesending;
        applyNewStates((TextView) view, caseDesending);
        setData(ExtensionsKt.sortByCases(allCountriesResults, caseDesending));
    }

    public void sortByNewCases(View view) {
        newCasesDesending = !newCasesDesending;
        applyNewStates((TextView) view, newCasesDesending);
        setData(ExtensionsKt.sortByNewCases(allCountriesResults, newCasesDesending));
    }

    public void sortByRecover(View view) {
        recoverDesending = !recoverDesending;
        applyNewStates((TextView) view, recoverDesending);
        setData(ExtensionsKt.sortByRecover(allCountriesResults, recoverDesending));
    }

    public void sortByDeaths(View view) {
        deathsDesending = !deathsDesending;
        applyNewStates((TextView) view, deathsDesending);
        setData(ExtensionsKt.sortByDeaths(allCountriesResults, deathsDesending));
    }

    public void sortBynewDeaths(View view) {
        newDaethsDesending = !newDaethsDesending;
        applyNewStates((TextView) view, newDaethsDesending);
        setData(ExtensionsKt.sortByNewDeaths(allCountriesResults, newDaethsDesending));
    }

    void setData(List<CountryItem> countryItems) {
        listCountriesAdapter = new ListCountriesAdapter(this, countryItems);
        listViewCountries.setAdapter(listCountriesAdapter);
    }

}

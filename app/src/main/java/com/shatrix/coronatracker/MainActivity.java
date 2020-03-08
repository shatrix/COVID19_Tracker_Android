package com.shatrix.coronatracker;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate, textViewDeathsTitle, textViewRecoveredTitle ;
    Handler handler;
    public ProgressBar pBar;
    String url = "https://www.worldometers.info/coronavirus/";
    String body, tmpString, tmpCountry, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage;
    Document doc;
    Element countriesTable, row;
    Elements countriesRows, cols;
    Pattern p, p1;
    Matcher m;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Calendar myCalender;
    SimpleDateFormat myFormat;
    double tmpNumber;
    DecimalFormat generalDecimalFormat;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    ListView listViewCountries;
    ListCountriesAdapter listCountriesAdapter;
    List<String> countriesNames;
    List<String> numberCases;
    List<String> numberRecovered;
    List<String> numberDeaths;
    int colNumCountry, colNumCases, colNumRecovered, colNumDeaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCases = (TextView)findViewById(R.id.textViewCases);
        textViewRecovered = (TextView)findViewById(R.id.textViewRecovered);
        textViewDeaths = (TextView)findViewById(R.id.textViewDeaths);
        textViewDate = (TextView)findViewById(R.id.textViewDate);
        textViewRecoveredTitle = (TextView)findViewById(R.id.textViewRecoveredTitle);
        textViewDeathsTitle = (TextView)findViewById(R.id.textViewDeathsTitle);

        listViewCountries = (ListView)findViewById(R.id.listViewCountries);

        colNumCountry = 0; colNumCases = 1; colNumRecovered = 0; colNumDeaths = 0;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        myFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm:ss aaa", Locale.US);
        myCalender = Calendar.getInstance();
        handler = new Handler() ;
        pBar = (ProgressBar) findViewById(R.id.pBar);
        pBar.setVisibility(View.VISIBLE);

        generalDecimalFormat = new DecimalFormat("0.00", symbols);

        if(preferences.getString("textViewCases", null) != null ){
            textViewCases.setText(preferences.getString("textViewCases", null));
            textViewRecovered.setText(preferences.getString("textViewRecovered", null));
            textViewDeaths.setText(preferences.getString("textViewDeaths", null));
            textViewDate.setText(preferences.getString("textViewDate", null));

            calculate_percentages();
        }

        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    void setListViewCountries(String[] countriesNames, String[] numberCases, String[] numberRecovered, String[] numberDeaths) {
        listCountriesAdapter = new ListCountriesAdapter(this, countriesNames, numberCases, numberRecovered, numberDeaths);
        listViewCountries.setAdapter(listCountriesAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                new AlertDialog.Builder(this)
                        .setTitle("Corona COVID-19 Monitor")
                        .setCancelable(true)
                        .setMessage("COVID-19 CORONAVIRUS Latest Global Updates\n\n" +
                                "Numbers are based on:\nhttps://www.worldometers.info/coronavirus\n" +
                                "\n\n" +
                                "Developer: Sherif Mousa (Shatrix)" +
                                "\n" +
                                "\n" +
                                "Github,LinkedIn,Facebook,Twitter @shatrix")
                        .setPositiveButton("Close", null)
                        .setIcon(R.drawable.ic_info)
                        .show();
                return true;
            case R.id.action_refresh:
                pBar.setVisibility(View.VISIBLE);
                refreshData();
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
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    doc = Jsoup.connect(url).timeout(10000).get();
                    //body = doc.body().text();
                    // table id main_table_countries
                    countriesTable = doc.getElementById("main_table_countries");
                    countriesRows = countriesTable.select("tr");
                    //Log.e("TITLE", elementCases.text());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // get countries
                            Iterator<Element> rowIterator = countriesRows.iterator();
                            //rowIterator.next();
                            countriesNames = new ArrayList<String>();
                            numberCases = new ArrayList<String>();
                            numberRecovered = new ArrayList<String>();
                            numberDeaths = new ArrayList<String>();

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

                                countriesNames.add(tmpCountry);
                                numberCases.add(tmpCases);
                                numberRecovered.add(tmpRecovered);
                                numberDeaths.add(tmpDeaths);
                            }

                            setListViewCountries(countriesNames.toArray(new String[countriesNames.size()]),
                                    numberCases.toArray(new String[countriesNames.size()]),
                                    numberRecovered.toArray(new String[countriesNames.size()]),
                                    numberDeaths.toArray(new String[countriesNames.size()]));

                            // save results
                            editor.putString("textViewCases", textViewCases.getText().toString());
                            editor.putString("textViewRecovered", textViewRecovered.getText().toString());
                            editor.putString("textViewDeaths", textViewDeaths.getText().toString());
                            editor.putString("textViewDate", textViewDate.getText().toString());
                            editor.apply();

                            calculate_percentages();

                            pBar.setVisibility(View.GONE);
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
                            pBar.setVisibility(View.GONE);
                        }
                    });
                }
                finally {
                    doc = null;
                }
            }
        }).start();
    }
}

//                            // Cases: 98,061
//                            p = Pattern.compile("-?\\d+,\\d+");
//                            p1 = Pattern.compile("-?Cases: \\d+,\\d+");
//                            m = p1.matcher(body);
//                            m.find();
//                            tmpString = m.group();
//                            m = p.matcher(tmpString);
//                            m.find();
//                            textViewCases.setText(m.group());
//                            // Deaths: 3,356
//                            p1 = Pattern.compile("-?Deaths: \\d+,\\d+");
//                            m = p1.matcher(body);
//                            m.find();
//                            tmpString = m.group();
//                            m = p.matcher(tmpString);
//                            m.find();
//                            textViewDeaths.setText(m.group());
//                            // Recovered: 51,202
//                            p1 = Pattern.compile("-?Recovered: \\d+,\\d+");
//                            m = p1.matcher(body);
//                            m.find();
//                            tmpString = m.group();
//                            m = p.matcher(tmpString);
//                            m.find();
//                            textViewRecovered.setText(m.group());
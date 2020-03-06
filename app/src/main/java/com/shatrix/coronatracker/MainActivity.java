package com.shatrix.coronatracker;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView textViewCases, textViewRecovered, textViewDeaths, textViewDate ;
    Button btnRefresh ;
    Handler handler;
    public ProgressBar pBar;
    String url = "https://www.worldometers.info/coronavirus/";
    String title, body, tmpString;
    Document doc;
    Pattern p, p1;
    Matcher m;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Calendar myCalender;
    SimpleDateFormat myFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCases = (TextView)findViewById(R.id.textViewCases);
        textViewRecovered = (TextView)findViewById(R.id.textViewRecovered);
        textViewDeaths = (TextView)findViewById(R.id.textViewDeaths);
        textViewDate = (TextView)findViewById(R.id.textViewDate);
        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        myFormat = new SimpleDateFormat("hh:mm aaa .. dd MMMM yyy");
        myCalender = Calendar.getInstance();
        handler = new Handler() ;
        pBar = (ProgressBar) findViewById(R.id.pBar);
        pBar.setVisibility(View.VISIBLE);
        refreshData();

        if(preferences.getString("textViewCases", null) != null ){
            textViewCases.setText(preferences.getString("textViewCases", null));
            textViewRecovered.setText(preferences.getString("textViewRecovered", null));
            textViewDeaths.setText(preferences.getString("textViewDeaths", null));
            textViewDate.setText(preferences.getString("textViewDate", null));
        }

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Will be implemented later!",
                //        Toast.LENGTH_LONG).show();
                pBar.setVisibility(View.VISIBLE);
                refreshData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                new AlertDialog.Builder(this)
                        .setTitle("Corona Monitor")
                        .setCancelable(true)
                        .setMessage("Coronavirus live updates are retrieved from\n\n" +
                                "https://www.worldometers.info/coronavirus\n" +
                                "\n\n" +
                                "Developed by Shatrix")
                        .setPositiveButton("Close", null)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    void refreshData() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    try {
                        doc = Jsoup.connect(url).timeout(5000).get();
                        title = doc.title();
                        body = doc.body().text();
                        Log.e("TITLE", title);
                        Log.e("BODY", body);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                p = Pattern.compile("-?\\d+,\\d+");
                                m = p.matcher(title);
                                m.find();
                                textViewCases.setText(m.group());
                                m.find();
                                textViewDeaths.setText(m.group());
                                // Recovered: 51,202
                                p1 = Pattern.compile("-?Recovered: \\d+,\\d+");
                                m = p1.matcher(body);
                                m.find();
                                tmpString = m.group();
                                Log.e("tmpString", tmpString);
                                m = p.matcher(tmpString);
                                m.find();
                                textViewRecovered.setText(m.group());
                                pBar.setVisibility(View.GONE);
                                textViewDate.setText("Updated: " + myFormat.format(myCalender.getTime()));

                                // save results
                                editor.putString("textViewCases", textViewCases.getText().toString());
                                editor.putString("textViewRecovered", textViewRecovered.getText().toString());
                                editor.putString("textViewDeaths", textViewDeaths.getText().toString());
                                editor.putString("textViewDate", textViewDate.getText().toString());
                                editor.apply();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Network Connection Error!",
                                Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}

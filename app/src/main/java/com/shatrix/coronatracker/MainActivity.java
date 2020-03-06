package com.shatrix.coronatracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    TextView textViewCases, textViewRecovered, textViewDeaths ;
    Button btnRefresh ;
    Handler handler;
    public ProgressBar pBar;
    String url = "https://www.worldometers.info/coronavirus/";
    String title, body, tmpString;
    Document doc;
    Pattern p, p1;
    Matcher m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCases = (TextView)findViewById(R.id.textViewCases);
        textViewRecovered = (TextView)findViewById(R.id.textViewRecovered);
        textViewDeaths = (TextView)findViewById(R.id.textViewDeaths);
        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        handler = new Handler() ;
        pBar = (ProgressBar) findViewById(R.id.pBar);
        pBar.setVisibility(View.VISIBLE);
        refreshData();

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
    void refreshData() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    try {
                        doc = Jsoup.connect(url).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                        }
                    });
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}

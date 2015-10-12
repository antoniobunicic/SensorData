package hr.fer.tel.ab46700.sensordata;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DoorActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);

        Button button = (Button) findViewById(R.id.buttonSetHours);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                DatePicker dp = (DatePicker) findViewById(R.id.datePicker);
                Date date = getDateFromDatePicker(dp);

                new FromDatabase().execute("doorlog", df.format(date));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_door, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FromDatabase extends AsyncTask<String, Integer, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            String code = params[0];
            String date = params[1];

            String[] array = new String[3];
            array[0] = code;
            array[1] = date;

            try {
                //otvara vezu sa serverom preko URLa
                URL url= new URL ("http://" + Constants.myIP + ":8000/get?what=" + code + "&hours=" + date);
                URLConnection yc = url.openConnection();

                //odgovor servera, zadnja vrijednost
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    array[2] = inputLine;
                }
                in.close();

            }
            catch(Exception ex) {
            }

            return array;
        }

        @Override
        protected void onPostExecute(String[] result) {
            String date = result[1];

            TextView statistics = (TextView) findViewById(R.id.textViewStatistika);
            statistics.setText(getString(R.string.door_open));

            LinearLayout lv1 = (LinearLayout) findViewById(R.id.linear);
            lv1.removeAllViews();

            if (result[2] != null) {
                String[] parts = result[2].split("/");

                for (int i = 0; i < parts.length; i++ ) {
                    String[] datumisat = parts[i].split(" ");
                    TextView item = new TextView(DoorActivity.this);
                    item.setText(datumisat[1]);
                    item.setTextColor(Color.BLACK);
                    item.setTextSize(25);
                    item.setPadding(0,0,0,5);
                    lv1.addView(item);
                }
            } else {
                TextView noValues = new TextView(DoorActivity.this);
                noValues.setText("Za navedeni datum nema dostupnih mjerenja.");
                lv1.addView(noValues);
            }

            // enable clicking on button
            Button button = (Button) DoorActivity.this.findViewById(R.id.buttonSetHours);
            button.setClickable(true);
        }

        @Override
        protected void onPreExecute() {
            // disable clicking on button
            Button button = (Button) DoorActivity.this.findViewById(R.id.buttonSetHours);
            button.setClickable(false);

            //reset and show progress bar
            //ProgressBar pbar = (ProgressBar) HumidityActivity.this.findViewById(R.id.progressBar);
            //pbar.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //update progress bar
            //ProgressBar pbar = (ProgressBar) HumidityActivity.this.findViewById(R.id.progressBar);
            //pbar.setProgress(values[0]);
        }
    }

    public static java.util.Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }
}

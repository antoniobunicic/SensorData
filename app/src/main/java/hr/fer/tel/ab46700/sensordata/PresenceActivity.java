package hr.fer.tel.ab46700.sensordata;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class PresenceActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence);

        TextView legend1 = (TextView)findViewById(R.id.textViewLegend1);
        TextView legend2 = (TextView)findViewById(R.id.textViewLegend2);
        TextView description1 = (TextView)findViewById(R.id.textViewDescription1);
        TextView description2 = (TextView)findViewById(R.id.textViewDescription2);
        legend1.setVisibility(View.GONE);
        legend2.setVisibility(View.GONE);
        description1.setVisibility(View.GONE);
        description2.setVisibility(View.GONE);

        Button button = (Button) findViewById(R.id.buttonSetHours);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                DatePicker dp = (DatePicker)findViewById(R.id.datePicker);
                Date date = getDateFromDatePicker(dp);

                new FromDatabase().execute("presencelog", df.format(date));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_presence, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            String[] parts = result[2].split("/");

            float[] ratio = new float[2];
            ratio[0] = Float.parseFloat(parts[0]);
            ratio[1] = Float.parseFloat(parts[1]);
            float ukupno = ratio[0]+ratio[1];

            TextView statistics = (TextView) findViewById(R.id.textViewStatistika);
            statistics.setText("Statistika za datum: " + date);

            TextView legend1 = (TextView)findViewById(R.id.textViewLegend1);
            TextView legend2 = (TextView)findViewById(R.id.textViewLegend2);
            TextView description1 = (TextView)findViewById(R.id.textViewDescription1);
            TextView description2 = (TextView)findViewById(R.id.textViewDescription2);
            legend1.setVisibility(View.VISIBLE);
            legend2.setVisibility(View.VISIBLE);
            description1.setVisibility(View.VISIBLE);
            description2.setVisibility(View.VISIBLE);

            LinearLayout lv1 = (LinearLayout) findViewById(R.id.linear);
            lv1.removeAllViews();

            if (ratio[0]==0 && ratio[1]==0) {
                TextView noValues = new TextView(PresenceActivity.this);
                noValues.setText("Za navedeni datum nema dostupnih mjerenja.");
                lv1.addView(noValues);
            } else {
                ratio = calculateData(ratio);
                MyGraphview graphview = new MyGraphview(PresenceActivity.this, ratio);
                lv1.addView(graphview);
            }

            // enable clicking on button
            Button button = (Button) PresenceActivity.this.findViewById(R.id.buttonSetHours);
            button.setClickable(true);
        }

        @Override
        protected void onPreExecute() {
            // disable clicking on button
            Button button = (Button) PresenceActivity.this.findViewById(R.id.buttonSetHours);
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

    private float[] calculateData(float[] data) {
        float total = 0;
        for (int i = 0; i < data.length; i++) {
            total += data[i];
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = 360 * (data[i] / total);
        }
        return data;
    }

    public class MyGraphview extends View {
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float[] value_degree;
        RectF rectf = new RectF(235, 110, 835, 710);
        float temp = 0;

        public MyGraphview(Context context, float[] values) {
            super(context);
            value_degree = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                value_degree[i] = values[i];
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            for (int i = 0; i < value_degree.length; i++) {
                if (i == 0) {
                    int color = Color.parseColor("#00c853");
                    paint.setColor(color);
                    canvas.drawArc(rectf, 0, value_degree[i], true, paint);
                } else {
                    temp += value_degree[i - 1];
                    int color = Color.parseColor("#ffa8e6ba");
                    paint.setColor(color);
                    canvas.drawArc(rectf, temp, value_degree[i], true, paint);
                }
            }
        }
    }

}

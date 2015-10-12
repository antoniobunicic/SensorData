package hr.fer.tel.ab46700.sensordata;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class HumidityActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humidity);

        final SeekBar sb = (SeekBar) findViewById(R.id.seekBarHum);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tv = (TextView) findViewById(R.id.textViewSeekBarValues);
                tv.setText(progress+2 + "h");
                //setProgress(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button buttonSetHours = (Button) findViewById(R.id.buttonSetHours);
        buttonSetHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int brojSati = sb.getProgress()+2;
                new FromDatabase().execute("humiditylog", Integer.toString(brojSati));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_humidity, menu);
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
            String hours = params[1];

            String[] array = new String[3];
            array[0] = code;
            array[1] = hours;

            //int progress = 1;
            //publishProgress(progress++);

            try {
                //otvara vezu sa serverom preko URLa
                URL url= new URL ("http://" + Constants.myIP + ":8000/get?what=" + code + "&hours=" + hours);
                URLConnection yc = url.openConnection();

                //odgovor servera, zadnja vrijednost
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    array[2] = inputLine;
                    //publishProgress(progress++);
            }
                in.close();

            }
            catch(Exception ex) {
            }
            return array;
        }

        @Override
        protected void onPostExecute(String[] result) {
            int hours = Integer.parseInt(result[1]);

            TextView statistics = (TextView) findViewById(R.id.textViewStatistika);
            statistics.setText("Statistika za zadnjih " + hours + "h:");

            GraphView graph = (GraphView) findViewById(R.id.graphHumidity);
            DataPoint[] points = new DataPoint[hours];
            String[] parts = result[2].split("/");

            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
            final DateFormat onlyHour = new SimpleDateFormat("HH:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.set(2000, 1, 1);

            Date dmax = calendar.getTime();
            float maxvalue = 1;

            try {
                for (int i = 0; i < hours; i++) {

                    String[] one = parts[i].split(":");
                    Date date = dateFormat.parse(one[0]);

                    float value = Float.parseFloat(one[1]) / 10;
                    points[i] = new DataPoint(date, value);

                    if (date.after(dmax)) dmax = date;
                    if (value > maxvalue) maxvalue = value;
                }

            } catch (ParseException ex){
            }

            calendar.setTime(dmax);
            calendar.add(Calendar.HOUR, -hours + 1);
            Date dmin = calendar.getTime();

            graph.removeAllSeries();
            BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(points);

            series.setColor(Color.parseColor("#1976d2"));
            series.setSpacing(15);

            series.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((long) dataPoint.getX());
                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy. - HH");
                    String vlaznost = String.format("%.1f", (float) dataPoint.getY());
                    Toast.makeText(HumidityActivity.this, df.format(calendar.getTime()) + "h: " + vlaznost + "%", Toast.LENGTH_SHORT).show();
                }
            });

            // set date label formatter
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(HumidityActivity.this, onlyHour));
            graph.getGridLabelRenderer().setNumHorizontalLabels(5);

            //set manual x bounds
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(dmax.getTime());
            graph.getViewport().setMinX(dmin.getTime());

            //set manual y bounds
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(100);
            graph.getViewport().setMinY(0);

            graph.addSeries(series);

            // enable clicking on button
            Button button = (Button) HumidityActivity.this.findViewById(R.id.buttonSetHours);
            button.setClickable(true);
        }

        @Override
        protected void onPreExecute() {
            // disable clicking on button
            Button button = (Button) HumidityActivity.this.findViewById(R.id.buttonSetHours);
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
}

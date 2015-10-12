package hr.fer.tel.ab46700.sensordata;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LuminosityActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luminosity);

        final SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tv = (TextView) findViewById(R.id.textViewSeekBarValues);
                tv.setText(progress+2 + "h");
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
                int hours = sb.getProgress()+2;
                new FromDatabase().execute("luminositylog", Integer.toString(hours));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_temperature, menu);
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

            try {
                //otvara vezu sa serverom preko URLa
                URL url= new URL ("http://" + Constants.myIP + ":8000/get?what=" + code + "&hours=" + hours);
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
            int hours = Integer.parseInt(result[1]);

            TextView statistics = (TextView) findViewById(R.id.textViewStatistika);
            statistics.setText("Statistika za zadnjih " + hours + "h:");

            GraphView graph = (GraphView) findViewById(R.id.graphLuminosity);
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

                    float value = Float.parseFloat(one[1]);
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
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(points);

            series.setColor(Color.parseColor("#ff9800"));
            series.setThickness(10);

            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
            staticLabelsFormatter.setVerticalLabels(new String[]{getString(R.string.very_bad)+" ", getString(R.string.bad)+" ", getString(R.string.medium)+" ", getString(R.string.good)+" "});
            staticLabelsFormatter.setDynamicLabelFormatter(new DateAsXAxisLabelFormatter(LuminosityActivity.this, onlyHour));
            graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
            graph.getGridLabelRenderer().setNumHorizontalLabels(5);

            //set manual x bounds
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(dmax.getTime());
            graph.getViewport().setMinX(dmin.getTime());

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(0);
            graph.getViewport().setMinY(Math.round(maxvalue) + 100);

            graph.addSeries(series);

            // enable clicking on button
            Button button = (Button) LuminosityActivity.this.findViewById(R.id.buttonSetHours);
            button.setClickable(true);
        }

        @Override
        protected void onPreExecute() {
            // disable clicking on button
            Button button = (Button) LuminosityActivity.this.findViewById(R.id.buttonSetHours);
            button.setClickable(false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }
}

package hr.fer.tel.ab46700.sensordata;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main2);

        new Latest().execute("templog2");
        new Latest().execute("humiditylog");
        new Latest().execute("luminositylog");
        new Latest().execute("presencelog");
        new Latest().execute("doorlog");

        Button buttonRefresh = (Button) findViewById(R.id.refresh_button);

        //Activity Refresh button
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBarMain);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.progressBarColor), PorterDuff.Mode.SRC_IN);

        final TextView TemperatureTextView = (TextView)findViewById(R.id.textViewTemp);
        TextView HumidityTextView = (TextView)findViewById(R.id.textViewVlaga);
        TextView LuminosityTextView = (TextView)findViewById(R.id.textViewOsvjetljenje);
        TextView PresenceTextView = (TextView)findViewById(R.id.textViewPrisutnost);
        TextView DoorTextView = (TextView)findViewById(R.id.textViewVrata);

        TableRow tableRowLuminosity = (TableRow) findViewById(R.id.tableRowLum);
        TableRow tableRowPresence = (TableRow) findViewById(R.id.tableRowPres);
        TableRow tableRowDoor = (TableRow) findViewById(R.id.tableRowDoor);

        TemperatureTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TemperatureActivity.class);
                startActivity(intent);
            }
        });

        HumidityTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HumidityActivity.class);
                startActivity(intent);
            }
        });

        tableRowLuminosity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LuminosityActivity.class);
                startActivity(intent);
            }
        });

        tableRowPresence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PresenceActivity.class);
                startActivity(intent);
            }
        });

        tableRowDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DoorActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class Latest extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String code = params[0];
            String latest_value = "0";

            String[] array = new String[2];
            array[0] = code;

            try {
                //otvara vezu sa serverom preko URLa
                URL url= new URL ("http://" + Constants.myIP + ":8000/get?what=" + code + "&hours=latest");
                URLConnection conn = url.openConnection();
                //odgovor servera, zadnja vrijednost
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    latest_value = inputLine;
                    array[1] = latest_value;
                }
                in.close();
            }
            catch(Exception ex) {
                System.out.println(ex);
            }
            return array;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result[1] != null) {
                switch (result[0]) {
                    case "templog2":
                        TextView TemperatureValueTextView = (TextView) findViewById(R.id.current_temp_value);
                        TemperatureValueTextView.setText(Float.parseFloat(result[1]) / 10 + "°C");
                        break;
                    case "humiditylog":
                        TextView HumidityValueTextView = (TextView) findViewById(R.id.current_humidity_value);
                        HumidityValueTextView.setText(Float.parseFloat(result[1]) / 10 + "%");
                        break;
                    case "luminositylog":
                        TextView LuminosityValueTextView = (TextView) findViewById(R.id.current_luminosity_value);
                        if (Integer.parseInt(result[1]) >= 1 && Integer.parseInt(result[1]) <= 10) {
                            LuminosityValueTextView.setText("dobro");
                        } else if (Integer.parseInt(result[1]) >= 11 && Integer.parseInt(result[1]) <= 20) {
                            LuminosityValueTextView.setText("slabo");
                        } else {
                            LuminosityValueTextView.setText("loše");
                        }
                        break;
                    case "presencelog":
                        TextView PresenceValueTextView = (TextView) findViewById(R.id.presence_value);
                        if (result[1].equals("0")) {
                            PresenceValueTextView.setText("prazna");
                        } else {
                            PresenceValueTextView.setText("nije prazna");
                        }
                        break;
                    case "doorlog":
                        TextView DoorValueTextView = (TextView) findViewById(R.id.door_value);
                        if (result[1].equals("0")) {
                            DoorValueTextView.setText("otvorena");
                        } else {
                            DoorValueTextView.setText("zatvorena");
                        }

                        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBarMain);
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                }
            } else {
                Toast.makeText(MainActivity.this, "Dohvaćanje podataka nije uspjelo. Provjerite vezu s Internetom i je li server dostupan.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}

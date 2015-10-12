package hr.fer.tel.ab46700.sensordata;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final EditText editText = (EditText)findViewById(R.id.editTextIP);
        editText.setText(Constants.myIP);

        Button buttonSetIP = (Button)findViewById(R.id.buttonSetIP);
        buttonSetIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.myIP = editText.getText().toString();
                Toast.makeText(getApplicationContext(), "Postavljena nova IP adresa: " + Constants.myIP, Toast.LENGTH_LONG).show();
            }
        });

    }
}


package it.unisalento.iit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import it.unisalento.iit.graph.Grafico;
import it.unisalento.iit.uart.ComandoActivity;
import it.unisalento.iit.utility.IntentHelper;
import it.unisalento.iit.utility.MessaggiHelper;
import it.unisalento.iit.utility.MyUtility;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton btnConnect, btnDatiSalvati;
    Button btnExit;

    final int RIGA_TIMESTAMP = 0, RIGA_SAMPLES = 1, RIGA_FREQ = 2, RIGA_GAIN = 3, RIGA_CANALI = 4, RIGA_DATI = 7;

    String timestamp, samples, freq, gain, n_canali;
    String riga;
    String info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);
        btnDatiSalvati = findViewById(R.id.btnDatiSalvati);
        btnExit = findViewById(R.id.btnExit);

        setListener();
    }

    private void setListener(){
        btnConnect.setOnClickListener(this);
        btnDatiSalvati.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case(R.id.btnConnect):
                IntentHelper.apriConnectActivity(this);
                break;
            case(R.id.btnDatiSalvati):
                IntentHelper.apriCsv(this);
                break;
            case(R.id.btnExit):
                MessaggiHelper.showDialogExit(this);
                break;
        }
    }


    // Metodo che ottiene i dati dal file scelto dall'utente
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        InputStream inputStream = null;
        BufferedReader reader = null;
        String[] colonne;
        int count = 0;


        ArrayList<Entry> val_ch1 = new ArrayList<>();
        ArrayList<Entry> val_ch2 = new ArrayList<>();
        float x, y_ch1, y_ch2;

        if (requestCode == IntentHelper.getRequestcodeApricsv() && resultCode == RESULT_OK) {
            try {
                assert data != null;
                inputStream = getContentResolver().openInputStream(data.getData());
                reader = new BufferedReader(new InputStreamReader(inputStream));

                while ((riga = reader.readLine()) != null) {
                    switch(count){
                        case(RIGA_TIMESTAMP):
                            timestamp = estraiValore();
                            break;
                        case(RIGA_SAMPLES):
                            samples = estraiValore();
                            break;
                        case(RIGA_FREQ):
                            freq = estraiValore();
                            break;
                        case(RIGA_GAIN):
                            gain = estraiValore();
                            break;
                        case(RIGA_CANALI):
                            n_canali = estraiValore();
                            break;
                    }

                    info = "Timestamp: " + timestamp + "\n" +
                            "Samples: " + samples + "\n" +
                            "Freq: " + freq + "\n" +
                            "Gain: " + gain+ "\n" +
                            "Channels: " + n_canali;

                    if(count >= RIGA_DATI) {
                        colonne = suddividiCelle();

                        x = Float.parseFloat(colonne[0]);
                        y_ch1 =  Float.parseFloat(colonne[1]);
                        y_ch2 =  Float.parseFloat(colonne[2]);

                        val_ch1.add(new Entry(x,y_ch1));
                        val_ch2.add(new Entry(x,y_ch2));
                    }
                    count++;
                }
                inputStream.close();

                IntentHelper.apriDatiSalvatiActivity(this, info, Integer.valueOf(freq), val_ch1,val_ch2 );
                //IntentHelper.apriGraficoActivity(this, info, Integer.valueOf(freq), val_ch1,val_ch2 );

            }
            catch (Exception exception) {
                MessaggiHelper.showToast(this,"File non valido");
                //exception.printStackTrace();
            }
        }
    }


    private String[] suddividiCelle(){
        return riga.split(";");
    }

    private String estraiValore(){
        return riga.substring(riga.indexOf(": ")+2);
    }

    @Override
    public void onBackPressed() {
        MessaggiHelper.showDialogExit(this);
    }
}
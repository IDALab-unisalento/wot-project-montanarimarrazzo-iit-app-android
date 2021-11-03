package it.unisalento.iit.graph;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import it.unisalento.iit.uart.UARTService;
import it.unisalento.iit.R;
import it.unisalento.iit.utility.DatiHelper;
import it.unisalento.iit.utility.IntentHelper;
import it.unisalento.iit.utility.MessaggiHelper;
import it.unisalento.iit.utility.MyUtility;
import it.unisalento.iit.utility.SwipeListener;

public class GraficoActivity extends AppCompatActivity implements View.OnClickListener {

    private RicevitorePacchetto ricevitorePacchetto;

    private boolean salvato = false, grafico_completo = false;

    private String pacchetto = "", ESTENSIONE = ".csv";
    private String[] header_temp;
    private int[] header, dati_pacchetto;

    private View viewRoot;
    private LinearLayout containerNavigazione, opzioni;
    private ImageButton btnSalvaFile, btnAvanti, btnZoom, btnIndietro, btnCondividi, btnEmail, btnHome;
    private TextView txtHeader;
    private LineChart multilineChart;

    //Informazioni dell'Header
    private int tipo_pacc, pacc_corr, tot_pacc, n_canali, tot_sample, guad, freq, n_sample_ult_pac;
    int SAMPLE_MAX = 59; //Numero massimo campioni nel pacchetto

    private ArrayList<Entry> valoriCh1 = new ArrayList<>();
    private ArrayList<Entry> valoriCh2 = new ArrayList<>();

    //BroadcastReceiver per ricevere i dati dal dispositivo
    private class RicevitorePacchetto extends BroadcastReceiver {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {

            // Pacchetto Dati Ricevuto
            pacchetto = intent.getStringExtra("it.unisalento.iit.uart.EXTRA_PACCHETTO");

            header_temp = DatiHelper.estraiHeader(pacchetto);
            DatiHelper.inverti_Byte(header_temp, 6);
            header = DatiHelper.convHeaderHex2Dec(header_temp);

            //Informazioni dell'Header
            tipo_pacc = header[0];
            pacc_corr = header[1];
            tot_pacc = header[2];
            n_canali = header[3];
            n_sample_ult_pac = header[4];
            guad = header[5];
            freq = header[6]; //2 byte
            tot_sample = (SAMPLE_MAX * (tot_pacc - 1)) + n_sample_ult_pac; //extra_header[4] contiene il numero di campioni dell'ultimo pacchetto

            String[] dati_temp = DatiHelper.estraiDati(pacchetto);
            DatiHelper.inverti_Byte(dati_temp, 0);
            dati_pacchetto = DatiHelper.convDatiHex2Dec(dati_temp);

            separaDati();

            //Visualizzo il Grafico solo dopo aver ricevuto tutti i pacchetti
            if (pacc_corr == tot_pacc) {

                Grafico.setGrafico(multilineChart, freq);
                Grafico.disegna_grafico(multilineChart, freq, true, valoriCh1, valoriCh2 );
                showInfoHeader();
                grafico_completo = true;
                showNextView(); //Mostro le varie View per poter proseguire
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafico);

        SwipeListener.swipe("avanti", GraficoActivity.this);  //Quando viene creata l'activity viene aperta con uno Swipe in avanti (da sinistra verso destra)

        findView();
        setListener();
        Grafico.setNoDataChart(multilineChart);

        setViewForSwipe(viewRoot); //Trovo tutte le view per decidere su quali si può fare lo Swipe
        Grafico.indice=0;
    }

    private void findView(){
        viewRoot = getWindow().getDecorView(); //Root View
        txtHeader = findViewById(R.id.txtHeader);
        containerNavigazione = findViewById(R.id.containerNavigazione);
        multilineChart = findViewById(R.id.multilineChart);

        opzioni = findViewById(R.id.containerButton);

        btnSalvaFile = findViewById(R.id.btnSalvaFile);
        btnAvanti = findViewById(R.id.btnAvanti);
        btnZoom = findViewById(R.id.btnZoom);
        btnIndietro = findViewById(R.id.btnIndietro);
        btnCondividi = findViewById(R.id.btnCondividi);
        btnEmail = findViewById(R.id.btnEmail);
        btnHome = findViewById(R.id.btnHome);
    }

    private void setListener(){
        btnSalvaFile.setOnClickListener(this);
        btnAvanti.setOnClickListener(this);
        btnZoom.setOnClickListener(this);
        btnIndietro.setOnClickListener(this);
        btnCondividi.setOnClickListener(this);
        btnEmail.setOnClickListener(this);
        btnHome.setOnClickListener(this);
    }

    /* SWIPE */
    private void setViewForSwipe(View v) {
        ViewGroup viewgroup=(ViewGroup)v;

        for (int i=0;i<viewgroup.getChildCount();i++) {
            View v1=viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup) {
                setViewForSwipe(v1);
            }
            // escludo le view sulle quali non si può fare lo swipe
            // Attenzione: le view non sono selezionabili se è consentito lo swipe su di loro
            if (!(v1 instanceof ImageButton) && !(v1 instanceof LineChart)) {
                enableSwipeOnView(v1);
            }
        }
    }

    /* SWIPE */
    @SuppressLint("ClickableViewAccessibility")
    private void enableSwipeOnView(View view){
        view.setOnTouchListener(new SwipeListener(this) {
            public void versoDestra() {
                if(salvato){
                    GraficoActivity.this.finish();
                }else{
                    MessaggiHelper.showDialogPerditaDati(GraficoActivity.this);
                }
            }
            public void versoSinistra(){
                if (grafico_completo) {
                    IntentHelper.apriGraficiSep(GraficoActivity.this, freq, valoriCh1, valoriCh2);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ricevitorePacchetto = new RicevitorePacchetto();
        LocalBroadcastManager.getInstance(this).registerReceiver(ricevitorePacchetto, new IntentFilter(UARTService.BROADCAST_UART_RX));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ricevitorePacchetto);
        super.onStop();
    }

    private void showNextView() {
        containerNavigazione.setVisibility(View.VISIBLE);
        opzioni.setVisibility(View.VISIBLE);
        btnAvanti.setVisibility(View.VISIBLE);
        btnHome.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void showInfoHeader() {
        txtHeader.setText(getString(R.string.infoHeader1) + ((int) multilineChart.getXChartMax() + 1) + getString(R.string.infoHeader2) + freq + getString(R.string.infoHeader3) + getString(R.string.infoHeader4) + guad );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentHelper.getRequestcodeSalvacsv() && resultCode == Activity.RESULT_OK){
            if (data != null && data.getData() != null) {
                salvato = MyUtility.scriviFile(data, getContentResolver(), getContenutoFile(multilineChart), this);
            }
        }
    }

    private String getContenutoFile(LineChart multilineChart) {
        String sep = ";";
        if(!ESTENSIONE.toLowerCase().equals("csv")) {
            sep = "";
        }
        return MyUtility.creaStringa(sep,
                "TIMESTAMP: ", MyUtility.getTimestamp(), "\n",
                "SAMPLES: ", String.valueOf((int) multilineChart.getXChartMax() + 1), "\n",
                "SAMPLING RATE: ", String.valueOf(freq), "\n",
                "GAIN: ", String.valueOf(guad), "\n",
                "CHANNELS: ", String.valueOf(n_canali), "\n",
                "\n",
                "TIME(s);", "CH1(mV);", "CH2(mV);", "\n",
                getValori()
        );
    }

    private String getValori(){
        String valori = "";
        for (int i=0; i<multilineChart.getLineData().getEntryCount()/2; i++){
            valori = valori + MyUtility.creaStringa(";", getValoreX(i), getValYCanale(1, i), getValYCanale(2, i),"\n");
        }
        return valori;
    }

    private String getValoreX(int i){
        return String.valueOf((float)i/(float)freq); //secondi asse X
    }

    private String getValYCanale(int canale, int i){
        return String.valueOf(multilineChart.getData().getDataSetByIndex(canale-1).getEntryForIndex(i).getY());  //"canale-1" perchè l'indice parte da 0
    }

    //Separo i dati dei due Canali e li inserisco in due ArrayList<Entry>
    private void separaDati() {
        int n_record = 0;
        for (int i = 0; i < dati_pacchetto.length; i = i + n_canali) {
            //Valori X e Y
            float x = n_record + (SAMPLE_MAX * (pacc_corr - 1));
            valoriCh1.add(new Entry(x, dati_pacchetto[i])); //ArrayList<Entry>
            valoriCh2.add(new Entry(x, dati_pacchetto[i+1])); //ArrayList<Entry>
            n_record++;
        }
    }

    /* Opzioni */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            //Se l'utente preme indietro senza aver salvato i dati viene visualizzato un messaggio
            case(R.id.btnIndietro):
                if(salvato) {
                    this.finish();
                }else{
                    MessaggiHelper.showDialogPerditaDati(GraficoActivity.this);
                }
                break;
            //Salvo i dati in un File .csv
            case(R.id.btnSalvaFile):
                IntentHelper.salvaCsvIntent(this);
                break;
            //Apro la schermata con i grafici separati, passandogli i dati
            case(R.id.btnAvanti):
                IntentHelper.apriGraficiSep(this, freq, valoriCh1, valoriCh2);
                break;
            //Android mostra le App che possono inviare una email
            case(R.id.btnEmail):
                IntentHelper.emailIntent(this);
                break;
            //Android mostra le App con cui si possono condividere i dati come testo
            case(R.id.btnCondividi):
                IntentHelper.shareIntent(this, getContenutoFile(multilineChart));
                break;
            //Reset dello Zoom
            case(R.id.btnZoom):
                Grafico.resetZoom(multilineChart);
                break;
            case(R.id.btnHome):
                IntentHelper.apriMainActivity(this);
                break;
            default:
                throw new IllegalStateException("Valore sconosciuto: " + v);
        }
    }

    @Override
    public void onBackPressed() {
        if(salvato){
            GraficoActivity.this.finish();
        }else {
            MessaggiHelper.showDialogPerditaDati(GraficoActivity.this);
        }
    }

    @Override
    public void finish() {
        super.finish();
        SwipeListener.swipe("indietro", GraficoActivity.this);
    }
}
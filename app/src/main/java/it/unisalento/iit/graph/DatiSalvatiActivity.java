package it.unisalento.iit.graph;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import it.unisalento.iit.R;
import it.unisalento.iit.utility.IntentHelper;
import it.unisalento.iit.utility.SwipeListener;

public class DatiSalvatiActivity extends AppCompatActivity implements View.OnClickListener{

    private View viewRoot;
    private LineChart chart;
    private ImageButton btnIndietro, btnAvanti;
    private TextView txtInfo;

    int freq;
    String info;
    ArrayList<Entry> val_ch1, val_ch2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dati_salvati);
        SwipeListener.swipe("avanti", this);

        findView();
        setListener();

        //Dati ricevuti
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            getDatiGrafico(extras);

            txtInfo.setText(info);
            Grafico.disegna_grafico(chart, freq, true, val_ch1, val_ch2);

        }

        setViewForSwipe(viewRoot); //Trovo tutte le view per decidere su quali si può fare lo Swipe

    }

    private void findView(){
        viewRoot = getWindow().getDecorView(); //Root View
        txtInfo = findViewById(R.id.txtInfo);
        btnIndietro = findViewById(R.id.btnIndietro);
        chart = findViewById(R.id.chart);
        btnAvanti = findViewById(R.id.btnAvanti);
    }

    private void setListener(){
        btnIndietro.setOnClickListener(this);
        btnAvanti.setOnClickListener(this);
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
                IntentHelper.apriMainActivity(DatiSalvatiActivity.this);
                SwipeListener.swipe("indietro", DatiSalvatiActivity.this);
             }
            public void versoSinistra(){
                IntentHelper.apriGraficiSep(DatiSalvatiActivity.this, freq, val_ch1, val_ch2);
            }
        });
    }

    private void getDatiGrafico(Bundle extras){
        val_ch1 = (ArrayList<Entry>)extras.get("Canale1");
        val_ch2 = (ArrayList<Entry>)extras.get("Canale2");
        freq = (int)extras.get("freq");
        info = (String)extras.get("info");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case(R.id.btnIndietro):
                IntentHelper.apriMainActivity(this);
                SwipeListener.swipe("indietro", this);
                break;
            case(R.id.btnAvanti):
                IntentHelper.apriGraficiSep(this, freq, val_ch1, val_ch2);
                SwipeListener.swipe("avanti", this);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        IntentHelper.apriMainActivity(this);
        SwipeListener.swipe("indietro", this);
    }

}
package it.unisalento.iit.graph;

import android.annotation.SuppressLint;


import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;

import java.util.ArrayList;

import it.unisalento.iit.R;
import it.unisalento.iit.utility.IntentHelper;
import it.unisalento.iit.utility.MessaggiHelper;
import it.unisalento.iit.utility.SwipeListener;


public class GraficiSeparati extends AppCompatActivity implements View.OnClickListener {

    int freq;
    ImageButton btnIndietro, btnHome;
    LineChart chart1, chart2;
    ArrayList<Entry> ch1, ch2;
    View viewRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafici_separati);
        SwipeListener.swipe("avanti", GraficiSeparati.this);

        findView();
        setListener();

        setViewForSwipe(viewRoot);

        //Dati ricevuti dal Grafico Unico
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            getDatiGrafico(extras);

            Grafico.disegna_grafico(chart1, freq, true, ch1);
            Grafico.disegna_grafico(chart2, freq, false, ch2);
        }
    }

    private void findView(){
        viewRoot = getWindow().getDecorView(); //Root View
        btnIndietro = findViewById(R.id.btnIndietro);
        chart1 = findViewById(R.id.chart1);
        chart2 = findViewById(R.id.chart2);
        btnHome = findViewById(R.id.btnHome);
    }

    private void setListener(){
        btnIndietro.setOnClickListener(this);
        btnHome.setOnClickListener(this);
    }


    private void setViewForSwipe(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i=0;i<viewgroup.getChildCount();i++) {
            View v1=viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup) {setViewForSwipe(v1);}
            if (!(v1 instanceof ImageButton) && !(v1 instanceof LineChart)) {
                enableSwipeOnView(v1);
            }
        }
    }

    private void getDatiGrafico(Bundle extras){
        ch1 = (ArrayList<Entry>)extras.get("Canale1");
        ch2 = (ArrayList<Entry>)extras.get("Canale2");
        freq = (int)extras.get("freq");
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case(R.id.btnIndietro):
                finish();
                break;
            case(R.id.btnHome):
                IntentHelper.apriMainActivity(this);
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        SwipeListener.swipe("indietro", GraficiSeparati.this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableSwipeOnView(View view){
        view.setOnTouchListener(new SwipeListener(this) {
            public void versoDestra() {
                finish();
            }
            public void versoSinistra() {
                MessaggiHelper.showToast(getApplicationContext(), getString(R.string.ultimaActivity));
            }
        });
    }

}
package it.unisalento.iit.uart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;


import it.unisalento.iit.R;
import it.unisalento.iit.utility.IntentHelper;
import it.unisalento.iit.utility.MessaggiHelper;
import it.unisalento.iit.utility.MyUtility;
import it.unisalento.iit.utility.SwipeListener;
import it.unisalento.iit.profile.BleProfileService;

public class ComandoActivity extends AppCompatActivity implements View.OnClickListener {

    private UARTInterface uartInterface; //interfaccia per inviare dati al target

    private final String SEPARATORE = " ";

    private static final String DEFAULT_SAMPLE = "201" ;
    private static final String DEFAULT_FREQ = "100";
    private static final String DEFAULT_GAIN = "1";

    private Button btnStart;
    private ImageButton btnIndietro, btnAvanti, btnHome;
    private Spinner n_campioniSpinner, freqSpinner, guadagnoSpinner;
    private ArrayAdapter arrayAdapterSpinner;
    private View viewRoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comando);
        //Registrazione BroadcastReceiver "commonBroadcastReceiver" con azione "BleProfileService.BROADCAST_CONNECTION_STATE"
        LocalBroadcastManager.getInstance(this).registerReceiver(commonBroadcastReceiver, makeIntentFilter());

        //Apro questa activity con effetto swipe
        SwipeListener.swipe("avanti", ComandoActivity.this);

        //Imposto l'interfaccia
        findView();
        setListener();

        //Spinner Samples
        setSpinner(n_campioniSpinner, R.layout.spinner_item, getResources().getStringArray(R.array.Sample_array), android.R.layout.simple_spinner_dropdown_item, DEFAULT_SAMPLE);

        //Spinner Freq
        setSpinner(freqSpinner, R.layout.spinner_item, getResources().getStringArray(R.array.Freq_array), android.R.layout.simple_spinner_dropdown_item, DEFAULT_FREQ);

        //Spinner Gain
        setSpinner(guadagnoSpinner, R.layout.spinner_item, getResources().getStringArray(R.array.Gain_array), android.R.layout.simple_spinner_dropdown_item, DEFAULT_GAIN);

        setViewForSwipe(viewRoot);
    }

    private void findView(){
        viewRoot = getWindow().getDecorView(); //Root View

        btnIndietro = findViewById(R.id.btnIndietro);
        btnAvanti = findViewById(R.id.btnAvanti);
        btnStart = findViewById(R.id.btnStart);
        btnHome = findViewById(R.id.btnHome);
        n_campioniSpinner = findViewById(R.id.spinnerSample);
        freqSpinner = findViewById(R.id.spinnerFreq);
        guadagnoSpinner = findViewById(R.id.spinnerGain);
    }

    private void setListener(){
        btnIndietro.setOnClickListener(this);
        btnAvanti.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnHome.setOnClickListener(this);
    }

    private void setSpinner(Spinner spinner, int layoutSpinner, String[] valori, int layoutDropDown , String val_default){
        arrayAdapterSpinner = new ArrayAdapter(this, layoutSpinner, valori);
        arrayAdapterSpinner.setDropDownViewResource(layoutDropDown);

        spinner.setAdapter(arrayAdapterSpinner);
        spinner.setSelection(arrayAdapterSpinner.getPosition(val_default));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /* SWIPE */
    private void setViewForSwipe(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i=0;i<viewgroup.getChildCount();i++) {
            View v1=viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup) {setViewForSwipe(v1);}
            if (!(v1 instanceof ImageButton) && !(v1 instanceof Button) && !(v1 instanceof Spinner)) {
                enableSwipeOnView(v1);
            }
        }
    }

    /* SWIPE */
    @SuppressLint("ClickableViewAccessibility")
    private void enableSwipeOnView(View view){
        view.setOnTouchListener(new SwipeListener(this) {
            public void versoDestra() {
                IntentHelper.apriConnectActivity(ComandoActivity.this);
                SwipeListener.swipe("indietro", ComandoActivity.this);
            }
            public void versoSinistra(){
                if (btnStart.isEnabled()){
                    MessaggiHelper.showToast(getApplicationContext(), getString(R.string.clickOnStart));
                } else {
                    MessaggiHelper.showToast(getApplicationContext(), getString(R.string.clickConnect));
                }
            }
        });
    }

    /**
     * The receiver that listens for {@link BleProfileService#BROADCAST_CONNECTION_STATE} action.
     */
    private final BroadcastReceiver commonBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            // This receiver listens only for the BleProfileService.BROADCAST_CONNECTION_STATE action, no need to check it.
            final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);

            switch (state) {
                case BleProfileService.STATE_CONNECTED: {
                    onDeviceConnected();
                    break;
                }
                case BleProfileService.STATE_DISCONNECTED: {
                    onDeviceDisconnected();
                    break;
                }
                case BleProfileService.STATE_CONNECTING:
                case BleProfileService.STATE_DISCONNECTING:
                default:
                    // there should be no other actions
                    break;
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            final UARTService.UARTBinder bleService = (UARTService.UARTBinder) service;
            uartInterface = bleService;

            // and notify user if device is connected
            if (bleService.isConnected())
                onDeviceConnected();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            onDeviceDisconnected();
            uartInterface = null;
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(commonBroadcastReceiver);
        super.onDestroy();
    }

    //Abilita il pulsante Start quando il Dispositivo si è connesso
    protected void onDeviceConnected() { //Dispositivo Connesso
        btnStart.setEnabled(true);
    }

    //Disabilita il pulsante Start quando il Dispositivo è Disconnesso
    protected void onDeviceDisconnected() {
        btnStart.setEnabled(false);
    }

    //Azione per il Broadcast Receiver
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
         * If the service has not been started before the following lines will not start it. However, if it's running, the Activity will be bound to it
         * and notified via serviceConnection.
         */
        final Intent service = new Intent(this, UARTService.class);
        bindService(service, serviceConnection, 0); // we pass 0 as a flag so the service will not be created if not exists
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            unbindService(serviceConnection);
            uartInterface = null;
        } catch (final IllegalArgumentException e) {
            // do nothing, we were not connected to the sensor
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            //Torna su ConnectActivity (con effetto swipe)
            case(R.id.btnIndietro):
                IntentHelper.apriConnectActivity(this);
                SwipeListener.swipe("indietro", ComandoActivity.this);
                break;

            //Apre GraficoActivity (con effetto swipe)
            case(R.id.btnAvanti):
                IntentHelper.apriGraficoActivity(this);
                SwipeListener.swipe("avanti", ComandoActivity.this);
                break;

            //Crea il Comando da inviare al dispositivo, indicando il tempo stimato se è superiore ai 2 secondi e apre GraficoActivity (con effetto swipe)
            case(R.id.btnStart):
                final String comando = MyUtility.creaStringa(SEPARATORE, "Start", getValSpinner(n_campioniSpinner), getValSpinner(freqSpinner), getValSpinner(guadagnoSpinner));

                uartInterface.send(comando);

                int tot_campioni = Integer.parseInt(getValSpinner(n_campioniSpinner));
                long freq_camp = Long.parseLong(getValSpinner(freqSpinner));
                long tempo_stimato = getTempoStimato(tot_campioni, freq_camp);

                if (tempo_stimato >= 2) {
                    MessaggiHelper.showToastTempo(getApplicationContext(), tempo_stimato);
                }

                IntentHelper.apriGraficoActivity(ComandoActivity.this);
                SwipeListener.swipe("avanti", ComandoActivity.this);
                break;
            case(R.id.btnHome):
                IntentHelper.apriMainActivity(this);
        }
    }

    //Calcola il tempo stimato per ricevere i dati richiesti e visualizzare il grafico
    private long getTempoStimato(int tot_campioni, long freq_camp){ return tot_campioni/freq_camp; }

    //Restituisce il valore selezionato nello Spinner
    private String getValSpinner(Spinner spinner){
        return spinner.getSelectedItem().toString();
    }

    //Torna su ConnectActivity (con effetto swipe)
    @Override
    public void onBackPressed() {
        IntentHelper.apriConnectActivity(this);
        SwipeListener.swipe("indietro", ComandoActivity.this);
    }

    //Torna su ConnectActivity (con effetto swipe)
    @Override
    public void finish() {
        super.finish();
        SwipeListener.swipe("indietro", ComandoActivity.this);
    }

}
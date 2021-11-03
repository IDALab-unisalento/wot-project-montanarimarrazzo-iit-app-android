package it.unisalento.iit.uart;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import java.util.UUID;

import it.unisalento.iit.utility.IntentHelper;
import it.unisalento.iit.utility.MessaggiHelper;
import it.unisalento.iit.utility.SwipeListener;
import it.unisalento.iit.profile.BleProfileService;
import it.unisalento.iit.profile.BleProfileServiceReadyActivity;
import it.unisalento.iit.R;

public class ConnectActivity extends BleProfileServiceReadyActivity<UARTService.UARTBinder> implements UARTInterface, View.OnClickListener {

    private boolean prima_volta = true;
    private View viewRoot;
    private Button btnProsegui;
    private ImageButton btnConnect;
    private ImageButton btnIndietro, btnAvanti;

    @Override
    protected void onCreateView(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_connect);
        findView();
        setListener();

        setViewForSwipe(viewRoot);
    }

    private void findView(){
        viewRoot = getWindow().getDecorView(); //Root View
        btnIndietro = findViewById(R.id.btnIndietro);
        btnAvanti = findViewById(R.id.btnAvanti);
        btnConnect = findViewById(R.id.btnConnect);
        btnProsegui = findViewById(R.id.btnProsegui);
    }

    private void setListener(){
        btnIndietro.setOnClickListener(this);
        btnAvanti.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        btnProsegui.setOnClickListener(this);
    }

    /* SWIPE */
    private void setViewForSwipe(View v) {
        ViewGroup viewgroup=(ViewGroup)v;

        for (int i=0;i<viewgroup.getChildCount();i++) {
            View v1=viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup) {setViewForSwipe(v1);}
            if (!(v1 instanceof ImageButton) && !(v1 instanceof Button)) {
                enableSwipeOnView(v1);
            }
        }
    }

    /* SWIPE */
    @SuppressLint("ClickableViewAccessibility")
    private void enableSwipeOnView(View view){
        view.setOnTouchListener(new SwipeListener(this) {
           public void versoDestra() {
               IntentHelper.apriMainActivity(ConnectActivity.this);
               SwipeListener.swipe("indietro", ConnectActivity.this);
           }
           public void versoSinistra(){
               if(isDeviceConnected()) {
                   IntentHelper.apriComandoActivity(ConnectActivity.this);
               }else{
                   MessaggiHelper.showDialogConnect(ConnectActivity.this);
               }
           }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(prima_volta) {
            controllaGps();
            prima_volta=false;
        }
    }

    private void controllaGps(){
        //Controllo se il Gps è attivo e in caso richiedo di accenderlo
        LocationManager locationmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!(locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER))){
            // Dialog GPS
            MessaggiHelper.showDialogGps(ConnectActivity.this);
        }
    }


    @Override
    protected int getDefaultDeviceName() {
        return R.string.nome_dispositivo;
    }

    //Restituisce l'UUID del servizio che devono avere i dispositivi scansionati
    @Override
    protected UUID getFilterUUID() {
        return UARTManager.getUartServiceUuid();
        // return null;  //per visualizzare tutti i dispositivi nelle vicinanze
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case(R.id.btnConnect):
                if (isBLEEnabled()) {
                    if (service == null) {
                        setDefaultUI();
                        showDeviceScanningDialog(getFilterUUID()); //null se vogio tutti i dispositivi nelle vicinanze
                    } else {
                        service.disconnect();
                    }
                } else {
                    showBLEDialog();
                }
                break;

            case(R.id.btnAvanti):
                if(isDeviceConnected()){
                    IntentHelper.apriComandoActivity(ConnectActivity.this);
                    SwipeListener.swipe("avanti", ConnectActivity.this);
                }else{
                    MessaggiHelper.showDialogConnect(ConnectActivity.this);
                }
                break;
            case(R.id.btnIndietro):
                IntentHelper.apriMainActivity(this);
                break;
            case(R.id.btnProsegui):
                IntentHelper.apriComandoActivity(this);
                SwipeListener.swipe("avanti", ConnectActivity.this);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        IntentHelper.apriMainActivity(this);
        SwipeListener.swipe("indietro", ConnectActivity.this);
    }

    /* Codice nRF Toolbox */
    private UARTService.UARTBinder serviceBinder;

    @Override
    protected Class<? extends BleProfileService> getServiceClass() {
        return UARTService.class;
    }

    @Override
    protected void setDefaultUI() {}

    @Override
    protected void onServiceBound(final UARTService.UARTBinder binder) {
        serviceBinder = binder;
    }

    @Override
    protected void onServiceUnbound() {
        serviceBinder = null;
    }

    @Override
    public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
    }

    @Override
    public void onDeviceSelected(@NonNull final BluetoothDevice device, final String name) {
        // The super method starts the service
        super.onDeviceSelected(device, name); // BleProfileServiceReadyActivity
    }

    @Override
    public void send(final String text) {
        if (serviceBinder != null)
            serviceBinder.send(text); //UARTService
    }

}

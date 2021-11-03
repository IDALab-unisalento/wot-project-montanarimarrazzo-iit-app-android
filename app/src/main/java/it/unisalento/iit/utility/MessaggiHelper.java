package it.unisalento.iit.utility;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import it.unisalento.iit.R;
import it.unisalento.iit.uart.ConnectActivity;

public class MessaggiHelper {

    /*Vari Toast*/
    public static void showToastTempo(Context contesto, Long tempo) {
        Toast.makeText(contesto, "Tempo stimato: " + tempo + " secondi", Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context contesto, String messaggio) {
        Toast.makeText(contesto, messaggio, Toast.LENGTH_SHORT).show();
    }
    
    /*Vari Dialog*/
    public static void showDialogGps(Activity contesto) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(contesto);
        builder.setTitle(R.string.gps_obbligatorio_title);
        builder.setMessage(R.string.gps_obbligatorio);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.si, (dialog, id) -> IntentHelper.apriImpostazioniGps(contesto));
        builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialogExit(Activity contesto) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(contesto);
        builder.setTitle(R.string.titleExit);
        builder.setMessage(R.string.messageExit);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.si, (dialog, id) -> {
            contesto.finishAffinity();
            System.exit(0);
        });
        builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());

        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialogPerditaDati(Activity contesto) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(contesto);
        builder.setTitle(R.string.titlePerditaDati);
        builder.setMessage(R.string.messagePerditaDati);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.si, (dialog, id) -> IntentHelper.salvaCsvIntent(contesto));
        builder.setNegativeButton(R.string.no, (dialog, id) -> contesto.finish());

        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showDialogConnect(ConnectActivity contesto) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(contesto);
        builder.setTitle(R.string.titleDispNoConn).setMessage(R.string.messageDispNoConn).setCancelable(true);

        final AlertDialog alert = builder.create();
        alert.show();
    }

}

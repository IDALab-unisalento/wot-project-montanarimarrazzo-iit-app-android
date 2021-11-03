package it.unisalento.iit.utility;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import it.unisalento.iit.graph.DatiSalvatiActivity;
import it.unisalento.iit.MainActivity;
import it.unisalento.iit.graph.GraficiSeparati;
import it.unisalento.iit.graph.GraficoActivity;
import it.unisalento.iit.uart.ComandoActivity;
import it.unisalento.iit.uart.ConnectActivity;

public class IntentHelper {

    private static int REQUESTCODE_SALVACSV = 1;
    private static int REQUESTCODE_APRICSV = 55;

    public static int getRequestcodeSalvacsv() {
        return REQUESTCODE_SALVACSV;
    }

    public static int getRequestcodeApricsv() {
        return REQUESTCODE_APRICSV;
    }

    public static void emailIntent(Activity activity){
        /* SCRIVI EMAIL */
        Intent intent_email = new Intent(Intent.ACTION_SENDTO);
        intent_email.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
        intent_email.setData(Uri.parse("mailto:"));
        activity.startActivity(Intent.createChooser(intent_email, "Invia Email"));
    }

    public static void salvaCsvIntent(Activity activity){
        Intent intentSalvaFile;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intentSalvaFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intentSalvaFile.addCategory(Intent.CATEGORY_OPENABLE);
            intentSalvaFile.setType("text/csv");
            intentSalvaFile.putExtra(Intent.EXTRA_TEXT, "File.csv");
            activity.startActivityForResult(intentSalvaFile, REQUESTCODE_SALVACSV);
        }else{
            MessaggiHelper.showToast(activity, "Opzione non disponibile per dispositivi Android precedenti alla 4.4 (KITKAT)");
        }
    }

    public static void apriCsv(Activity activity){
        Intent intentApriCsv = new Intent(Intent.ACTION_GET_CONTENT);
        intentApriCsv.addCategory(Intent.CATEGORY_OPENABLE);
        intentApriCsv.setType("text/*");
        activity.startActivityForResult(intentApriCsv, REQUESTCODE_APRICSV);
    }

    public static void shareIntent(Activity activity, String contenuto) {
        Intent intent_share = new Intent(android.content.Intent.ACTION_SEND);
        intent_share.setType("text/plain");
        intent_share.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
        intent_share.putExtra(android.content.Intent.EXTRA_TEXT, contenuto);
        activity.startActivity(Intent.createChooser(intent_share, "Condividi con:"));
    }

    public static void apriMainActivity(Activity activity) {
        Intent newIntent = new Intent(activity, MainActivity.class);
        activity.startActivity(newIntent);
    }

    public static void apriConnectActivity(Activity activity) {
        Intent newIntent = new Intent(activity, ConnectActivity.class);
        activity.startActivity(newIntent);
    }

    public static void apriComandoActivity(Activity activity) {
        Intent intentAvanti = new Intent(activity, ComandoActivity.class);
        activity.startActivity(intentAvanti);
    }

    public static void apriGraficoActivity(Activity activity) {
        Intent apriActivtyGrafico = new Intent(activity, GraficoActivity.class);
        activity.startActivity(apriActivtyGrafico);
    }

    public static void apriGraficiSep(Activity activity, int freq, ArrayList<Entry> valoriCh1, ArrayList<Entry> valoriCh2){
        Intent separaGrafici = new Intent(activity, GraficiSeparati.class);
        separaGrafici.putExtra("Canale1", valoriCh1);
        separaGrafici.putExtra("Canale2", valoriCh2);
        separaGrafici.putExtra("freq", freq);
        activity.startActivity(separaGrafici);
    }

    public static void apriImpostazioniGps(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivity(intent);
    }

    public static void apriDatiSalvatiActivity(Activity activity, String info, int freq, ArrayList<Entry>... valoriCanale) {
        Intent datiSalvati = new Intent(activity, DatiSalvatiActivity.class);
        datiSalvati.putExtra("info", info);
        int count = 0;
        for(ArrayList<Entry> val: valoriCanale) {
            datiSalvati.putExtra("Canale"+ (count+1) , val);
            count++;
        }
        datiSalvati.putExtra("freq", freq);
        activity.startActivity(datiSalvati);
    }

}

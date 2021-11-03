package it.unisalento.iit.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

public class MyUtility {

    public static String getTimestamp(){
        return java.text.DateFormat.getDateTimeInstance().format(new Date()); //formato: gg/mm/aaaa  hh:mm:ss
    }

    public static String creaStringa(String sep, String... valori){
        StringBuilder mystringa = new StringBuilder();
        for(String x: valori) {
            if (x.equals("\n")) {
                mystringa.append(x); //se è il carattere ritorno a capo non aggiunge il separatore
            }else {
                mystringa.append(x).append(sep);
            }
        }
        return mystringa.toString();
    }


    public static boolean scriviFile(Intent data, ContentResolver contentResolver, String contenuto, Context contesto) {
        OutputStream os;
        try {
            os = contentResolver.openOutputStream(data.getData());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(contenuto);
            bw.flush();
            bw.close();
            MessaggiHelper.showToast(contesto, "File salvato");
            return true; //salvato
        } catch (IOException e) {
            MessaggiHelper.showToast(contesto, "ERRORE: File non salvato");
            e.printStackTrace();
            return false;
        }
    }
}

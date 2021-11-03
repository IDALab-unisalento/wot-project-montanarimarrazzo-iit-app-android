package it.unisalento.iit.utility;

public class DatiHelper {
    private static final int LUNG_HEADER = 8;

    public static String[] estraiHeader(String pacchetto){
        String[] header_temp = ((pacchetto.substring(5, 5+LUNG_HEADER*3-1)).split("-")); //dal carattere 6
        return header_temp;
    }

    public static int[] convHeaderHex2Dec(String[] header_temp){
        int[] header = new int[header_temp.length-1];
        for(int i=0; i<header.length; i++){
            if(i!=header.length-1){
                header[i] = Integer.parseInt(header_temp[i], 16);
            }else{
                header[i] = Integer.parseInt(header_temp[i]+header_temp[i+1], 16); //ultimi due Byte dell'header sono la frequenza
            }
        }
        return header;
    }

    public static String[] estraiDati(String pacchetto){
        String[] dati_temp = ((pacchetto.substring(5+LUNG_HEADER*3)).split("-"));
        return dati_temp;
    }

    public static int[] convDatiHex2Dec(String[] dati_temp){
        //Converto i valori in Decimale
        int[] dati_pacchetto = new int[dati_temp.length/2];
        for(int i=0; i<dati_pacchetto.length; i++){
            dati_pacchetto[i] = Integer.parseInt(dati_temp[i*2]+dati_temp[(i*2)+1], 16);
        }
        return dati_pacchetto;
    }

    public static void inverti_Byte(String[] array, int inizio) {
        //Inverti Byte (posizione dispari con quelli in posizione pari)
        String temp = "";
        for (int j = inizio; j < array.length-1; j += 2) {
            temp = array[j];
            array[j] = array[j+1];
            array[j+1] = temp;
        }
    }
}

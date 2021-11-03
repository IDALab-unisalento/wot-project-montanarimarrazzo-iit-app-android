package it.unisalento.iit.graph;

import android.graphics.Color;
import android.graphics.Typeface;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class Grafico {

    static XAxis asse_x;
    static YAxis asse_ys;
    static YAxis asse_yd;
    static int[] COLORI = new int[]{Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.BLACK};
    static int indice = 0;

    static void setNoDataChart(LineChart multilineChart){
        multilineChart.setNoDataText("ATTENDERE: raccolta dati in corso…");
        multilineChart.setNoDataTextColor(Color.RED);
        multilineChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
    }

    @SafeVarargs
    public static void disegna_grafico(LineChart chart, long freq, boolean resetColore, ArrayList<Entry>... canali){

        ArrayList<ILineDataSet> array_LDataSet = new ArrayList<>(); //ArrayList contentente tutti i valori di tutti i canali

        if(resetColore){
            indice = 0;
        }

        for(ArrayList<Entry> canale: canali) {
            LineDataSet lineDataSet = new LineDataSet(canale, "Canale"+ (indice+1));
            array_LDataSet.add(lineDataSet);
            lineDataSet.setColor(COLORI[indice]);
            lineDataSet.setDrawCircles(false);
            indice++;
        }

        LineData lineData = new LineData(array_LDataSet); //Valori da graficare (tutti i canali)

        Grafico.setAssiXY(chart, freq);

        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.invalidate();

        lineData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.moveViewToX(lineData.getEntryCount());    // mi sposto all'ultimo punto
    }

    static void setGrafico(LineChart multilineChart, long freq){

        setAsseX(multilineChart, freq);
        setAsseY(multilineChart);

        multilineChart.setDrawBorders(false);
        multilineChart.getDescription().setEnabled(false); //rimuove la scritta "Description Label" in fondo al grafico
    }

    private static void setAsseX(LineChart multilineChart, long freq){
        asse_x = multilineChart.getXAxis();
        asse_x.setDrawGridLines(true);
        asse_x.setAvoidFirstLastClipping(true);
        asse_x.setEnabled(true);
        asse_x.setPosition(XAxis.XAxisPosition.BOTTOM);

        //Indico sull'Asse X il tempo
        converti_ntosec(multilineChart, freq);
    }

    private static void setAsseY(LineChart multilineChart){
        asse_ys = multilineChart.getAxisLeft();
        asse_ys.setDrawGridLines(true);
        asse_yd = multilineChart.getAxisRight();
        asse_yd.setEnabled(false);
    }

    static void setAssiXY(LineChart chart, long freq){
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        converti_ntosec(chart, freq);
    }

    private static void converti_ntosec(LineChart chart, long freq){
        chart.getXAxis().setValueFormatter((n_campione, axis) -> (String.valueOf(n_campione/freq)));
    }

    static void resetZoom(LineChart multilineChart){
        multilineChart.fitScreen();
    }

}

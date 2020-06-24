package cz.utb.thesisapp.ui.touch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.utb.thesisapp.Event;
import cz.utb.thesisapp.MainActivity;
import cz.utb.thesisapp.R;
import cz.utb.thesisapp.ui.info.InfoViewModel;

public class TouchFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private InfoViewModel infoViewModel;
    private TouchViewModel touchViewModel;
    MyDrawingView dvThisApp;
    MyDrawingView dvPairedApp;
    private Paint mPaint;
    View root;
    private LineChart mChart;
    List yValues;
    //    volatile ArrayList<Entry> yValues;
    Date startTime;
    volatile LineDataSet set1;
    volatile ArrayList<ILineDataSet> dataSets;
    volatile LineData data;
    volatile int dataSize = 1;
    private Thread thread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        touchViewModel =
                ViewModelProviders.of(this).get(TouchViewModel.class);
        try {
            infoViewModel =
                    ViewModelProviders.of(getActivity()).get(InfoViewModel.class);
        } catch (Exception e) {

        }
        root = inflater.inflate(R.layout.fragment_touch, container, false);
//        final TextView textView = root.findViewById(R.id.text_gallery);
//        touchViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        Log.i(TAG, "~~onCreateView");
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            dvThisApp = (MyDrawingView) root.findViewById(R.id.scratch_pad);
//            dvThisApp.setTextView(((TextView) root.findViewById(R.id.tvTouchInfo)));
            dvThisApp.act = act;
        }

        if (act instanceof MainActivity) {
            dvPairedApp = (MyDrawingView) root.findViewById(R.id.scratch_pad_partner);
            dvPairedApp.setThisApp(false);
            int color = ContextCompat.getColor(act, R.color.colorPrimary);
            dvPairedApp.paint.setColor(color);
            dvPairedApp.act = act;

            LocalBroadcastManager.getInstance(act).registerReceiver(mReceiver, new IntentFilter("touch"));
            LocalBroadcastManager.getInstance(act).registerReceiver(maReiver, new IntentFilter("MainActivity"));
        }
        mChart = (LineChart) root.findViewById(R.id.chartFT);
//        ArrayList<Entry> yValues;
        yValues = Collections.synchronizedList(new ArrayList<Entry>());
        yValues.add(new Entry(0, 0));
//        LineDataSet set1;
        set1 = new LineDataSet(yValues, "Delay");
        set1.setFillAlpha(110);
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.RED);
//        ArrayList<ILineDataSet> dataSets;
        dataSets = new ArrayList<>();
        dataSets.add(set1);
//        LineData data;
        data = new LineData(dataSets);

        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setEnabled(false);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawBorders(false);
        Description description = new Description();
        description.setText("");

        mChart.setDescription(description);
        mChart.setData(data);

        startTime = new Date(System.currentTimeMillis());

        return root;
    }

    public void startPlot() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        plot();
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void addDataToChart(long difference) {
        final float y = (float) difference;
        update(y);
    }

    private synchronized void plot() {
        set1.notifyDataSetChanged();
        data.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private synchronized void update(float y) {
        synchronized (yValues) {
            yValues.add(new Entry(dataSize, y));
            dataSize++;
            if (yValues.size() > 30) {
                yValues.remove(0);
            }
        }
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            synchronized (infoViewModel.getDelays()) {
                Entry a = new Entry(getTime(), y);
                infoViewModel.delayAddEntry(a);
            }
        }
    }

    private float getTime() {
        long millis = System.currentTimeMillis();
        long millisWithoutDays = millis - TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(millis));
        String sb1 = Long.toString(millisWithoutDays);
        sb1 = sb1.substring(1);
        return Float.valueOf(sb1)/1000;
    }

    private void countTime(float x, float y) {
        Float z = x + y;
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            try {
                Date previousDate = ((MainActivity) act).operation.get(z.hashCode());
                Date thisTime = new Date(System.currentTimeMillis());
                long seconds = (thisTime.getTime() - previousDate.getTime());
                ((MainActivity) act).operation.remove(z.hashCode());
                Log.d(TAG, "onReceive: ~~difference " + seconds);
                addDataToChart(seconds);
            } catch (Exception e) {

            }
        }
    }

    private final BroadcastReceiver maReiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            if (i.hasExtra("refresh")) {
                dvPairedApp.clear();
                dvThisApp.clear();
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent i) {
            if (i.hasExtra("TouchStart")) {
                countTime(i.getFloatExtra("x", 0), i.getFloatExtra("y", 0));
                dvPairedApp.remoteTouchEvent("TouchStart",
                        i.getFloatExtra("x", 0),
                        i.getFloatExtra("y", 0));
            }
            if (i.hasExtra("TouchMove")) {
                countTime(i.getFloatExtra("x", 0), i.getFloatExtra("y", 0));
                dvPairedApp.remoteTouchEvent("TouchMove",
                        i.getFloatExtra("x", 0),
                        i.getFloatExtra("y", 0));
            }
            if (i.hasExtra("ScreenSize")) {
            }
            if (i.hasExtra("CleanCanvas")) {
            }
            if (i.hasExtra("TouchUp")) {
                dvPairedApp.remoteTouchEvent("TouchUp", 0, 0);
            }
            if (i.hasExtra("TouchTolerance")) {
            } else {               // Do something else
            }

        }
    };

    @Override
    public void onResume() {
        startPlot();
        super.onResume();
        Log.d(TAG, "onResume: ~~");
    }

    @Override
    public void onPause() {
        if (thread != null) {
            thread.interrupt();
        }
        super.onPause();
        Log.d(TAG, "onPause: ~~");
    }

    @Override
    public void onStop() {
        if (thread != null) {
            thread.interrupt();
        }
        super.onStop();
        Log.d(TAG, "onStop: ~~");
    }
}

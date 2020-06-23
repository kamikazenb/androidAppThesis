package cz.utb.thesisapp.ui.info;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import com.github.mikephil.charting.data.Entry;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.utb.thesisapp.MainActivity;
import cz.utb.thesisapp.R;

public class InfoFragment extends Fragment {

    private InfoViewModel infoViewModel;
    private static final String TAG = "InfoFragment";
    View root;
    volatile LineDataSet setDelays;
    volatile LineDataSet setDownloads;
    volatile LineDataSet setUploads;
    volatile ArrayList<ILineDataSet> dataSets;
    volatile LineData data;
    private LineChart mChart;
    private Thread thread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        infoViewModel =
                ViewModelProviders.of(this).get(InfoViewModel.class);
        root = inflater.inflate(R.layout.fragment_info, container, false);
//        final TextView textView = root.findViewById(R.id.text_slideshow);
//        infoViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        ImageButton ib = root.findViewById(R.id.ibFI_speedRequest);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity act = getActivity();
                if (act instanceof MainActivity) {
                    Animation ranim = (Animation) AnimationUtils.loadAnimation(act, R.anim.rotate);
                    ((ImageButton) root.findViewById(R.id.ibFI_speedRequest)).startAnimation(ranim);
                    ((MainActivity) act).startDownloadTest();
                    ((ImageButton) root.findViewById(R.id.ibFI_speedRequest)).setEnabled(false);
                    ((TextView) root.findViewById(R.id.tvFI_internetSpeedDownload)).setText("");
                    ((TextView) root.findViewById(R.id.tvFI_internetSpeedUpload)).setText("");

                }
            }
        });
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            LocalBroadcastManager.getInstance(act).registerReceiver(mReceiver, new IntentFilter("info"));
        }

        mChart = (LineChart) root.findViewById(R.id.chartFI);
        if (act instanceof MainActivity) {
            setDelays = new LineDataSet(((MainActivity) act).delays, "Delay");
            setDownloads = new LineDataSet(((MainActivity) act).download, "Download MB/s *100");
            setUploads = new LineDataSet(((MainActivity) act).upload, "Upload MB/s *100");
            //        setDelays.setFillAlpha(110);
            setDelays.setColor(Color.RED);
            setDelays.setCircleColor(Color.RED);
            setDownloads.setColor(Color.GREEN);
            setDownloads.setCircleColor(Color.GREEN);
            setUploads.setColor(Color.BLUE);
            setUploads.setCircleColor(Color.BLUE);
            dataSets = new ArrayList<>();
            dataSets.add(setDelays);
            dataSets.add(setUploads);
            dataSets.add(setDownloads);
            try {
                data = new LineData(dataSets);
            } catch (Exception e) {
                Log.d(TAG, "onCreateView: ~~" + e);
            }
//            mChart.getAxisRight().setDrawGridLines(false);
//            mChart.getAxisLeft().setDrawGridLines(false);
//            mChart.getXAxis().setDrawGridLines(false);
//            mChart.getXAxis().setEnabled(false);
//            mChart.getDescription().setEnabled(false);
//            mChart.setDrawBorders(false);
            Description description = new Description();
            description.setText("");
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);
//            mChart.setScaleEnabled(true);
            mChart.setScaleXEnabled(true);
            mChart.setScaleYEnabled(true);
            mChart.setPinchZoom(false);
            mChart.setDescription(description);
            mChart.setData(data);
        }
        Log.d(TAG, "onCreateView: ~~");
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
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    private float getTime() {
        long millis = System.currentTimeMillis();
        long millisWithoutDays = millis - TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(millis));
        String sb1 = Long.toString(millisWithoutDays);
        sb1 = sb1.substring(1);
        return Float.valueOf(sb1);
    }

    private synchronized void plot() {
        setUploads.notifyDataSetChanged();
        setDownloads.notifyDataSetChanged();
        setDelays.notifyDataSetChanged();
        data.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Activity act = getActivity();

            if (intent.hasExtra("DOWNLOAD")) {
                if (intent.getIntExtra("progress", 0) == 101) {
                    if (act instanceof MainActivity) {
                        Entry entry = new Entry(getTime(),
                                (intent.getFloatExtra("DOWNLOAD", 0))*100);
                        ((MainActivity) act).download.add(entry);
                    }
                } else {
                    ((ProgressBar) root.findViewById(R.id.progressBarFI_internetSpeed))
                            .setProgress(intent.getIntExtra("progress", 0));
                }
                String text = String.valueOf(intent.getFloatExtra("DOWNLOAD", 0)) + "MB/s";
                ((TextView) root.findViewById(R.id.tvFI_internetSpeedDownload)).setText(text);
            }
            if (intent.hasExtra("UPLOAD")) {
                if (intent.getIntExtra("progress", 0) == 101) {
                    ((ImageButton) root.findViewById(R.id.ibFI_speedRequest)).setEnabled(true);
                    ((ProgressBar) root.findViewById(R.id.progressBarFI_internetSpeed))
                            .setProgress(0);
                    if (act instanceof MainActivity) {
                        Entry entry = new Entry(getTime(),
                                (intent.getFloatExtra("UPLOAD", 0))*100);
                        ((MainActivity) act).upload.add(entry);
                    }
                } else {
                    ((ProgressBar) root.findViewById(R.id.progressBarFI_internetSpeed))
                            .setProgress(intent.getIntExtra("progress", 0));
                }
                String text = String.valueOf(intent.getFloatExtra("UPLOAD", 0)) + "MB/s";
                ((TextView) root.findViewById(R.id.tvFI_internetSpeedUpload)).setText(text);
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
        super.onStop();
        Log.d(TAG, "onStop: ~~");
    }
}

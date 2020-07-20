package cz.utb.thesisapp.ui.touch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cz.utb.thesisapp.GlobalValues;
import cz.utb.thesisapp.MainActivity;
import cz.utb.thesisapp.R;
import cz.utb.thesisapp.ui.info.InfoViewModel;

import static cz.utb.thesisapp.GlobalValues.*;

public class TouchFragment extends Fragment {
    private static final String TAG = "TouchFragment";
    private InfoViewModel infoViewModel;
    private TouchViewModel touchViewModel;
    MyDrawingView dvThisApp;
    MyDrawingView dvRemoteApp;
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
    boolean threadRun = true;
    private Thread thread;
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    AsyncTask<Void, Void, Integer> runningTask;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            touchViewModel =
                    ViewModelProviders.of(getActivity()).get(TouchViewModel.class);
            infoViewModel =
                    ViewModelProviders.of(getActivity()).get(InfoViewModel.class);
        } catch (Exception e) {

        }
        root = inflater.inflate(R.layout.fragment_touch, container, false);
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            dvThisApp = (MyDrawingView) root.findViewById(R.id.scratch_pad);
//            dvThisApp.setTextView(((TextView) root.findViewById(R.id.tvTouchInfo)));
            dvThisApp.act = act;
        }

        if (act instanceof MainActivity) {
            dvRemoteApp = (MyDrawingView) root.findViewById(R.id.scratch_pad_partner);
            dvRemoteApp.setThisApp(false);
            dvRemoteApp.paint.setColor(ContextCompat.getColor(act, R.color.colorPrimary));
            dvRemoteApp.act = act;

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
        touchViewModel.getTouch().observe(getViewLifecycleOwner(), new Observer<ArrayList<GlobalValues.Touch>>() {
            @Override
            public void onChanged(ArrayList<GlobalValues.Touch> touches) {
                try {
                    for (GlobalValues.Touch touch : touches) {
                        Activity act = getActivity();
                        if (act instanceof MainActivity) {
                           /* Log.d(TAG, "onChanged: ~~" + touch.touchType + "\n" +
                                    "clientCreated: " + df.format(touch.clientCreated.getTime() + ((MainActivity) act).difference) + "\n"
                                    + "serverReceived:" + df.format(touch.serverReceived) + "\n" +
                                    "clientReceived:" + df.format(touch.clientReceived.getTime() + ((MainActivity) act).difference));*/
                        }

                        countTime(touch.clientCreated, touch.clientReceived);
                        dvRemoteApp.remoteTouchEvent(touch.touchType, touch.x, touch.y);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onChanged: ~~" + e);
                }

            }
        });

        touchViewModel.getTest().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.d(TAG, "onChanged: ~~");
                switch (integer) {
                    case TOUCH_START_TEST:
                        Log.d(TAG, "onChanged: ~~TOUCH_START_TEST");
                        if (runningTask != null) {
                            runningTask.cancel(true);
                        }
                        runningTask = new TestTask();
                        runningTask.execute();
                        touchViewModel.setTest(TOUCH_NO_TEST);
                        break;
                    case TOUCH_BREAK_TEST:
                        Log.d(TAG, "onChanged: ~~TOUCH_BREAK_TEST");
                        if (runningTask != null) {
                            runningTask.cancel(true);
                        }
                        touchViewModel.setTest(TOUCH_NO_TEST);
                        break;
                    case TOUCH_TEST_FINISHED:
                        Activity act = getActivity();
                        if (act instanceof MainActivity) {
                            ((MainActivity) act).exportDB();
                        }
                        try {
                            dvThisApp.clear();
                            dvRemoteApp.clear();
                        }catch (Exception e){
                            Log.d(TAG, "onChanged: ~~"+e);
                        }

                        touchViewModel.setTest(TOUCH_NO_TEST);
                        break;
                    case TOUCH_NO_TEST:
                        break;
                }
            }
        });


        return root;
    }

    public void startPlot() {
        if (thread != null) {
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    while (true) {
                        try {
                            Thread.sleep(150);
                            (getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    plot();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
            if (yValues.size() > 20) {
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
        return Float.valueOf(sb1) / 1000;
    }

    private void countTime(Date clientCreated, Date clientReceived) {
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            try {
                long seconds = (clientReceived.getTime() - clientCreated.getTime());
                addDataToChart(seconds);
            } catch (Exception e) {

            }
        }
    }

    private final BroadcastReceiver maReiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            if (i.hasExtra("refresh")) {
                dvRemoteApp.clear();
                dvThisApp.clear();
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {

            if (i.hasExtra("ScreenSize")) {
            }
            if (i.hasExtra("CleanCanvas")) {
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
//        touchViewModel.setTouchStart(null);
//        touchViewModel.setTouchMove(null);
        if (thread != null) {
            thread.interrupt();
        }
        super.onStop();
        Log.d(TAG, "onStop: ~~");
    }

    private class TestTask extends AsyncTask<Void, Void, Integer> {
        protected Integer doInBackground(Void... params) {
            int canvasH = dvThisApp.getCanvasH();
            int canvasW = dvThisApp.getCanvasW();
            boolean started = false;
            for (int i = 0; i < TOUCH_ITERATIONS; i++) {
                if (isCancelled()) {
                    break;
                }
                try {
                    int action = MotionEvent.ACTION_DOWN;
                    if (!started) {
                        started = true;
                    } else {
                        if (new Random().nextDouble() < 0.8) {
                            action = MotionEvent.ACTION_MOVE;
                        } else {
                            action = MotionEvent.ACTION_UP;
                            started = false;
                        }
                    }
                    dvThisApp.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            action,
                            canvasW * (float) (new Random().nextDouble()),
                            canvasH * (float) (new Random().nextDouble()),
                            0));

                    double percentage = Math.abs(Math.sin(Math.toRadians((double) i)));
                    Log.d(TAG, "doInBackground: ~~" + percentage);
                    Thread.sleep(TOUCH_SLEEP_MIN + ((int) (TOUCH_SLEEP_BASE * percentage)));
                } catch (InterruptedException e) {
                    // We were cancelled; stop sleeping!
                    return TOUCH_NO_TEST;
                }
            }
            return TOUCH_TEST_FINISHED;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "onPostExecute: ~~"+result);
            touchViewModel.setTest(result);
            // txt.setText(result);
            // You might want to change "executed" for the returned string
            // passed into onPostExecute(), but that is up to you
        }
    }
}

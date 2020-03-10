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
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.HashMap;

import cz.utb.thesisapp.MainActivity;
import cz.utb.thesisapp.R;

public class TouchFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    private TouchViewModel touchViewModel;
    MyDrawingView dvThisApp;
    MyDrawingView dvPairedApp;
    private Paint mPaint;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        touchViewModel =
                ViewModelProviders.of(this).get(TouchViewModel.class);
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
            dvThisApp.setTextView(((TextView) root.findViewById(R.id.tvTouchInfo)));
            dvThisApp.act = act;
        }

        if (act instanceof MainActivity) {
            dvPairedApp = (MyDrawingView) root.findViewById(R.id.scratch_pad_partner);
            dvPairedApp.setThisApp(false);
            dvPairedApp.paint.setColor(Color.YELLOW);
            dvPairedApp.act = act;

            LocalBroadcastManager.getInstance(act).registerReceiver(mReceiver, new IntentFilter("touch"));
        }
        return root;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            if (i.hasExtra("TouchStart")) {
                dvPairedApp.remoteTouchEvent("TouchStart",
                        i.getFloatExtra("x", 0),
                        i.getFloatExtra("y", 0));
            }
            if (i.hasExtra("TouchMove")) {
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

            } else {
                // Do something else
            }

        }
    };
}

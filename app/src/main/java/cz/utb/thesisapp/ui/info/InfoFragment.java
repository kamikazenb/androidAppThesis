package cz.utb.thesisapp.ui.info;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
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

import java.util.HashMap;

import cz.utb.thesisapp.MainActivity;
import cz.utb.thesisapp.R;

public class InfoFragment extends Fragment {

    private InfoViewModel infoViewModel;
    private static final String TAG = "InfoFragment";
    View root;

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
                    ((ImageButton)root.findViewById(R.id.ibFI_speedRequest)).startAnimation(ranim);
                    ((MainActivity) act).startDownloadTest();
                    ((ImageButton)root.findViewById(R.id.ibFI_speedRequest)).setEnabled(false);
                    ((TextView)root.findViewById(R.id.tvFI_internetSpeedDownload)).setText("");
                    ((TextView)root.findViewById(R.id.tvFI_internetSpeedUpload)).setText("");

                }
            }
        });

        Log.d(TAG, "onCreateView: ~~");
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            LocalBroadcastManager.getInstance(act).registerReceiver(mReceiver, new IntentFilter("info"));

        }

        return root;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra("DOWNLOAD")) {
                ((ProgressBar) root.findViewById(R.id.progressBarFI_internetSpeed))
                        .setProgress(intent.getIntExtra("progress", 0));
                ((TextView) root.findViewById(R.id.tvFI_internetSpeedDownload))
                        .setText(intent.getStringExtra("DOWNLOAD"));
            }
            if (intent.hasExtra("UPLOAD")) {
                ((ProgressBar) root.findViewById(R.id.progressBarFI_internetSpeed))
                        .setProgress(intent.getIntExtra("progress", 0));
                ((TextView) root.findViewById(R.id.tvFI_internetSpeedUpload))
                        .setText(intent.getStringExtra("UPLOAD"));
                if(intent.getIntExtra("progress", 0) == 100){
                    ((ImageButton)root.findViewById(R.id.ibFI_speedRequest)).setEnabled(true);
                    ((ProgressBar) root.findViewById(R.id.progressBarFI_internetSpeed))
                            .setProgress(0);
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ~~");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ~~");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ~~");
    }
}

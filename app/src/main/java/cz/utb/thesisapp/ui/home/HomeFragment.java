package cz.utb.thesisapp.ui.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;

import cz.utb.thesisapp.MainActivity;
import cz.utb.thesisapp.R;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private HashMap<String, String> kryoClients = new HashMap<>();
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        ((Switch) root.findViewById(R.id.bTest)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity act = getActivity();
                if (isChecked) {
                    if (act instanceof MainActivity) {
                        if (((MainActivity) act).ismBound()) {
                            Log.d(TAG, "onClick: ~~");
                            String input = ((EditText) root.findViewById(R.id.etKryoIP)).getText().toString();
                            ((MainActivity) act).startKryo(input);
                        }
                    }
                    ((Spinner) root.findViewById(R.id.spinner)).setClickable(true);
                } else {
                    if (((MainActivity) act).ismBound()) {
                        ((MainActivity) act).stopKryo();
                    }
                    ((Spinner) root.findViewById(R.id.spinner)).setClickable(false);
                    ((Switch) root.findViewById(R.id.switchSynced)).setClickable(false);
                    ((Switch) root.findViewById(R.id.switchSynced)).setChecked(false);
                }
            }
        });
        ((Switch) root.findViewById(R.id.switchSynced)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                }else {
                    ((TextView)root.findViewById(R.id.tvKryoPairName)).setText("");
                    Activity act = getActivity();
                    if (act instanceof MainActivity) {
                        ((MainActivity) act).kryoUnpair();
                    }
                }
            }
        });

        Log.d(TAG, "onCreateView: ~~");
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            LocalBroadcastManager.getInstance(act).registerReceiver(mReceiver, new IntentFilter("kryo"));
        }

        return root;
    }

    public void setDropdownMenu(final HashMap<String, String> source) {
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Select:");
        final ArrayList<String> keys = new ArrayList<String>(source.keySet());
        spinnerArray.addAll(source.values());
        Spinner spinnerDropdown = root.findViewById(R.id.spinner);
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                    (act, android.R.layout.simple_spinner_dropdown_item,
                            spinnerArray);
            spinnerDropdown.setAdapter(spinnerArrayAdapter);
            spinnerDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    if (position == 0) {
                        Log.d(TAG, "onItemSelected: ~~0");
                    } else {
                        Log.d(TAG, "onItemSelected: ~~else");
                        try {
                            Activity act = getActivity();
                            if (act instanceof MainActivity) {
                                ((MainActivity) act).requestPartner(keys.get(position - 1));
                            }
                        } catch (Exception e) {
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("users")) {
                try {
                    HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("users");
                    setDropdownMenu(hashMap);
                } catch (Exception e) {

                }
            }
            if (intent.hasExtra("paired")) {
                try {
                    ((TextView)root.findViewById(R.id.tvKryoPairName)).setText(intent.getStringExtra("paired"));
                    ((Switch)root.findViewById(R.id.switchSynced)).setChecked(true);
                    ((Switch)root.findViewById(R.id.switchSynced)).setClickable(true);
                } catch (Exception e) {

                }
            }
            if (intent.hasExtra("unpaired")) {
                try {
                    ((Switch)root.findViewById(R.id.switchSynced)).setChecked(false);
                } catch (Exception e) {

                }
            }
            if (intent.hasExtra("command")) {
                if (intent.getStringExtra("command").equals("setChecked")) {
                    ((Switch) root.findViewById(R.id.bTest)).setChecked(true);
                }
                if (intent.getStringExtra("command").equals("setUnchecked")) {
                    ((Switch) root.findViewById(R.id.bTest)).setChecked(false);
                }
            } else {
                // Do something else
            }

        }
    };

}

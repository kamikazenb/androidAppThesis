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

        Switch ib = root.findViewById(R.id.bTest);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity act = getActivity();
                if (((Switch) root.findViewById(R.id.bTest)).isChecked()) {
                    if (act instanceof MainActivity) {
                        if (((MainActivity) act).ismBound()) {
                            Log.d(TAG, "onClick: ~~");
                            String input = ((EditText) root.findViewById(R.id.etKryoIP)).getText().toString();
                            ((MainActivity) act).startKryo(input);

                        }
                    }
                } else {
                    if (((MainActivity) act).ismBound()) {
                        ((MainActivity) act).stopKryo();
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
        ArrayList<String> spinnerArray = new ArrayList<String>(source.keySet());
        Spinner spinnerDropdown = root.findViewById(R.id.spinner);
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                    (act,
                            android.R.layout.simple_spinner_dropdown_item,
                            spinnerArray);

            spinnerDropdown.setAdapter(spinnerArrayAdapter);
            spinnerDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    try {
                        String token = (String) source.get((String) parent.getItemAtPosition(position));
                        Activity act = getActivity();
                        if (act instanceof MainActivity) {
                            ((MainActivity) act).requestPartner(token);
                        }
                    } catch (Exception e) {
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
                    Bundle args = intent.getBundleExtra("users");
                    HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("users");
                    setDropdownMenu(hashMap);
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

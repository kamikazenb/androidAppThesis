package cz.utb.thesisapp.ui.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import androidx.lifecycle.MutableLiveData;
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
    String userName;
    SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            homeViewModel =
                    ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        } catch (Exception e) {

        }

        root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel.getPairedName().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                ((TextView) root.findViewById(R.id.tvKryoPairName)).setText(s);
            }
        });

        homeViewModel.getRequireRefresh().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    ((Switch) root.findViewById(R.id.sKryoIP)).setChecked(false);
                    homeViewModel.setRequireRefresh(false);
                }
            }
        });

        homeViewModel.getKryoConnected().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                ((Switch) root.findViewById(R.id.sKryoIP)).setChecked(aBoolean);

            }
        });

        homeViewModel.getPaired().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    ((Switch) root.findViewById(R.id.switchSynced)).setChecked(true);
                    ((Switch) root.findViewById(R.id.switchSynced)).setClickable(true);
                } else {
                    ((Switch) root.findViewById(R.id.switchSynced)).setChecked(false);
                }
            }
        });

        homeViewModel.getUsers().observe(getViewLifecycleOwner(), new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringStringHashMap) {
                if (stringStringHashMap != null) {
                    ((Spinner) root.findViewById(R.id.spinner)).setClickable(true);
                    setDropdownMenu(stringStringHashMap);
                } else {
                    ((Spinner) root.findViewById(R.id.spinner)).setClickable(false);
                    ((Spinner) root.findViewById(R.id.spinner)).setAdapter(null);
                }
            }
        });

        Activity act = getActivity();
        if (act instanceof MainActivity) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(act);
            userName = sharedPreferences.getString("userName", "null");
            if (userName.equals("null")) {
                userName = "Android client";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", userName);
                editor.apply();
            }
        }

        ((EditText) root.findViewById(R.id.etUserName)).setText(userName);

        ((Switch) root.findViewById(R.id.sKryoIP)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity act = getActivity();
                if (isChecked) {
                    if (act instanceof MainActivity) {
                        if (unboxBool(homeViewModel.getmBounded())) {
                            Log.d(TAG, "onClick: ~~");
                            String input = ((EditText) root.findViewById(R.id.etKryoIP)).getText().toString();
                            userName = ((EditText) root.findViewById(R.id.etUserName)).getText().toString();
                            if (userName.equals(null) || userName.length() < 1) {
                                userName = "Android client";
                            }
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userName", userName);
                            editor.apply();
                            ((MainActivity) act).startKryo(input, userName);

                        }
                    }

                } else {
                    if (unboxBool(homeViewModel.getmBounded())) {
                        ((MainActivity) act).stopKryo();
                    }
                    homeViewModel.setPaired(false);
                    homeViewModel.setUsers(null);
                    homeViewModel.setPairedname("");
                }
            }
        });

        ((Switch) root.findViewById(R.id.switchSynced)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {
                    ((TextView) root.findViewById(R.id.tvKryoPairName)).setText("");
                    Activity act = getActivity();
                    if (act instanceof MainActivity) {
                        ((MainActivity) act).kryoUnpair();
                    }
                }
            }
        });
        Log.d(TAG, "onCreateView: ~~");
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

    private boolean unboxBool(MutableLiveData<Boolean> a) {
        Boolean b = a.getValue();
        if (b == null) {
            b = false;
        }
        return b;
    }
}

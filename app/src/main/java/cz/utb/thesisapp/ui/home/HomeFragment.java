package cz.utb.thesisapp.ui.home;

import android.app.Activity;
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
    String ipAdress;
    SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            homeViewModel =
                    ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        } catch (Exception e) {

        }

        root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel.getKryoUseDatabase().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.i(TAG, "onChanged: ~~db usage"+aBoolean);
                ((Switch) root.findViewById(R.id.sKryoDB)).setChecked(aBoolean);
            }
        });

        homeViewModel.getRequireRefresh().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    ((Switch) root.findViewById(R.id.sKryoConnected)).setChecked(false);
                    homeViewModel.setRequireRefresh(false);
                }
            }
        });

        homeViewModel.getKryoConnected().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ((Switch) root.findViewById(R.id.sKryoConnected)).setChecked(aBoolean);
                ((Switch) root.findViewById(R.id.sKryoDB)).setClickable(aBoolean);
            }
        });
        homeViewModel.getWebConnected().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ((Switch) root.findViewById(R.id.sSpringConnected)).setChecked(aBoolean);

            }
        });
        homeViewModel.getFirebaseConnected().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ((Switch) root.findViewById(R.id.sFirebaseConnected)).setChecked(aBoolean);
                ((Switch) root.findViewById(R.id.sFirebaseRemoteListener)).setClickable(aBoolean);
            }
        });
        homeViewModel.getFirebaseRemoteListener().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                ((Switch) root.findViewById(R.id.sFirebaseRemoteListener)).setChecked(aBoolean);
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
            ipAdress = sharedPreferences.getString("kryoIP", "null");
            if (userName.equals("null") | ipAdress.equals("null")) {
                userName = "Android client";
                ipAdress = "195.178.94.66";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", userName);
                editor.putString("kryoIP", ipAdress);
                editor.apply();
            }
        }

        ((Switch) root.findViewById(R.id.sKryoDB)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Activity act = getActivity();
                if (act instanceof MainActivity) {
                    ((MainActivity) act).kryoUseDatabase(b);
                }
            }
        });

        ((EditText) root.findViewById(R.id.etUserName)).setText(userName);
        ((EditText) root.findViewById(R.id.etIP)).setText(ipAdress);

        ((Switch) root.findViewById(R.id.sKryoConnected)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity act = getActivity();
                if (isChecked) {
                    if (act instanceof MainActivity) {
                        if (unboxBool(homeViewModel.getmBounded())) {
                            setPersistenceData();
                            ((MainActivity) act).startKryo(ipAdress, userName);
                        }
                    }
                } else {
                    if (unboxBool(homeViewModel.getmBounded())) {
                        ((MainActivity) act).stopKryo();
                    }
                    homeViewModel.setUsers(null);
                }
            }
        });
        ((Switch) root.findViewById(R.id.sSpringConnected)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity act = getActivity();
                if (isChecked) {
                    if (act instanceof MainActivity) {
                        setPersistenceData();
                        ((MainActivity) act).startWebServices(ipAdress, userName);
                    }
                } else {
                    if (act instanceof MainActivity) {
                        ((MainActivity) act).stopWebServices();
                    }
                }
            }
        });
        ((Switch) root.findViewById(R.id.sFirebaseConnected)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity act = getActivity();
                if (isChecked) {
                    if (act instanceof MainActivity) {
                        setPersistenceData();
                        ((MainActivity) act).startFirebaseServices(userName);
                    }
                } else {
                    if (act instanceof MainActivity) {
                        ((MainActivity) act).stopFirebase();
                    }
                }
            }
        });
        ((Switch) root.findViewById(R.id.sFirebaseRemoteListener)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity act = getActivity();
                if (act instanceof MainActivity) {
                    homeViewModel.setFirebaseRemoteListener(isChecked);
                    ((MainActivity) act).setFirebaseRemoteListener();

                }
            }
        });


        ((Switch) root.findViewById(R.id.switchSynced)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    Activity act = getActivity();
                    if (act instanceof MainActivity) {
                        ((MainActivity) act).kryoUnfollow();
                    }
                }
            }
        });
        Log.i(TAG, "onCreateView: ~~");
        return root;
    }

    public void setPersistenceData() {
        ipAdress = ((EditText) root.findViewById(R.id.etIP)).getText().toString();
        userName = ((EditText) root.findViewById(R.id.etUserName)).getText().toString();
        if (userName.equals(null) || userName.length() < 1) {
            userName = "Android client";
        }
        if (kryoClients.equals(null) | kryoClients.equals("")) {
            ipAdress = "195.178.94.66";
            ((EditText) root.findViewById(R.id.etIP)).setText(ipAdress);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", userName);
        editor.putString("kryoIP", ipAdress);
        editor.apply();
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
                        Log.i(TAG, "onItemSelected: ~~0");
                    } else {
                        Log.i(TAG, "onItemSelected: ~~else");
                        try {
                            Activity act = getActivity();
                            if (act instanceof MainActivity) {
                                Log.i(TAG, "onItemSelected: ~~" + keys.get(position - 1));
                                ((MainActivity) act).kryoRequestFollow(keys.get(position - 1));
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
        Log.i(TAG, "onResume: ~~");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ~~");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ~~");
    }

    private boolean unboxBool(MutableLiveData<Boolean> a) {
        Boolean b = a.getValue();
        if (b == null) {
            b = false;
        }
        return b;
    }
}

package ssdl.technion.ac.il.locationnotification.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ssdl.technion.ac.il.locationnotification.MainActivity;
import ssdl.technion.ac.il.locationnotification.R;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;

public class ZeroRemindersFragment extends Fragment {

    public ZeroRemindersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_zero_reminders, container, false);

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity)getActivity()).updateFragment();
    }
}

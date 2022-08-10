package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.makepe.blackout.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DynamicSearchFragment extends Fragment {
    private String tabTitle;
    private int tabPosition;
    private ArrayList<String> searchCategories;

    public DynamicSearchFragment() {
        // Required empty public constructor
    }

    public static Fragment addFrag(int position, ArrayList<String> searchTabList) {
        DynamicSearchFragment fragment = new DynamicSearchFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        args.putStringArrayList("tabTitles", searchTabList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic_search, container, false);

        searchCategories = new ArrayList<>();

        searchCategories = getArguments().getStringArrayList("tabTitles");
        tabPosition = getArguments().getInt("tabPosition", 0);


        return view;
    }
}
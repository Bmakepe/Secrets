package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.makepe.blackout.R;

import java.util.ArrayList;

public class DynamicNewsFragment extends Fragment {

    private int position;
    private ArrayList<String> newsCategories;
    private SearchView searchView;
    
    public DynamicNewsFragment() {
        // Required empty public constructor
    }

    public static Fragment addFragment(int position, ArrayList<String> newsTabList) {
        DynamicNewsFragment fragment = new DynamicNewsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("tabPosition", position);
        bundle.putStringArrayList("tabTitles", newsTabList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_dynamic_news, container, false);

        searchView = view.findViewById(R.id.dynamicNewsSearchView);
        position = getArguments().getInt("tabPosition", 0);
        newsCategories = getArguments().getStringArrayList("tabTitles");

        for(int i = 0; i < newsCategories.size(); i++){
            if(i == position)
                searchView.setQueryHint("Search " + newsCategories.get(i) + " Stories");
        }

        return view;
    }
}
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

public class DynamicShoppingFragment extends Fragment {

    private int position;
    private ArrayList<String> shoppingCategories;
    private SearchView searchView;

    public DynamicShoppingFragment() {
        // Required empty public constructor
    }

    public static Fragment addFragment(int position, ArrayList<String> shoppingCategories) {
        DynamicShoppingFragment fragment = new DynamicShoppingFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        args.putStringArrayList("tabTitles", shoppingCategories);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic_shopping, container, false);

        searchView = view.findViewById(R.id.dynamicShoppingSearchView);
        position = getArguments().getInt("tabPosition", 0);
        shoppingCategories = getArguments().getStringArrayList("tabTitles");

        for(int i = 0; i< shoppingCategories.size(); i++){
            if(i == position)
                searchView.setQueryHint("Search " + shoppingCategories.get(i) + " Stores");
        }

        return view;
    }
}
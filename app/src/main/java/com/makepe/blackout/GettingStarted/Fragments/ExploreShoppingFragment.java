package com.makepe.blackout.GettingStarted.Fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.makepe.blackout.GettingStarted.Adapters.ShoppingTabAdapter;
import com.makepe.blackout.R;

import java.util.ArrayList;

public class ExploreShoppingFragment extends Fragment {

    private ArrayList<String> shoppingCategories;
    private ViewPager shoppingPager;
    private TabLayout shoppingTabs;

    public ExploreShoppingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_shopping, container, false);

        shoppingPager = view.findViewById(R.id.shoppingViewPager);
        shoppingTabs = view.findViewById(R.id.shoppingTabs);

        shoppingCategories = new ArrayList<>();
        shoppingCategories.add("Clothing");
        shoppingCategories.add("Food & Beverage");
        shoppingCategories.add("Health");
        shoppingCategories.add("Restaurant");
        shoppingCategories.add("Entertainment");
        shoppingCategories.add("Other");

        for(int i = 0; i < shoppingCategories.size(); i++){
            shoppingTabs.addTab(shoppingTabs.newTab().setText(shoppingCategories.get(i)));
        }

        try {
            ShoppingTabAdapter tabAdapter = new ShoppingTabAdapter(getChildFragmentManager(), shoppingTabs.getTabCount(), shoppingCategories, getContext());
            shoppingPager.setAdapter(tabAdapter);
            shoppingPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(shoppingTabs));

            shoppingTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    shoppingPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

        }catch (IllegalArgumentException ignore){}
        //shoppingTabs.setupWithViewPager(shoppingPager);

        return view;
    }
}
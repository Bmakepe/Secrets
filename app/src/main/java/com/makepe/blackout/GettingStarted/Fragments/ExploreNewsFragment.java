package com.makepe.blackout.GettingStarted.Fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import com.makepe.blackout.GettingStarted.Adapters.NewsTabAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.SearchActivity;
import com.makepe.blackout.R;

import java.util.ArrayList;

public class ExploreNewsFragment extends Fragment {

    private ArrayList<String> newsCategories;
    private ViewPager newsPager;
    private TabLayout newsTabs;

    private NewsTabAdapter tabAdapter;

    public ExploreNewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_news, container, false);

        newsTabs = view.findViewById(R.id.newsTabs);
        newsPager = view.findViewById(R.id.newsViewPager);

        newsCategories = new ArrayList<>();
        newsCategories.add("Breaking News");
        newsCategories.add("Business");
        newsCategories.add("Science & Technology");
        newsCategories.add("Music & Entertainment");
        newsCategories.add("Sports");

        for(int i = 0; i < newsCategories.size(); i++){
            newsTabs.addTab(newsTabs.newTab().setText(newsCategories.get(i)));
        }

        try{
            tabAdapter = new NewsTabAdapter(getChildFragmentManager(), newsTabs.getTabCount(), newsCategories, getContext());
            newsPager.setAdapter(tabAdapter);
            //newsTabs.setupWithViewPager(newsPager);
            newsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(newsTabs));

            newsTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    newsPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }catch (IllegalArgumentException ignored){}

        return view;
    }
}
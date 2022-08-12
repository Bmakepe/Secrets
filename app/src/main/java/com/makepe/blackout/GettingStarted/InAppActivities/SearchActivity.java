package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.makepe.blackout.GettingStarted.Adapters.SearchTabAdapter;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;

public class SearchActivity extends AppCompatActivity {

    private TabLayout searchTabs;
    private ViewPager searchPager;
    private ArrayList<String> searchCategories;
    private SearchTabAdapter tabAdapter;

    private SearchView secretSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar hisProfileToolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(hisProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchTabs = findViewById(R.id.searchTabs);
        searchPager = findViewById(R.id.searchViewPager);
        secretSearchView = findViewById(R.id.secretSearchView);

        searchCategories = new ArrayList<>();
        searchCategories.add("All");
        searchCategories.add("Posts");
        searchCategories.add("Videos");
        searchCategories.add("Users");
        searchCategories.add("Groups");
        //searchCategories.add("Stores");
        searchCategories.add("News");

        Collections.sort(searchCategories);

        for (int i = 0; i < searchCategories.size(); i++){
            searchTabs.addTab(searchTabs.newTab().setText(searchCategories.get(i)));
        }

        try{
            tabAdapter = new SearchTabAdapter(getSupportFragmentManager(), searchTabs.getTabCount(), searchCategories, this);
            searchPager.setAdapter(tabAdapter);
            //searchTabs.setupWithViewPager(searchPager);
            searchPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(searchTabs));

            searchTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    searchPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

        }catch (IllegalArgumentException ignored){}

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
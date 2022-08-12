package com.makepe.blackout.GettingStarted.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.tabs.TabLayout;
import com.makepe.blackout.GettingStarted.Adapters.VideoAdapter;
import com.makepe.blackout.GettingStarted.Adapters.VideoTabAdapter;
import com.makepe.blackout.GettingStarted.OtherClasses.VideoPlayer;
import com.makepe.blackout.R;

import java.util.ArrayList;

public class ExploreFragment extends Fragment {

    private ArrayList<String> videoTabList;

    private ViewPager explorePager;
    private TabLayout videosTab;
    private VideoTabAdapter tabAdapter;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        explorePager = view.findViewById(R.id.exploreViewPager);
        videosTab = view.findViewById(R.id.videosTab);

        videoTabList = new ArrayList<>();
        videoTabList.add("Following");
        videoTabList.add("Explore");

        for (int i = 0; i < videoTabList.size(); i++){
            videosTab.addTab(videosTab.newTab().setText(videoTabList.get(i)));
        }

        try{
            tabAdapter = new VideoTabAdapter(getChildFragmentManager(), videosTab.getTabCount(), videoTabList, getContext());
            explorePager.setAdapter(tabAdapter);
            explorePager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(videosTab));

            videosTab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    explorePager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    Toast.makeText(getContext(), "Videos should refresh : " + tab.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (IllegalArgumentException ignored){}

        return view;
    }
}
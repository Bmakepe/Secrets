package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.makepe.blackout.GettingStarted.Fragments.DynamicVideoFragment;

import java.util.ArrayList;

public class VideoTabAdapter extends FragmentStatePagerAdapter {

    int numOfTabs;
    ArrayList<String> videoTabList;
    Context context;

    public VideoTabAdapter(FragmentManager fm, int numOfTabs, ArrayList<String> videoTabList, Context context) {
        super(fm);
        this.numOfTabs = numOfTabs;
        if (!videoTabList.isEmpty())
            this.videoTabList = videoTabList;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position >= 0)
            return DynamicVideoFragment.addFrag(position, videoTabList);
        else
            return null;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}

package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.makepe.blackout.GettingStarted.Fragments.DynamicNewsFragment;

import java.util.ArrayList;

public class NewsTabAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;
    private ArrayList<String> newsTabList;
    private Context context;

    public NewsTabAdapter(@NonNull FragmentManager fm, int numOfTabs, ArrayList<String> newsTabList, Context context) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.newsTabList = newsTabList;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position >= 0)
            return DynamicNewsFragment.addFragment(position, newsTabList);
        else
            return null;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}

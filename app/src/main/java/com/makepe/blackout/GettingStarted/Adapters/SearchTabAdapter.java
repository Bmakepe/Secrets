package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.makepe.blackout.GettingStarted.Fragments.DynamicSearchFragment;

import java.util.ArrayList;

public class SearchTabAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;
    private ArrayList<String> searchTabList;
    private Context context;

    public SearchTabAdapter(@NonNull FragmentManager fm, int numOfTabs, ArrayList<String> searchTabList, Context context) {
        super(fm);
        this.numOfTabs = numOfTabs;
        if (!searchTabList.isEmpty())
            this.searchTabList = searchTabList;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position >= 0)
            return DynamicSearchFragment.addFrag(position, searchTabList);
        else
            return null;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}

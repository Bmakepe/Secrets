package com.makepe.blackout.GettingStarted.Adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.makepe.blackout.GettingStarted.Fragments.HisPostsFragment;
import com.makepe.blackout.GettingStarted.Fragments.HisVideosFragment;

import java.util.ArrayList;

public class HisTabAdapter extends FragmentPagerAdapter {


    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;
    private String userID;

    public HisTabAdapter(@NonNull FragmentManager fm){
        super(fm);
        this.fragments = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    public void addFragment(Fragment fragment, String title, String userID){
        fragments.add(fragment);
        titles.add(title);
        this.userID = userID;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position){
            case 0:
                fragment = new HisPostsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("hisUserID", userID);
                fragment.setArguments(bundle);
                break;

            case 1:
                fragment = new HisVideosFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putString("hisUserID", userID);
                fragment.setArguments(bundle1);
                break;

            default:
                fragment = null;
                break;
        }

        return fragment;
    }


    public void addFragment(Fragment fragment, String title){
        fragments.add(fragment);
        titles.add(title);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}

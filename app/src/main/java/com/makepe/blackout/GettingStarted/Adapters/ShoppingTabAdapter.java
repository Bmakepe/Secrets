package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.makepe.blackout.GettingStarted.Fragments.DynamicShoppingFragment;

import java.util.ArrayList;

public class ShoppingTabAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;
    private ArrayList<String> shoppingCategories;
    private Context context;

    public ShoppingTabAdapter(@NonNull FragmentManager fm, int numOfTabs, ArrayList<String> shoppingCategories, Context context) {
        super(fm);
        this.numOfTabs = numOfTabs;
        this.shoppingCategories = shoppingCategories;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position >= 0)
            return DynamicShoppingFragment.addFragment(position, shoppingCategories);
        else
            return null;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}

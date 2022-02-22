package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makepe.blackout.GettingStarted.OtherClasses.ViewPagerAdapter;
import com.makepe.blackout.R;

public class ExploreFragment extends Fragment {

    ViewPager explorePager;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        explorePager = view.findViewById(R.id.exploreViewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new ExploreImagesFragment());
        viewPagerAdapter.addFragment(new ExploreVideosFragment());
        explorePager.setAdapter(viewPagerAdapter);

        return view;
    }
}
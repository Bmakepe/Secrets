package com.makepe.blackout.GettingStarted.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.makepe.blackout.GettingStarted.InAppActivities.SearchActivity;
import com.makepe.blackout.GettingStarted.OtherClasses.ViewPagerAdapter;
import com.makepe.blackout.R;
public class SecretExploreFragment extends Fragment {

    private ViewPager exploreFragmentPager;
    private Toolbar exploreToolbar;

    public SecretExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_secret_explore, container, false);

        exploreFragmentPager = view.findViewById(R.id.exploreFragmentPager);
        exploreToolbar = view.findViewById(R.id.exploreToolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(exploreToolbar);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new ExploreImagesFragment());
        viewPagerAdapter.addFragment(new ExploreNewsFragment());
        viewPagerAdapter.addFragment(new ExploreShoppingFragment());

        exploreFragmentPager.setAdapter(viewPagerAdapter);

        exploreFragmentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        exploreToolbar.setTitle("Explore");
                        break;

                    case 1:
                        exploreToolbar.setTitle("Explore News");
                        break;

                    case 2:
                        exploreToolbar.setTitle("Explore Stores");
                        break;

                    default:
                        Toast.makeText(getContext(), "Unknown Selection", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.explore_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.exploreSearch) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
package com.makepe.blackout.GettingStarted.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makepe.blackout.GettingStarted.Adapters.NewsAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.NewsDetailsActivity;
import com.makepe.blackout.GettingStarted.Models.NewsApiResponse;
import com.makepe.blackout.GettingStarted.Models.NewsHeadlines;
import com.makepe.blackout.GettingStarted.OtherClasses.OnFetchDataListener;
import com.makepe.blackout.GettingStarted.OtherClasses.RequestManager;
import com.makepe.blackout.GettingStarted.OtherClasses.SelectListener;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

public class DynamicNewsFragment extends Fragment implements SelectListener {

    private String tabTitle;
    private int position;
    private ArrayList<String> newsCategories;
    private SearchView searchView;

    private RecyclerView newsRecycler;
    private View view;
    
    public DynamicNewsFragment() {
        // Required empty public constructor
    }

    public static Fragment addFragment(int position, ArrayList<String> newsTabList) {
        DynamicNewsFragment fragment = new DynamicNewsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("tabPosition", position);
        bundle.putStringArrayList("tabTitles", newsTabList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_dynamic_news, container, false);

        searchView = view.findViewById(R.id.dynamicNewsSearchView);
        position = getArguments().getInt("tabPosition", 0);
        newsCategories = getArguments().getStringArrayList("tabTitles");

        for(int i = 0; i < newsCategories.size(); i++){
            if(i == position)
                tabTitle = newsCategories.get(i);
                searchView.setQueryHint("Search " + newsCategories.get(i) + " Stories");
        }

        RequestManager manager = new RequestManager(getContext());
        if (position == 0)
            manager.getNewsHeadlines(listener, "general", null);
        else
            manager.getNewsHeadlines(listener, tabTitle, null);

        return view;
    }

    private final OnFetchDataListener<NewsApiResponse> listener = new OnFetchDataListener<NewsApiResponse>() {
        @Override
        public void onFetchData(List<NewsHeadlines> list, String message) {
            showList(list);
        }

        @Override
        public void onError(String message) {

        }
    };

    private void showList(List<NewsHeadlines> list) {

        newsRecycler = view.findViewById(R.id.newsRecycler);
        newsRecycler.hasFixedSize();
        newsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecycler.setAdapter(new NewsAdapter(getContext(), list, this));
    }

    @Override
    public void OnNewsClicked(NewsHeadlines headlines) {
        startActivity(new Intent(getContext(), NewsDetailsActivity.class)
                .putExtra("data", headlines));
    }
}
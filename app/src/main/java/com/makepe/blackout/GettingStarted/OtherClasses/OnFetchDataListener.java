package com.makepe.blackout.GettingStarted.OtherClasses;

import com.makepe.blackout.GettingStarted.Models.NewsHeadlines;

import java.util.List;

public interface OnFetchDataListener<NewsApiResponse> {
    void onFetchData(List<NewsHeadlines> list, String message);
    void onError(String message);
}

package com.makepe.blackout.GettingStarted.OtherClasses;

import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationListener extends RecyclerView.OnScrollListener {
    public static final int PAGE_START = 1;
    public static final int PAGE_SIZE = 10;

    private LinearLayoutManager layout;

    public PaginationListener(LinearLayoutManager layout) {
        this.layout = layout;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layout.getChildCount();
        int totalItemCount = layout.getChildCount();
        int firstVisibleItemPosition = layout.findFirstVisibleItemPosition();

        if (!isLoading() && isLastPage()){
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= PAGE_SIZE){
                loadMoreItems();
            }
        }
    }

    protected abstract void loadMoreItems();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();
}

package com.makepe.blackout.GettingStarted.Fragments;

import static com.makepe.blackout.GettingStarted.OtherClasses.PaginationListener.PAGE_START;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.ExploreAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.SearchActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.PaginationListener;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;

public class ExploreImagesFragment extends Fragment {

    private ArrayList<String> exploreItems;
    private RecyclerView exploreRecycler;
    private ExploreAdapter exploreAdapter;

    private DatabaseReference postRef, userRef;
    private FirebaseUser firebaseUser;

    private ProgressDialog pd;
    private Toolbar exploreToolbar;

    private View view;

    /*private int currentPage = PAGE_START;
    private final boolean isLastPage = false;
    private final int totalPage = 10;
    private boolean isLoading = false;

    private SwipeRefreshLayout refreshLayout;*/

    private LinearLayoutManager layoutManager;

    public ExploreImagesFragment() {
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
        view = inflater.inflate(R.layout.fragment_explore_images, container, false);

        exploreRecycler = view.findViewById(R.id.exploreRecycler);
        exploreToolbar = view.findViewById(R.id.exploreImageToolbar);
        //refreshLayout = view.findViewById(R.id.exploreRefresher);

        ((AppCompatActivity)getActivity()).setSupportActionBar(exploreToolbar);

        postRef = FirebaseDatabase.getInstance().getReference("SecretPosts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.show();

        exploreItems = new ArrayList<>();

        declareRecycler();
        getAllExploreItems();

        /*refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doApiCall();
                    }
                }, 5000);
            }
        });

        refreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_red_dark
        );

        exploreRecycler.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage ++;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                refreshLayout.setEnabled(isLastItemDisplayed(exploreRecycler));
            }
        });*/

        return view;
    }

    /*private boolean isLastItemDisplayed(RecyclerView recyclerView){
        if(recyclerView.getAdapter().getItemCount() != 0){
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1;
        }
        return false;
    }

    private void doApiCall() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refreshLayout != null){
                    refreshLayout.setRefreshing(true);
                }
                getAllExploreItems();
                exploreAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);

            }
        }, 1500);
    }*/

    private void getAllExploreItems() {
        getNormalPostsItems();
        getExploreUsers();

        pd.dismiss();
    }

    private void declareRecycler() {
        exploreItems.clear();

        exploreRecycler.hasFixedSize();
        exploreRecycler.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false);
        exploreRecycler.setLayoutManager(layoutManager);

    }

    private void getExploreUsers() {

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    User user = snap.getValue(User.class);
                    assert user != null;
                    if (!user.getUserID().equals(firebaseUser.getUid()))
                        exploreItems.add(user.getUserID());
                }

                updateRecycler();

                exploreAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Cannot retrieve users", Toast.LENGTH_SHORT).show();}
        });
    }

    private void getNormalPostsItems() {
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    try{
                        assert postModel != null;
                        if (!postModel.getPostPrivacy().equals("Private")
                                && postModel.getPostType().equals("imagePost")
                                || postModel.getPostType().equals("audioImagePost"))
                            exploreItems.add(postModel.getPostID());

                    }catch (NullPointerException ignored){}

                }
                updateRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateRecycler() {
        Collections.shuffle(exploreItems);

        exploreAdapter = new ExploreAdapter(exploreItems, getContext());
        exploreRecycler.setAdapter(exploreAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.explore_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        View menuView = view.findViewById(item.getItemId());

        switch (item.getItemId()){
            case R.id.exploreSearch:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.filterExplore:

                PopupMenu popupMenu = new PopupMenu(getActivity(), menuView);
                popupMenu.getMenu().add(0, 0, Menu.NONE, "All");
                popupMenu.getMenu().add(0, 1, Menu.NONE, "Posts");
                popupMenu.getMenu().add(0, 2, Menu.NONE, "Users");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                pd.setMessage("Loading...");
                                pd.show();

                                declareRecycler();
                                getAllExploreItems();
                                pd.dismiss();
                                break;

                            case 1:
                                pd.setMessage("Loading Posts");
                                pd.show();

                                declareRecycler();
                                getNormalPostsItems();
                                pd.dismiss();

                                break;

                            case 2:
                                pd.setMessage("Loading Users");
                                pd.show();

                                declareRecycler();
                                getExploreUsers();
                                pd.dismiss();

                                break;

                            default:
                                Toast.makeText(getActivity(), "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
                break;

            default:
                Toast.makeText(getContext(), "Unknown Selection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

}
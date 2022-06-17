package com.makepe.blackout.GettingStarted.Fragments;

import static com.makepe.blackout.GettingStarted.OtherClasses.PaginationListener.PAGE_START;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.Adapters.StoryAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.CameraActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MessagesActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.Data;
import com.makepe.blackout.GettingStarted.OtherClasses.PaginationListener;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class TimelineFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, followReference, storyReference, postReference;

    private CircleImageView homeProPic;
    private Toolbar homeToolbar;

    //for stories
    private RecyclerView storyRecycler;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    //for followers
    private List<String> followingList;

    //for posts
    private RecyclerView postRecycler;
    private ArrayList<PostModel> postList;
    private PostAdapter postAdapter;

    private ProgressBar timelineLoader;

    private ExtendedFloatingActionButton scrollFAB;

    private SwipeRefreshLayout refreshLayout;

    private String last_key = "", last_node = "";
    private boolean isMaxData = false, isScrolling = false;
    private final int ITEM_LOAD_COUNT = 10;
    private int currentItems, totalItems, scrolledOutItems;

    public TimelineFragment() {
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
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        ButterKnife.bind(getActivity());

        homeProPic = view.findViewById(R.id.homePic);
        postRecycler = view.findViewById(R.id.postRecycler);
        storyRecycler = view.findViewById(R.id.viewStoryRecycler);
        homeToolbar = view.findViewById(R.id.timelineToolbar);
        timelineLoader = view.findViewById(R.id.timelineLoader);
        scrollFAB = view.findViewById(R.id.scrollToTheTopBTN);
        refreshLayout = view.findViewById(R.id.timelineRefresher);

        ((AppCompatActivity)getActivity()).setSupportActionBar(homeToolbar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        storyList = new ArrayList<>();
        postList = new ArrayList<>();

        //for post recycler view
        postRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(true);
        postRecycler.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(getActivity());
        postRecycler.setAdapter(postAdapter);

        //for stories recycler view
        storyRecycler.setHasFixedSize(true);
        storyRecycler.setNestedScrollingEnabled(false);
        storyRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        getUserDetails();
        checkFollowing();
        getLastKeyFromFirebase();

        storyAdapter = new StoryAdapter(getContext(), storyList);
        storyRecycler.setAdapter(storyAdapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        readAppropriatePosts();
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

        postRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // scrolling down
                    scrollFAB.setVisibility(View.GONE);
                } else if (dy < 0) { // scrolling up
                    scrollFAB.setVisibility(View.VISIBLE);
                }

                currentItems = layoutManager.getChildCount();
                totalItems = layoutManager.getItemCount();

                scrolledOutItems = layoutManager.findFirstVisibleItemPosition();

                if (isScrolling && currentItems + scrolledOutItems == totalItems){
                    isScrolling = false;
                    timelineLoader.setVisibility(View.VISIBLE);
                    readAppropriatePosts();
                }
            }
        });

        scrollFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRecycler.scrollToPosition(0);
            }
        });

        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();

        followReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                readAppropriatePosts();
                //doApiCall();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readStory() {
        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", firebaseUser.getUid(),
                        "", "", "", "", "",
                        0, 0, 0, 0));

                for(String id : followingList){

                    int countStory = 0;
                    Story story = null;

                    for(DataSnapshot snapshot : dataSnapshot.child(id).getChildren()){
                        story = snapshot.getValue(Story.class);
                        if(timecurrent>story.getTimeStart() && timecurrent < story.getTimeEnd()){
                            countStory++;
                        }
                    }
                    if(countStory > 0){
                        storyList.add(story);
                    }
                }
                Collections.shuffle(storyList);
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readAppropriatePosts() {

        /*if (!isMaxData){
            Query query;

            if (TextUtils.isEmpty(last_node)){
                query = postReference.orderByKey().limitToFirst(ITEM_LOAD_COUNT);
            }else{
                query = postReference.orderByKey().startAt(last_node).limitToFirst(ITEM_LOAD_COUNT);
            }

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()){
                        postList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            PostModel postModel = ds.getValue(PostModel.class);

                            assert postModel != null;
                            for (String ID : followingList){
                                if (postModel.getUserID().equals(ID))
                                    if (!postModel.getPostType().equals("videoPost")
                                            && !postModel.getPostType().equals("sharedVideoPost")
                                            && !postModel.getPostType().equals("audioVideoPost"))
                                        postList.add(postModel);
                            }

                            if (postModel.getUserID().equals(firebaseUser.getUid()))
                                if (!postModel.getPostType().equals("videoPost")
                                        && !postModel.getPostType().equals("sharedVideoPost")
                                        && !postModel.getPostType().equals("audioVideoPost"))
                                    postList.add(postModel);
                        }

                        last_node = postList.get(postList.size() - 1).getPostID();

                        if(!last_node.equals(last_key))
                            postList.remove(postList.size() - 1);
                        else
                            last_node = "end";

                        Collections.shuffle(postList);
                        postAdapter.addAll(postList);
                        postAdapter.notifyDataSetChanged();

                    }else{
                        isMaxData = true;
                    }

                    timelineLoader.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            timelineLoader.setVisibility(View.GONE);
        }*/

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    for (String ID : followingList){
                        if (postModel.getUserID().equals(ID))
                            if (!postModel.getPostType().equals("videoPost")
                                    && !postModel.getPostType().equals("sharedVideoPost")
                                    && !postModel.getPostType().equals("audioVideoPost"))
                                postList.add(postModel);
                    }

                    if (postModel.getUserID().equals(firebaseUser.getUid()))
                        if (!postModel.getPostType().equals("videoPost")
                                && !postModel.getPostType().equals("sharedVideoPost")
                                && !postModel.getPostType().equals("audioVideoPost"))
                            postList.add(postModel);

                }

                Collections.shuffle(postList);
                postAdapter = new PostAdapter(getActivity(), postList);
                postRecycler.setAdapter(postAdapter);
                timelineLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getLastKeyFromFirebase(){
        Query getLastKey = postReference.orderByKey().limitToLast(1);

        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot lastKey : snapshot.getChildren()){
                    last_key = lastKey.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserDetails() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(homeProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(homeProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.timeline_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.inbox:
                startActivity(new Intent(getActivity(), MessagesActivity.class));
                break;
            case R.id.newPostBTN:
                startActivity(new Intent(getActivity(), CameraActivity.class));
                break;

            default:
                Toast.makeText(getContext(), "Unknown Selection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
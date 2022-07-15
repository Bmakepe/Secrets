package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.makepe.blackout.GettingStarted.Fragments.CallHistoryFragment;
import com.makepe.blackout.GettingStarted.Fragments.ChatListFragment;
import com.makepe.blackout.GettingStarted.Fragments.GroupListFragment;
import com.makepe.blackout.GettingStarted.OtherClasses.ViewPagerAdapter;
import com.makepe.blackout.R;

public class MessagesActivity extends AppCompatActivity {

    private ViewPager messagesPager;
    private FloatingActionButton messagesFAB;

    int[] colorIntArray = {R.color.colorPrimaryDark, R.color.colorPrimary};
    int[] iconIntArray = {R.drawable.ic_contacts_black_24dp, R.drawable.ic_group_add_black_24dp};

    /*int[] colorIntArray = {R.color.colorPrimaryDark, R.color.colorPrimary, R.color.backGroundLeft};
    int[] iconIntArray = {R.drawable.ic_contacts_black_24dp, R.drawable.ic_group_add_black_24dp, R.drawable.ic_baseline_add_ic_call_24};*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        messagesPager = findViewById(R.id.messagesPager);
        TabLayout messagesTabs = findViewById(R.id.messageTabs);
        messagesFAB = findViewById(R.id.messagesFAB);
        Toolbar chatListToolbar = findViewById(R.id.messagesToolbar);
        setSupportActionBar(chatListToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatListFragment(), "Chats");
        viewPagerAdapter.addFragment(new GroupListFragment(), "Groups");
        //viewPagerAdapter.addFragment(new CallHistoryFragment(), "Calls");
        messagesPager.setAdapter(viewPagerAdapter);
        messagesTabs.setupWithViewPager(messagesPager);

        messagesFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessagesActivity.this, MyContactsActivity.class));
            }
        });

        messagesTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                messagesPager.setCurrentItem(tab.getPosition());
                animateFAB(tab.getPosition());

                switch (tab.getPosition()){
                    case 0:
                      messagesFAB.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              startActivity(new Intent(MessagesActivity.this, MyContactsActivity.class));
                          }
                      });
                      break;

                    case 1:
                        messagesFAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(MessagesActivity.this, CreateGroupActivity.class));
                            }
                        });
                        break;

                    /*case 2:
                        messagesFAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(MessagesActivity.this, CallUserListActivity.class));
                            }
                        });
                        break;*/

                    default:
                        Toast.makeText(MessagesActivity.this, "Unknown error detected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void animateFAB(int position) {
        messagesFAB.clearAnimation();

        // Scale down animation
        ScaleAnimation shrink = new ScaleAnimation(1f, 0.1f, 1f, 0.1f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(100);     // animation duration in milliseconds
        shrink.setInterpolator(new AccelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                messagesFAB.setBackgroundTintList(ContextCompat.getColorStateList(MessagesActivity.this, colorIntArray[position]));
                messagesFAB.setImageDrawable(ContextCompat.getDrawable(MessagesActivity.this, iconIntArray[position]));

                // Rotate Animation
                Animation rotate = new RotateAnimation(60.0f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);
                rotate.setDuration(150);
                rotate.setInterpolator(new DecelerateInterpolator());

                // Scale up animation
                ScaleAnimation expand = new ScaleAnimation(0.1f, 1f, 0.1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(150);     // animation duration in milliseconds
                expand.setInterpolator(new DecelerateInterpolator());

                // Add both animations to animation state
                AnimationSet s = new AnimationSet(false); //false means don't share interpolators
                s.addAnimation(rotate);
                s.addAnimation(expand);
                messagesFAB.startAnimation(s);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        messagesFAB.startAnimation(shrink);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.chatlistSearch:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
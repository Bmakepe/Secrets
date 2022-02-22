package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.ContactsAdapter;
import com.makepe.blackout.GettingStarted.CountryIso2Phone;
import com.makepe.blackout.GettingStarted.MainActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ContactsActivity extends AppCompatActivity {

    private TextView textView;
    private RecyclerView contactsRecycler;
    private SearchManager searchManager;

    private ImageView searchView;

    private Dialog searchDialog;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactsRecycler = findViewById(R.id.contactsRecycler);
        searchView = findViewById(R.id.contactSearch);
        textView = findViewById(R.id.selectTV);

        iniSearchDialog();

        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ContactsActivity.this);
        contactsRecycler.setLayoutManager(linearLayoutManager);

        searchManager = (SearchManager) getSystemService(ContactsActivity.SEARCH_SERVICE);

        List<ContactsModel> contactsList = new ArrayList<>();
        ContactsList contactsList1 = new ContactsList(contactsList, ContactsActivity.this);

        contactsList1.readContacts();

        ContactsAdapter contactsAdapter = new ContactsAdapter(contactsList, ContactsActivity.this);
        contactsRecycler.setAdapter(contactsAdapter);

        findViewById(R.id.cBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.createGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ContactsActivity.this, "You will be able to create a group", Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.show();
            }
        });
    }

    private void iniSearchDialog() {
        searchDialog = new Dialog(ContactsActivity.this);
        searchDialog.setContentView(R.layout.search_pop_up);
        searchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        searchDialog.findViewById(R.id.searchBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.dismiss();
            }
        });
    }

    public void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            /*User is signed in stay on this activity
            set phone number of logged in user
             */
            uid = user.getUid();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        //dbRef.keepSynced(true);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //set last seen time
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

}

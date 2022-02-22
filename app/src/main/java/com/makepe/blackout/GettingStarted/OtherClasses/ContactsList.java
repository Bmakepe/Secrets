package com.makepe.blackout.GettingStarted.OtherClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.makepe.blackout.GettingStarted.CountryIso2Phone;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class ContactsList {

    private List<ContactsModel> contactsList = new ArrayList<>();
    private HashSet<String> mobileNoSet = new HashSet<>();
    public Context context;
    public String name, phone;

    public ContactsList(List<ContactsModel> contactsList, Context context) {
        this.contactsList = contactsList;
        this.context = context;
    }

    public ContactsList() {
    }

    public void readContacts() {
        try{
            contactsList.clear();
            String ISOPrefix = getCountryISO();

            @SuppressLint("Recycle") Cursor phones = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);

            assert phones != null;
            while(phones.moveToNext()){
                name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String pic = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                if(!String.valueOf(phone.charAt(0)).equals("+"))
                    phone = ISOPrefix + phone;

                if(!mobileNoSet.contains(phone)){
                    contactsList.add(new ContactsModel(name, phone, pic, id));
                    mobileNoSet.add(phone);
                }

                Collections.sort(contactsList, new Comparator<ContactsModel>() {
                    @Override
                    public int compare(ContactsModel o1, ContactsModel o2) {
                        return o1.getUsername().compareTo(o2.getUsername());
                    }
                });

            }

        }catch (NullPointerException ignored){}

    }

    private String getCountryISO(){
        String iso = null;

        try{
            TelephonyManager telephonyManager = (TelephonyManager)context.getApplicationContext().getSystemService(context.getApplicationContext().TELEPHONY_SERVICE);
            if(telephonyManager.getNetworkCountryIso() != null){
                if(!telephonyManager.getNetworkCountryIso().toString().equals("")){
                    iso = telephonyManager.getNetworkCountryIso().toString();
                }
            }

        }catch (NullPointerException ignored){

        }

        return CountryIso2Phone.getPhone(iso);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<ContactsModel> getContactsList() {
        return Collections.unmodifiableList(contactsList);
    }

    public void setContactsList(List<ContactsModel> contactsList) {
        this.contactsList = contactsList;
    }
}

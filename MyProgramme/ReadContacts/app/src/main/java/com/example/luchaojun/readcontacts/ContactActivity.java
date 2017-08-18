package com.example.luchaojun.readcontacts;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by luchaojun on 8/18/17.
 */

public class ContactActivity extends Activity{

    private List<Contact> contacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        contacts = ReadContactUtils.readContact(getApplicationContext());
        ListView listView = (ListView)findViewById(R.id.lv_contact);
        listView.setAdapter(new MyListAdapter());
    }
    private class MyListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contacts.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            if(view == null){
                v = View.inflate(getApplicationContext(),R.layout.item,null);
            }else{
                v = view;
            }
            TextView textView1 = (TextView)v.findViewById(R.id.tv_id);
            TextView textView2 = (TextView)v.findViewById(R.id.tv_name);
            TextView textView3 = (TextView)v.findViewById(R.id.tv_phone);
            TextView textView4 = (TextView)v.findViewById(R.id.tv_email);
            textView1.setText(contacts.get(i).getId());
            textView2.setText(contacts.get(i).getName());
            textView3.setText(contacts.get(i).getPhone());
            textView4.setText(contacts.get(i).getEmail());
            return v;
        }
    }
}

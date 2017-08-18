package com.example.luchaojun.insertcontacts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText et_name;
    private EditText et_phone;
    private EditText et_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_name = (EditText) findViewById(R.id.et_name);
        et_phone = (EditText) findViewById(R.id.et_pone);
        et_email = (EditText) findViewById(R.id.et_email);
    }
    public void click(View view){
        String name = et_name.getText().toString().trim();
        String phone = et_phone.getText().toString().trim();
        String email = et_email.getText().toString().trim();

        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri uri2 = Uri.parse("content://com.android.contacts/data");

        Cursor query = getContentResolver().query(uri, null, null, null, null);
        int queryCount = query.getCount();
        int contact_id = queryCount+1;
        ContentValues value1 = new ContentValues();
        value1.put("contact_id",contact_id);
        getContentResolver().insert(uri,value1);

        ContentValues namevalue = new ContentValues();
        namevalue.put("data1",name);
        namevalue.put("raw_contact_id",contact_id);
        namevalue.put("mimetype","vnd.android.cursor.item/name");
        getContentResolver().insert(uri2,namevalue);

        ContentValues phonevalue = new ContentValues();
        phonevalue.put("data1",phone);
        phonevalue.put("raw_contact_id",contact_id);
        phonevalue.put("mimetype","vnd.android.cursor.item/phone_v2");
        getContentResolver().insert(uri2,phonevalue);

        ContentValues emailvalue = new ContentValues();
        emailvalue.put("data1",email);
        emailvalue.put("raw_contact_id",contact_id);
        emailvalue.put("mimetype","vnd.android.cursor.item/email_v2");
        getContentResolver().insert(uri2,emailvalue);
    }
}

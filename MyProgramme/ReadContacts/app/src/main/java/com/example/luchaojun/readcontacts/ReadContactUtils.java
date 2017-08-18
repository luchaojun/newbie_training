package com.example.luchaojun.readcontacts;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luchaojun on 8/18/17.
 */

public class ReadContactUtils {
    public static List<Contact> readContact(Context context){
        List<Contact> list = new ArrayList<>();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"contact_id"}, null, null, null);
        Uri uri2 = Uri.parse("content://com.android.contacts/data");

        while(cursor.moveToNext()){
            String contact_id = cursor.getString(0);
            if(contact_id != null){
                Contact contact = new Contact();
                contact.setId(contact_id);
                Cursor cursor2 = context.getContentResolver().query(uri2, new String[]{"data1","mimetype"}, "raw_contact_id=?", new String[]{contact_id}, null);
                while(cursor2.moveToNext()){
                    String data1 = cursor2.getString(0);
                    System.out.println(data1);
                    String mimetype = cursor2.getString(1);
                    if("vnd.android.cursor.item/name".equals(mimetype)){
                        contact.setName(data1);
                    }else if("vnd.android.cursor.item/phone_v2".equals(mimetype)){
                        contact.setPhone(data1);
                    }else if("vnd.android.cursor.item/email_v2".equals(mimetype)){
                        contact.setEmail(data1);
                    }
                }
                if(contact.getName() != null){
                    list.add(contact);
                }
            }
        }
        return list;
    }
}

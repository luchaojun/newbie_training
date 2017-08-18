package com.example.luchaojun.readmessage;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void click(View view){
        try{
            Uri uri = Uri.parse("content://sms/");
            Cursor cursor = getContentResolver().query(uri, new String[]{"address", "date", "body"}, null, null, null);
            XmlSerializer xs = Xml.newSerializer();
            File file = new File(Environment.getDownloadCacheDirectory().getPath(),"sms.xml");
            FileOutputStream fos = new FileOutputStream(file);
            xs.setOutput(fos,"utf-8");
            xs.startDocument("utf-8",true);
            while(cursor.moveToNext()){
                String address = cursor.getString(0);
                String date = cursor.getString(1);
                String body = cursor.getString(2);
                xs.startTag(null,"smss");
                xs.startTag(null,"address");
                xs.text(address);
                xs.endTag(null,"address");
                xs.startTag(null,"date");
                xs.text(date);
                xs.endTag(null,"date");
                xs.startTag(null,"body");
                xs.text(body);
                xs.endTag(null,"body");
                xs.endTag(null,"smss");
            }
            xs.endDocument();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

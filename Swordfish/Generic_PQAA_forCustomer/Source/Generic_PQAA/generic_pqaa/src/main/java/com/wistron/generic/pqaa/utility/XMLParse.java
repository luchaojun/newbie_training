package com.wistron.generic.pqaa.utility;

import android.content.Context;
import android.util.Log;

import com.wistron.generic.pqaa.R;
import com.wistron.generic.pqaa.utility.DataContentProvider.RunInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class XMLParse {
    private final Context context;
    private final String TAG_ITEM = "item";
    private final LinkedHashMap<String, String> mItemsList;

    public XMLParse(Context context) {
        super();
        this.context = context;
        mItemsList = new LinkedHashMap<String, String>();
    }

    public LinkedHashMap<String, String> getItemListFromXML(boolean isAutomatic) {
        try {
            XmlPullParser parser = null;
//			if (isPCBStage) {
//				parser = context.getResources().getXml(R.xml.config_pcba);
//			} else {
//				parser = context.getResources().getXml(R.xml.config_fa);
//			}
            if (isAutomatic) {
                parser = context.getResources().getXml(R.xml.automatic);
            } else {
                parser = context.getResources().getXml(R.xml.config);
            }
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    log("Start document:");
                } else if (eventType == XmlPullParser.END_DOCUMENT) {
                    log("End document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (TAG_ITEM.equals(parser.getName())) {
                        mItemsList.put(parser.getAttributeValue(null, RunInfo.ITEM), parser.getAttributeValue(null, RunInfo.ARG1) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG2) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG3) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG4) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG5) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG6) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG7) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG8) + ":" +
                                parser.getAttributeValue(null, RunInfo.ARG9));
                    }
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            Log.w("XMLParse", "Got exception parsing favorites.", e);
        } catch (IOException e) {
            Log.w("XMLParse", "Got exception parsing favorites.", e);
        }
        return mItemsList;
    }

    private void log(String content) {
        Log.i("XMLParse", content);
    }
}

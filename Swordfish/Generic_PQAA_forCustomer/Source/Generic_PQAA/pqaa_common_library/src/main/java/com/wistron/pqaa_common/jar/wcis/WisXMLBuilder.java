package com.wistron.pqaa_common.jar.wcis;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;

public class WisXMLBuilder extends Builder {
	private XmlSerializer serializer = Xml.newSerializer();
	private StringWriter writer = new StringWriter();

	public WisXMLBuilder(Context context) {
		super();
	}

	@Override
	public void date() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getResult() {
		// TODO Auto-generated method stub
		return writer.toString();
	}

	/**
	 * generate current wcis test item XML log
	 * @param item
	 * current wcis test item
	 */
	public void makeItem(WisTestItem item){
		try {
			//MARK
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", false);
			serializer.startTag(null, "testrun");
			serializer.attribute(null, "name", item.getTestItemActivityName());
			serializer.attribute(null, "project", item.getTestItemActivityName());
			serializer.attribute(null, "tests", String.valueOf(1));
			serializer.attribute(null, "started", String.valueOf(1));
			serializer.attribute(null, "failures", String.valueOf(1));
			serializer.attribute(null, "errors", String.valueOf(0));
			serializer.attribute(null, "ignored", String.valueOf(0));
			serializer.startTag(null, "testsuite");
			serializer.attribute(null, "name", item.getTestItemPackageName());
			serializer.attribute(null, "time", "N/A");
			serializer.startTag(null, "testcase");
			serializer.attribute(null, "name", item.getTestItemName());
			serializer.attribute(null, "classname", item.getTestItemPackageName());
			serializer.attribute(null, "time", "N/A");
			if(item.getTestItemResult() == WisTestItem.RESULT_FAIL){
				serializer.startTag(null, "failure");
				serializer.text(item.getTestRemarks());
				serializer.endTag(null, "failure");
			}
			serializer.endTag(null, "testcase");
			serializer.endTag(null, "testsuite");
			serializer.endTag(null, "testrun");
			
			serializer.endDocument();
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void makeString(String str) {
		// TODO Auto-generated method stub

	}

	@Override
	public void makeTitle(String title) {
		// TODO Auto-generated method stub

	}

}

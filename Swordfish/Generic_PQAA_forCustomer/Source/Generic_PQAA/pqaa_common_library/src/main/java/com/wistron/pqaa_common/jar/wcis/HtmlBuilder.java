package com.wistron.pqaa_common.jar.wcis;

import android.content.Context;
import android.text.format.DateFormat;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class HtmlBuilder extends Builder{
	private Context context;
	protected StringBuffer writer = new StringBuffer();

	public HtmlBuilder(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public String getResult() {
		// TODO Auto-generated method stub
		writer.append("</pre></html>");
		return writer.toString();
	}

	public void date() {
		// TODO Auto-generated method stub
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		SimpleDateFormat mFormat = null;
		if (!DateFormat.is24HourFormat(context)) {
			mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		} else {
			mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		writer.append("<table align=center width=800>");
		writer.append("<tr><th align=right>" + mFormat.format(new Date(mCalendar.getTimeInMillis())).trim()
				+ "</th></tr>");
		writer.append("</table>");
	}

	public void makeString(String str) {
		// TODO Auto-generated method stub
		writer.append("<p align=left>" + str + "<p>");
	}

	/* (non-Javadoc)
	 * @see com.wistron.pqaa_common.jar.wcis.Builder#makeTitle(java.lang.String)
	 */
	public void makeTitle(String title) {
		// TODO Auto-generated method stub
		writer.append("<html><pre><head><title align=center>" + title + "</title></head>");
		writer.append("<h1 align=center >" + title + "</h1>");
		
		writer.append("<meta http-equiv=Content-Language content=zh-cn>");
		writer.append("<meta http-equiv=Content-Type content=text/html;charset=gb2312>");
	}
	
	/**
	 * Set HTML head and title
	 * @param header
	 * @param title
	 */
	public void makeTitle(String header, String title) {
		// TODO Auto-generated method stub
		writer.append("<html><pre><head><title align=center>" + header + "</title></head>");
		writer.append("<h1 align=center >" + title + "</h1>");
		
		writer.append("<meta http-equiv=Content-Language content=zh-cn>");
		writer.append("<meta http-equiv=Content-Type content=text/html;charset=gb2312>");
	}
}

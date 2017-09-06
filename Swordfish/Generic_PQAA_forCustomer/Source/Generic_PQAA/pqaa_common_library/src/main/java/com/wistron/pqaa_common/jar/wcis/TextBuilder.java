package com.wistron.pqaa_common.jar.wcis;

import android.content.Context;

public class TextBuilder extends Builder{
	private StringBuffer writer = new StringBuffer();

	public TextBuilder(Context context) {
		super();
	}

	public String getResult() {
		// TODO Auto-generated method stub
		writer.append("======================\n");
		return writer.toString();
	}

	@Override
	public void makeString(String str) {
		// TODO Auto-generated method stub
		writer.append(str + "\n");
	}

	@Override
	public void makeTitle(String title) {
		// TODO Auto-generated method stub
		writer.append("=========================\n");
		writer.append(title + "\n");
	}

	@Override
	public void date() {
		// TODO Auto-generated method stub

	}
}

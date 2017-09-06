package com.wistron.pqaa_common.jar.global;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author King
 * the class that used to parse test parameters from database
 */
public class WisParseValue {
	private final ContentResolver mResolver;
	private final Cursor mCursor;
	private String arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8,arg9;
	
	/**
	 * for PQAA, Generic PQAA, WCIS or RunIn  test construct method,
	 * 
	 * @param context
	 * context
	 * @param item
	 * the test item
	 * @param authorities
	 * current database authorities
	 */
	public WisParseValue(Context context, String item,String authorities) {
		// TODO Auto-generated constructor stub
		mResolver = context.getContentResolver();
		mCursor = mResolver.query(Uri.parse("content://" + authorities + "/test"), null, "item=?", new String[] { item }, null);
		if (mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			arg1 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg1"));
			arg2 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg2"));
			arg3 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg3"));
			arg4 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg4"));
			arg5 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg5"));
			arg6 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg6"));
			arg7 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg7"));
			arg8 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg8"));
			arg9 = mCursor.getString(mCursor.getColumnIndexOrThrow("arg9"));
		}
		mCursor.close();
	}

	/**
	 * @return
	 * Return the the first parameter of current test item
	 */
	public String getArg1() {
		return arg1;
	}

	/**
	 * @return
	 * Return the the second parameter of current test item
	 */
	public String getArg2() {
		return arg2;
	}

	/**
	 * @return
	 * Return the the third parameter of current test item
	 */
	public String getArg3() {
		return arg3;
	}

	/**
	 * @return
	 * Return the the fourth parameter of current test item
	 */
	public String getArg4() {
		return arg4;
	}

	/**
	 * @return
	 * Return the the fifth parameter of current test item
	 */
	public String getArg5() {
		return arg5;
	}

	/**
	 * @return
	 * Return the the sixth parameter of current test item
	 */
	public String getArg6() {
		return arg6;
	}

	/**
	 * @return
	 * Return the the seventh parameter of current test item
	 */
	public String getArg7() {
		return arg7;
	}

	/**
	 * @return
	 * Return the the eighth parameter of current test item
	 */
	public String getArg8() {
		return arg8;
	}

	/**
	 * @return
	 * Return the the ninth parameter of current test item
	 */
	public String getArg9() {
		return arg9;
	}

}

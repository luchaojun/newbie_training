package com.wistron.pqaa_common.jar.wcis;

import android.content.Context;

public class WisSubHtmlBuilder extends HtmlBuilder {

	public WisSubHtmlBuilder(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * the Prefix of current test item log
	 */
	public void startTable(){
		writer.append("<hr width=800></hr>");
		writer.append("<br></br>");
		writer.append("<table align=center border=5 width=800>");
		writer.append("<tr><td align=center valign=middle>Index</td> <td align=center valign=middle>Result</td> <td align=center valign=middle>Remark</td></tr>");
	}
	
	/**
	 * add current test result to log file
	 * @param index
	 * test index
	 * @param result
	 * test result
	 * @param remark
	 * test remark
	 */
	public void addTableItem(int index,boolean result,String remark){
		writer.append("<tr>");
		writer.append("<td width=50 align=center>" + index + "</td>");

		if (result) {
			writer.append("<th  width=50 align=center> <font color=green>PASS</font></th>");
		} else {
			writer.append("<th  width=50 align=center> <font color=red>FAIL</font></th>");
		}
		writer.append("<td align=center style=word-wrap:break-word>" + remark+ "</td>");
		writer.append("</tr>");
	}
	
	/**
	 * the Suffix of current test item log
	 */
	public void endTable(){
		writer.append("</table>");
		writer.append("<br></br>");
		writer.append("<hr width=800></hr>");
	}
}

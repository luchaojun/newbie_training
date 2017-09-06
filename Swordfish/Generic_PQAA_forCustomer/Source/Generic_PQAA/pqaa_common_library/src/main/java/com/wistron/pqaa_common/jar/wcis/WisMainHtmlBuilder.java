package com.wistron.pqaa_common.jar.wcis;

import android.content.Context;

import java.util.ArrayList;

public class WisMainHtmlBuilder extends HtmlBuilder {

	public WisMainHtmlBuilder(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * add test item to log file
	 * @param mCurrentList
	 * current wcis test item list
	 */
	public void makeItems(ArrayList<WisTestItem> mCurrentList) {
		// TODO Auto-generated method stub
		// writer.append("<ul>");
		boolean isNeedFactoryReset=false;
		writer.append("<hr width=800></hr>");
		writer.append("<br></br>");
		writer.append("<table align=center border=5 width=800>");
		writer.append("<tr><td align=center valign=middle>Index</td> <td align=center valign=middle>Item</td> <td align=center valign=middle>Result</td> <td align=center valign=middle>Remark</td></tr>");
		for (WisTestItem item : mCurrentList) {
			if (item.getTestItemName().equals("FactoryReset")) {
				if (item.isChecked() && item.isInstalled()) {
					isNeedFactoryReset = true;
				}
				continue;
			}
			writer.append("<tr>");
			writer.append("<td width=50 align=center>" + (mCurrentList.indexOf(item) + 1) + "</td>");
			// if (mLanguage == Main.LANGUAGE_CHINESE_SIMPLE) {
			// writer.append("<td width=200 align=center>" +
			// item.getWisTestItemCNName() + "</td>");
			// } else {
			writer.append("<td width=200 align=center>"
					+ item.getTestItemName() + "</td>");
			// }

			if (item.getTestItemResult() == WisTestItem.RESULT_PASS) {
				writer.append("<th  align=center> <font color=green>PASS</font></th>");
			} else if (item.getTestItemResult() == WisTestItem.RESULT_FAIL) {
				writer.append("<th  align=center> <font color=red>FAIL</font></th>");
			} else if (item.getTestItemResult() == WisTestItem.RESULT_TIMEOUT) {
				writer.append("<th  align=center> <font color=red>TIMEOUT</font></th>");
			} else {
				writer.append("<th  align=center> <font color=gray>----</font></th>");
			}
			writer.append("<td width=450 align=center>" + item.getTestRemarks() + "</td>");
			// <a href="+item.getLogName()+" target=new> </a>"
			writer.append("</tr>");
		}

		writer.append("</table>");
		if (isNeedFactoryReset) {
			writer.append("<table align=center width=800>");
			writer.append("<tr><th align=left> <font color=red>" + "Note: Device will do factory reset after WCIS smoke test tool set!"+ "</font></th></tr>");
			writer.append("</table>");
		}
		writer.append("<br></br>");
		writer.append("<hr width=800></hr>");
	}
}

package com.wistron.pqaa_common.jar.wcis;

public class WisWCISSubLogItem {
	private boolean isPass;
	private String remark;
	
	public WisWCISSubLogItem(boolean isPass, String remark) {
		super();
		this.isPass = isPass;
		this.remark = remark;
	}
	public boolean isPass() {
		return isPass;
	}
	public void setPass(boolean isPass) {
		this.isPass = isPass;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}

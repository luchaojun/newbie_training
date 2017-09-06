package com.wistron.pqaa_common.jar.wcis;

import android.content.Context;

import java.io.Serializable;


public abstract class WisTestItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int RESULT_DEFAULT = 0;
	public static final int RESULT_PASS = 1;
	public static final int RESULT_FAIL = 2;
	public static final int	RESULT_TIMEOUT = 3;
	
	public static final int GROUP_TYPE_FUNCTION = 0;
	public static final int GROUP_TYPE_SENSE = 1;
	public static final int GROUP_TYPE_LONGRUN = 2;

	protected Context context;
	private String testItemName;
	private String testItemCmdLine;
	private String testItemPackageName;
	private String testItemActivityName;
	private String testItemCNName;
	private String testItemVersion;
	private int testItemGroupType;
	private boolean installed;
	private boolean checked;
	private boolean PCBATestStage;
	private boolean isFirstInGroup;
	private int testItemResult;
	private String testRemarks;
	private String configFolderPath;
	private String logFolderPath;
	
	private int	timeoutElapse = 0;
	
	public WisTestItem(Context mContext, String mTestItemName) {
		super();
		this.context=mContext;
		this.testItemName = mTestItemName;
		this.testItemCmdLine = "";
		this.testItemResult = RESULT_DEFAULT;
		setPackageAndActivityName();
		this.checked = true;
		this.testRemarks = "";
	}
	
	public abstract void setPackageAndActivityName();

	public String getTestItemName() {
		return testItemName;
	}

	public void setTestItemName(String testItemName) {
		this.testItemName = testItemName;
	}

	public String getTestItemCmdLine() {
		return testItemCmdLine;
	}

	public void setTestItemCmdLine(String testItemCmdLine) {
		this.testItemCmdLine = testItemCmdLine;
	}

	public String getTestItemPackageName() {
		return testItemPackageName;
	}

	public void setTestItemPackageName(String testItemPackageName) {
		this.testItemPackageName = testItemPackageName;
	}

	public String getTestItemActivityName() {
		return testItemActivityName;
	}

	public void setTestItemActivityName(String testItemActivityName) {
		this.testItemActivityName = testItemActivityName;
	}

	public String getTestItemCNName() {
		return testItemCNName;
	}

	public void setTestItemCNName(String testItemCNName) {
		this.testItemCNName = testItemCNName;
	}

	public String getTestItemVersion() {
		return testItemVersion;
	}

	public void setTestItemVersion(String testItemVersion) {
		this.testItemVersion = testItemVersion;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isPCBATestStage() {
		return PCBATestStage;
	}

	public void setPCBATestStage(boolean pCBATestStage) {
		PCBATestStage = pCBATestStage;
	}

	public int getTestItemResult() {
		return testItemResult;
	}

	public void setTestItemResult(int testItemResult) {
		this.testItemResult = testItemResult;
	}

	public String getTestRemarks() {
		return testRemarks;
	}

	public void setTestRemarks(String testRemarks) {
		this.testRemarks = testRemarks;
	}

	public String getConfigFolderPath() {
		return configFolderPath;
	}

	public void setConfigFolderPath(String configFolderPath) {
		this.configFolderPath = configFolderPath;
	}
	
	public String getLogFolderPath() {
		return logFolderPath;
	}

	public void setLogFolderPath(String logFolderPath) {
		this.logFolderPath = logFolderPath;
	}

	public int getTimeoutElapse() {
		return timeoutElapse;
	}

	public void setTimeoutElapse(int timeoutElapse) {
		this.timeoutElapse = timeoutElapse;
	}

	public int getTestItemGroupType() {
		return testItemGroupType;
	}

	public void setTestItemGroupType(int testItemGroupType) {
		this.testItemGroupType = testItemGroupType;
	}

	public boolean isFirstInGroup() {
		return isFirstInGroup;
	}

	public void setFirstInGroup(boolean isFirstInGroup) {
		this.isFirstInGroup = isFirstInGroup;
	}
	
}

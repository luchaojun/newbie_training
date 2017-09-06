package com.wistron.pqaa_common.jar.wcis;

public abstract class Builder {
	/**
	 * add the title into the current log string
	 * @param title
	 * the log title
	 */
	public abstract void makeTitle(String title);
    /**
     * add a string into the log file
     * @param str
     * the log string
     */
    public abstract void makeString(String str);
    /**
     * Return the current log string
     * @return
     * return the log string
     */
    public abstract String getResult();
	/**
	 * add current data time
	 */
	public abstract void date();
}

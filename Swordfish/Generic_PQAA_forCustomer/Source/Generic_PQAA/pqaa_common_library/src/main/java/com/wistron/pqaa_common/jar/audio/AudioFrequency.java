package com.wistron.pqaa_common.jar.audio;

public class AudioFrequency {
	private int lowFrequency,highFrequency;
	private double decibel;

	public AudioFrequency(int lowFrequency, int highFrequency) {
		super();
		this.lowFrequency = lowFrequency;
		this.highFrequency = highFrequency;
	}
	
	public void resetFrequency(){
		this.lowFrequency = 0;
		this.highFrequency = 0;
		this.decibel = 0;
	}

	public int getLowFrequency() {
		return lowFrequency;
	}

	public int getHighFrequency() {
		return highFrequency;
	}

	public double getDecibel() {
		return decibel;
	}

	public void setDecibel(double decibel) {
		this.decibel = decibel;
	}
}

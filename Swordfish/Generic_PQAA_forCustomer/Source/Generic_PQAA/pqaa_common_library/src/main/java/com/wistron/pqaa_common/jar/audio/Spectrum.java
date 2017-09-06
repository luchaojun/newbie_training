package com.wistron.pqaa_common.jar.audio;

public class Spectrum {

	private double[] spectrum;
	private int length;

	public Spectrum(double[] spectrum) {
		this.spectrum = spectrum;
		this.length = spectrum.length;
	}

	public void normalize() {
		double maxValue = 0.0;

		for (int i = 0; i < length; ++i)
			if (maxValue < spectrum[i])
				maxValue = spectrum[i];

		if (maxValue != 0)
			for (int i = 0; i < length; ++i)
				spectrum[i] /= maxValue;
	}

	public double get(int index) {
		return spectrum[index];
	}

	public int length() {
		return length;
	}
	
	public double mDecibel = 0;

	public void setDecibel(double db) {
		mDecibel = db;
	}

	public double getDecibel() {
		// double decibel = 0;
		// for (int i = 0; i < spectrum.length; i++)
		// decibel += Math.abs(spectrum[i]);
		//
		// decibel = 10 * Math.log10(decibel);
		return mDecibel;
	}

}

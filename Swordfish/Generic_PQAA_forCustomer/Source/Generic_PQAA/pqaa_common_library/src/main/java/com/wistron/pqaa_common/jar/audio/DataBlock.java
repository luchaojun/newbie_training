package com.wistron.pqaa_common.jar.audio;

public class DataBlock {
	private double[] block;

	public DataBlock(short[] buffer, int blockSize, int bufferReadSize) {
		block = new double[blockSize];

		for (int i = 0; i < blockSize && i < bufferReadSize; i++) {
			block[i] = (double) buffer[i];
		}
	}

	public DataBlock() {
	}

	public void setBlock(double[] block) {
		this.block = block;
	}

	public double[] getBlock() {
		return block;
	}

	public Spectrum FFT() {
		double decibel = 0;
		for (int i = 0; i < block.length; i++)
			decibel += Math.abs(block[i]);

		decibel = 10 * Math.log10(decibel);
		Spectrum spectrum = new Spectrum(FFT.magnitudeSpectrum(block));
		spectrum.setDecibel(decibel);
		return spectrum;
		// return new Spectrum(FFT.magnitudeSpectrum(block));
		// Spectrum spectrum = new Spectrum(block);
		// spectrum.toFFT();
		// return spectrum;
	}
}

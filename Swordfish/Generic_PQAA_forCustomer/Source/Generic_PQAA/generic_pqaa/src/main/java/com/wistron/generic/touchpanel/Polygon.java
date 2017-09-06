package com.wistron.generic.touchpanel;

public class Polygon {
    private int[] polyX, polyY;
    private int polySides = 4;

    public Polygon(int[] polyX, int[] polyY) {
        super();
        this.polyX = polyX;
        this.polyY = polyY;
    }

    public boolean contains(float x, float y) {
        boolean compareResult = false;
        for (int i = 0, j = polySides - 1; i < polySides; j = i++) {
            if (polyY[i] < y && polyY[j] >= y || polyY[j] < y && polyY[i] >= y) {
                if (polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x) {
                    compareResult = !compareResult;
                }
            }
        }
        return compareResult;
    }
}

package com.example.inspiron.phonesavior.ui;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class TVAnimation extends Animation {
    private int mCenterWidth;
    private int mCenterHeight;
    private Camera mCamera = new Camera();
    private float mRotateY = 0.0F;

    public TVAnimation() {
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        this.setDuration(3000L);
        this.setFillAfter(true);
        this.setInterpolator(new AccelerateInterpolator());
        this.mCenterWidth = width / 2;
        this.mCenterHeight = height / 2;
    }

    public void setRotateY(float rorateY) {
        this.mRotateY = rorateY;
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        Matrix matrix = t.getMatrix();
        matrix.preScale(1.0F, 1.0F - interpolatedTime, (float)this.mCenterWidth, (float)this.mCenterHeight);
    }
}


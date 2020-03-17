package com.example.viewdemo.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;

import com.example.viewdemo.BaseSurfaceView;

/**
 * Created by Wang.Wenhui
 * Date: 2020/3/16
 */
public class TextView extends BaseSurfaceView {
    String text = "而王";
    private Path path;
    private TextPaint textPaint;
    private long duration = 5000;
    private long time = 0;
    private PathMeasure measure;

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onInit() {
        path = new Path();
        textPaint = new TextPaint();
        textPaint.setFakeBoldText(true);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(20);
        textPaint.setTextSize(600);
        measure = new PathMeasure();
    }

    @Override
    protected void onReady() {
        Path p = new Path();
        for (int i = 0; i < text.length(); i++) {
            String substring = text.substring(i, i + 1);
            textPaint.getTextPath(substring, 0, substring.length(), 0, 0, p);
            path.addPath(p, 300 * i, 800);
        }
        Path pp = new Path();
        pp.moveTo(10, 0);
        pp.lineTo(20, 10);
        pp.lineTo(0, 10);
        pp.close();
        PathEffect dash = new PathDashPathEffect(pp, 20, 0, PathDashPathEffect.Style.TRANSLATE);
        PathEffect discreteEffect = new DiscretePathEffect(50, 8);
        textPaint.setPathEffect(new ComposePathEffect(dash, discreteEffect));
        measure.setPath(path, false);
        startAnim();
    }

    @Override
    protected void onDataUpdate() {
        time += 16;
        if (time >= duration) {
            time = 0;
        }
    }

    @Override
    protected void onRefresh(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        Path dst = new Path();
        measure.getSegment(0, (float) time / duration * measure.getLength(), dst, true);
//        textPaint.setColor(Color.CYAN);
//        canvas.drawPath(path, textPaint);
        textPaint.setColor(Color.RED);
        canvas.drawPath(dst, textPaint);
    }

    @Override
    protected void draw(Canvas canvas, Object data) {

    }

    @Override
    protected void onDrawRect(Canvas canvas, Object data, Rect rect) {

    }

    @Override
    protected boolean preventClear() {
        return false;
    }
}

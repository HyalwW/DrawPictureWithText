package com.example.viewdemo.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.viewdemo.BaseSurfaceView;
import com.example.viewdemo.R;

/**
 * Created by Wang.Wenhui
 * Date: 2020/3/9
 */
public class ArrowView extends BaseSurfaceView {
    private Path mPath;
    private Bitmap arrow;
    private PathMeasure measure;
    private boolean flying;
    private Rect dst;

    public ArrowView(Context context) {
        super(context);
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onInit() {
        mPath = new Path();
        arrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
        measure = new PathMeasure();
        dst = new Rect();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void onReady() {
        callDraw("white");
    }

    @Override
    protected void onDataUpdate() {

    }

    @Override
    protected void onRefresh(Canvas canvas) {

    }

    @Override
    protected void draw(Canvas canvas, Object data) {
        canvas.drawColor(Color.WHITE);
        if (data instanceof Path) {
            Path path = (Path) data;
            canvas.drawPath(path, mPaint);
        } else if (data instanceof Float) {
            float length = (float) data;
            float[] pos = new float[2];
            float[] tan = new float[2];
            measure.getPosTan(length, pos, tan);
            Path path = new Path();
            measure.getSegment(length, measure.getLength(), path, true);
            canvas.drawPath(path, mPaint);
            dst.set((int) pos[0] - arrow.getWidth() / 2,
                    (int) pos[1] - arrow.getHeight() / 2,
                    (int) pos[0] + arrow.getWidth() / 2,
                    (int) pos[1] + arrow.getHeight() / 2);
            float degress = (float) (Math.atan2(tan[1], tan[0]) * 180 / Math.PI) + 90;
            canvas.rotate(degress, pos[0], pos[1]);
            canvas.drawBitmap(arrow, null, dst, mPaint);
            if (length < measure.getLength() && flying) {
                length += (float) getMeasuredHeight() / 70;
                callDrawDelay(length, 16);
            }
        } else if (data instanceof String) {
            canvas.drawColor(Color.WHITE);
        }
    }

    @Override
    protected void onDrawRect(Canvas canvas, Object data, Rect rect) {

    }

    @Override
    protected boolean preventClear() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                flying = false;
                mPath.reset();
                mPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(event.getX(), event.getY());
                callDraw(mPath);
                break;
            case MotionEvent.ACTION_UP:
                measure.setPath(mPath, false);
                flying = true;
                callDraw(0f);
                break;
        }
        return true;
    }

}

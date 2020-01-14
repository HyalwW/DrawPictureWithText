package com.example.viewdemo.videos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.example.viewdemo.BaseSurfaceView;
import com.example.viewdemo.BitmapUtil;

import java.util.Arrays;

/**
 * Created by Wang.Wenhui
 * Date: 2020/1/14
 */
public class VideoTextView extends BaseSurfaceView {
    private Bitmap bitmap, base;
    private String[] temps;
    private int colorGap;
    private int textSize;
    //一行多少字
    private int textInLine = 300;
    private Rect dst;
    private MediaDecoder decoder;
    private boolean isPlaying;
    private long currentPosition;

    public VideoTextView(Context context) {
        super(context);
    }

    public VideoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onInit() {
        String temp = "一二三六四品圆履";
        temps = temp.split("");
        temps = Arrays.copyOfRange(temps, 1, temps.length);
        colorGap = -Color.BLACK / temps.length;
        mPaint.setFilterBitmap(true);
        dst = new Rect();
        decoder = new MediaDecoder();
    }

    @Override
    protected void onReady() {

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
        if (data instanceof String[][]) {
            String[][] strings = (String[][]) data;
            mPaint.setTextSize(textSize);
            StringBuilder builder;
            mPaint.setColor(Color.BLACK);
            for (int i = 0; i < strings.length; i++) {
                String[] string = strings[i];
                builder = new StringBuilder();
                for (String s : string) {
                    builder.append(s);
                }
                float width = mPaint.measureText(builder.toString());
                canvas.drawText(builder.toString(), (float) getMeasuredWidth() / 2 - width / 2, textSize * (i + 1), mPaint);
            }
            int top = textSize * (strings.length + 2);
            dst.set(0, top, getMeasuredWidth(), (int) (top + base.getHeight() * ((float) getMeasuredWidth() / base.getWidth())));
            canvas.drawBitmap(base, null, dst, mPaint);
            draw();
        }
    }

    @Override
    protected boolean preventClear() {
        return false;
    }

    private MediaDecoder.OnGetBitmapListener listener = (bitmap, timeMs) -> {
        if (base != null && !base.isRecycled()) {
            base.recycle();
        }
        base = bitmap;
    };

    public void start() {
        if (!isPlaying) {
            isPlaying = true;
            currentPosition = 0;
            draw();
        }
    }

    public void draw() {
        if (currentPosition < decoder.getVedioFileLength()) {
            decoder.decodeFrame(currentPosition, listener);
            currentPosition += 1000;
        }
        if (textInLine < base.getWidth()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = BitmapUtil.scaleBitmap(base, (float) textInLine / base.getWidth());
        }
        String[][] strings = new String[bitmap.getHeight()][bitmap.getWidth()];
        textSize = getMeasuredWidth() / textInLine;
        for (int i = 0; i < strings.length; i++) {
            for (int j = 0; j < strings[i].length; j++) {
                int pixel = bitmap.getPixel(j, i);
                strings[i][j] = genText(pixel);
            }
        }
        callDraw(strings);
    }

    private String genText(int pixel) {
        int index = temps.length - (pixel - Color.BLACK) / colorGap - 1;
        return temps[index < 0 ? 0 : index];
    }

    public void setFile(String fileName) {
        decoder.decore(fileName);
    }

    public boolean isDecoreDone() {
        return decoder.isDone();
    }
}

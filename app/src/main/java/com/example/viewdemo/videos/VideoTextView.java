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
    private String[] temps;
    private int colorGap;
    private int textSize;
    //一行多少字
    private int textInLine = 200;
    private MediaDecoder decoder;
    private boolean isPlaying;
    private long currentPosition;
    private BitmapsNode bitmapsNode, bitmapRoot;
    private StringsNode stringsNode, stringRoot;
    private Rect positionRect, videoRect;
    private Runnable decordBitmapRunnable = () -> {
        currentPosition = 0;
        while (currentPosition < decoder.getVedioFileLength()) {
            BitmapsNode node = new BitmapsNode(decoder.decodeFrame(currentPosition));
            if (bitmapsNode == null) {
                bitmapsNode = node;
                bitmapRoot = node;
            } else {
                bitmapsNode.setNext(node);
                bitmapsNode = bitmapsNode.next();
            }
            currentPosition += 30;
//            Log.e("wwh", "VideoTextView --> : duration" + currentPosition);
            callDraw((float) currentPosition / decoder.getVedioFileLength() * 100, positionRect);
        }
    };
    private Runnable bitmap2StringsRunnable = () -> {
        int code = 0;
        while (true) {
            if (bitmapRoot != null) {
                if (bitmapRoot.hashCode() != code) {
//                    Log.e("wwh", "VideoTextView --> : code" + code);
                    code = bitmapRoot.hashCode();
                    Bitmap bitmap = bitmapRoot.getBitmap();
                    if (textInLine < bitmap.getWidth()) {
                        bitmap = BitmapUtil.scaleBitmap(bitmap, (float) textInLine / bitmap.getWidth());
                    }
                    String[][] strings = new String[bitmap.getHeight()][bitmap.getWidth()];
                    textSize = getMeasuredWidth() / textInLine;
                    int bottom = textSize * (strings.length + 1);
                    videoRect.set(0, 0, getMeasuredWidth(), bottom < getMeasuredHeight() * 0.9 ? bottom : (int) (getMeasuredHeight() * 0.9));
                    for (int i = 0; i < strings.length; i++) {
                        for (int j = 0; j < strings[i].length; j++) {
                            int pixel = bitmap.getPixel(j, i);
                            strings[i][j] = genText(pixel);
                        }
                    }
                    if (stringsNode == null) {
                        stringsNode = new StringsNode(strings);
                        stringRoot = stringsNode;
                    } else {
                        stringsNode.setNext(new StringsNode(strings));
                        stringsNode = stringsNode.next();
                    }
                } else {
                    if (bitmapRoot.hasNext()) {
                        bitmapRoot = bitmapRoot.next();
                    }
                }
            }
        }
    };
    private Runnable drawRunnable = () -> {
        int code = 0;
        while (true) {
            if (isPlaying) {
                if (stringRoot != null) {
                    if (stringRoot.hashCode() != code) {
                        code = stringRoot.hashCode();
                        callDrawDelay(stringRoot.getStrings(), videoRect, 16);
                    } else {
                        if (stringRoot.hasNext()) {
                            stringRoot = stringRoot.next();
                        } else {
                            isPlaying = false;
                        }
                    }
                }
            }
        }
    };

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
//        一二三六四品圆履
        String temp = "一十工干天王口凸田回品困圆淼";
        temps = temp.split("");
        temps = Arrays.copyOfRange(temps, 1, temps.length);
        colorGap = -Color.BLACK / temps.length;
        mPaint.setFilterBitmap(true);
        decoder = new MediaDecoder();
        positionRect = new Rect();
        videoRect = new Rect();
    }

    @Override
    protected void onReady() {
        positionRect.set(0, (int) (getMeasuredHeight() * 0.9), getMeasuredWidth(), getMeasuredHeight());
        callDraw("init");
        doInThread(drawRunnable);
    }

    @Override
    protected void onDataUpdate() {

    }

    @Override
    protected void onRefresh(Canvas canvas) {

    }

    @Override
    protected void draw(Canvas canvas, Object data) {
        if (data instanceof String) {
            canvas.drawColor(Color.BLACK);
        }
    }

    @Override
    protected void onDrawRect(Canvas canvas, Object data, Rect rect) {
        if (data instanceof String[][]) {
//            Log.e("wwh", "VideoTextView --> draw: ");
            canvas.drawColor(Color.WHITE);
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
                canvas.drawText(builder.toString(), (float) getMeasuredWidth() / 2 - width / 2, rect.top + textSize * (i + 1), mPaint);
            }
        } else if (data instanceof Float) {
            canvas.drawColor(Color.GRAY);
            mPaint.setColor(Color.BLUE);
            mPaint.setTextSize(60);
            float position = ((float) data);
            String text = position < 100 ? "视频加载进度:" + String.format("%.2f", position) + "%" : "加载完成";
            float width = mPaint.measureText(text);
            canvas.drawText(text, (float) getMeasuredWidth() / 2 - width / 2, ((rect.bottom - rect.top) >> 1) + rect.top, mPaint);
        }
    }

    @Override
    protected boolean preventClear() {
        return false;
    }

    public void start() {
        if (!isPlaying) {
            isPlaying = true;
        }
    }

    public void pause() {
        if (isPlaying) {
            isPlaying = false;
        }
    }

    private String genText(int pixel) {
        int index = temps.length - (pixel - Color.BLACK) / colorGap - 1;
        return temps[index < 0 ? 0 : index];
    }

    public void setFile(String fileName) {
        reset();
        decoder.decore(fileName);
        doInThread(decordBitmapRunnable);
        doInThread(bitmap2StringsRunnable);
    }

    private void reset() {
        bitmapsNode = null;
        bitmapRoot = null;
        stringsNode = null;
        stringRoot = null;
    }

    public boolean isDecoreDone() {
        return decoder.isDone();
    }
}

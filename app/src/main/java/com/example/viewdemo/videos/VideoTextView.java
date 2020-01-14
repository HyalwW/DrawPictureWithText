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
    private BitmapsNode bitmapsNode, bitmapRoot;
    private StringsNode stringsNode, stringRoot;
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
            currentPosition += 33;
//            Log.e("wwh", "VideoTextView --> : duration" + currentPosition);
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
            if (stringRoot != null) {
                if (stringRoot.hashCode() != code) {
                    code = stringRoot.hashCode();
                    callDrawDelay(stringRoot.getStrings(), 33);
                } else {
                    if (stringRoot.hasNext()) {
                        stringRoot = stringRoot.next();
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
//            Log.e("wwh", "VideoTextView --> draw: " );
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
        }
    }

    @Override
    protected boolean preventClear() {
        return false;
    }

    public void start() {
        if (!isPlaying) {
            isPlaying = true;
            doInThread(drawRunnable);
        }
    }

    private String genText(int pixel) {
        int index = temps.length - (pixel - Color.BLACK) / colorGap - 1;
        return temps[index < 0 ? 0 : index];
    }

    public void setFile(String fileName) {
        decoder.decore(fileName);
        doInThread(decordBitmapRunnable);
        doInThread(bitmap2StringsRunnable);
    }

    public boolean isDecoreDone() {
        return decoder.isDone();
    }
}

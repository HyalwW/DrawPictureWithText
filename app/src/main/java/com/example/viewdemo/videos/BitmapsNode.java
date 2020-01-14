package com.example.viewdemo.videos;

import android.graphics.Bitmap;

/**
 * Created by Wang.Wenhui
 * Date: 2020/1/14
 */
public class BitmapsNode {
    private Bitmap bitmap;
    private BitmapsNode next;

    public BitmapsNode(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setNext(BitmapsNode next) {
        this.next = next;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public boolean hasNext() {
        return next != null;
    }

    public BitmapsNode next() {
        return next;
    }
}

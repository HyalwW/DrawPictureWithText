package com.example.viewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Wang.Wenhui
 * Date: 2020/1/10
 */
public abstract class BaseSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Handler.Callback {
    private HandlerThread mHandlerThread;
    private Handler drawHandler;
    private MsgBuilder builder;
    protected SurfaceHolder holder;
    protected long UPDATE_RATE = 16;
    protected Paint mPaint;
    private boolean running = true;
    private LifecycleListener listener;

    public BaseSurfaceView(Context context) {
        this(context, null);
    }

    public BaseSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setFilterBitmap(true);

        holder = getHolder();
        holder.addCallback(this);

        builder = new MsgBuilder();
        onInit();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 207:
                if (running) {
                    long before = System.currentTimeMillis();
                    drawEverything(null);
                    long waste = System.currentTimeMillis() - before;
                    builder.newMsg().what(207).sendDelay(UPDATE_RATE - waste);
                }
                break;
            case 208:
                drawEverything(msg.obj);
                break;
            case 209:
                ((Runnable) msg.obj).run();
                break;
        }
        return true;
    }

    private synchronized void drawEverything(Object data) {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            if (!preventClear()) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }
            if (running) {
                onRefresh(canvas);
            }
            if (data != null) {
                draw(canvas, data);
            }
            if (holder.getSurface().isValid()) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHandlerThread = new HandlerThread("drawThread");
        mHandlerThread.start();
        drawHandler = new Handler(mHandlerThread.getLooper(), this);
        onReady();
        if (listener != null) {
            listener.onCreate();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (listener != null) {
            listener.onChanged();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
        if (listener != null) {
            listener.onDestroy();
        }
    }

    public void startAnim() {
        running = true;
        builder.newMsg().what(207).send();
    }

    public void stopAnim() {
        running = false;
    }

    public void callDraw(Object data) {
        builder.newMsg().obj(data).what(208).send();
    }

    public void doInThread(Runnable runnable) {
        builder.newMsg().what(209).obj(runnable).send();
    }

    private final class MsgBuilder {

        private Message message;

        MsgBuilder newMsg() {
            message = Message.obtain();
            return this;
        }

        MsgBuilder what(int what) {
            checkMessageNonNull();
            message.what = what;
            return this;
        }

        MsgBuilder obj(Object o) {
            checkMessageNonNull();
            message.obj = o;
            return this;
        }

        void send() {
            sendDelay(0);
        }

        private void sendDelay(long millis) {
            checkMessageNonNull();
            onDataUpdate();
            drawHandler.sendMessageAtTime(message, millis < 0 ? 10 : millis);
        }

        private void checkMessageNonNull() {
            if (message == null) {
                throw new IllegalStateException("U should call newMsg() before use");
            }
        }
    }

    public void setListener(LifecycleListener listener) {
        this.listener = listener;
    }

    public interface LifecycleListener {
        void onCreate();

        void onChanged();

        void onDestroy();
    }

    /**
     * 用于初始化画笔，基础数据等
     */
    protected abstract void onInit();

    /**
     * 此时handler可用，异步加载数据调用doInThread
     */
    protected abstract void onReady();

    /**
     * 数据更新，刷新前调用
     */
    protected abstract void onDataUpdate();

    /**
     * 绘制内容，用于默认开启的绘图刷新,若不需要则调用stopAnim停止会刷新
     *
     * @param canvas 画布
     */
    protected abstract void onRefresh(Canvas canvas);

    /**
     * 调用callDraw后触发，根据特定data绘制
     *
     * @param canvas 画布
     * @param data   数据
     */
    protected abstract void draw(Canvas canvas, Object data);

    /**
     * 是否阻止刷新时清空画布
     *
     * @return
     */
    protected abstract boolean preventClear();
}

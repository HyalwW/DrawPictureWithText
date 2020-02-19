
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.byd.carlauncher.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Wang.Wenhui
 * Date: 2020/2/18
 */
public class SAEView extends BaseSurfaceView implements Animator.AnimatorListener {
    private ValueAnimator shakeAnim, dropAnim;
    //将图片分割出的颜色数量，将图片切割成多少行列，颜色跨度误差倍数，重复个数（总圆个数 = colorSize * reuseCount）
    private final int colorSize = 20, bitmapGap = 16, colorGap = 30, reuseCount = 3;
    private int[] colors;
    private SparseArray<Integer> queue;
    private Bitmap bitmap;
    private boolean analyzeDone, inAnalyze;
    private RectF dst;
    private List<Circle> list;
    private Random random;

    public SAEView(Context context) {
        super(context);
    }

    public SAEView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SAEView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onInit() {
        colors = new int[colorSize];
        queue = new SparseArray<>();
        list = new ArrayList<>();
        random = new Random();
        dst = new RectF();
        shakeAnim = new ValueAnimator();
        shakeAnim.setFloatValues(0, 1);
        shakeAnim.setRepeatCount(10);
        shakeAnim.setRepeatMode(ValueAnimator.REVERSE);
        shakeAnim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            callDraw(value);
        });
        shakeAnim.setDuration(40);
        shakeAnim.addListener(this);

        dropAnim = new ValueAnimator();
        dropAnim.setFloatValues(0, 1);
        dropAnim.setDuration(1000);
        dropAnim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            float time = 2 * value;
            for (Circle circle : list) {
                circle.move(time, 300);
            }
            callDraw("drawExplode");
        });
    }

    @Override
    protected void onReady() {
        setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music_item2));
    }

    private void analyze() {
        doInThread(() -> {
            inAnalyze = true;
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int raiseW = width / bitmapGap, raiseH = height / bitmapGap;
            int colorG = (Color.TRANSPARENT - Color.BLACK) / colorGap;
            for (int i = 0; i < width; i += raiseW) {
                for (int j = 0; j < height; j += raiseH) {
                    boolean use = false;
                    int color = bitmap.getPixel(i, j);
                    for (int index = 0; index < queue.size(); index++) {
                        if (color > queue.keyAt(index) - colorG && color < queue.keyAt(index) + colorG) {
                            queue.setValueAt(index, queue.valueAt(index) + 1);
                            use = true;
                            break;
                        }
                    }
                    if (!use) {
                        queue.put(color, 1);
                    }
                }
            }
            int index = 0;
            while (index < colorSize) {
                int color = 0, count = 0;
                for (int i = 0; i < queue.size(); i++) {
                    if (queue.valueAt(i) > count) {
                        count = queue.valueAt(i);
                        color = queue.keyAt(i);
                    }
                }
                queue.remove(color);
                colors[index++] = color;
            }
            analyzeDone = true;
            inAnalyze = false;
            for (int color : colors) {
                if (color == 0) {
                    color = Color.WHITE;
                }
                for (int i = 0; i < reuseCount; i++) {
                    list.add(new Circle(color, getMeasuredWidth() >> 1, getMeasuredHeight() >> 1));
                }
            }
            callDraw(bitmap);
        });
    }

    public void setBitmap(Bitmap bitmap) {
        if (inAnalyze) {
            return;
        }
        this.bitmap = bitmap;
        float left = (getMeasuredWidth() >> 1) - (bitmap.getWidth() >> 1);
        float top = (getMeasuredHeight() >> 1) - (bitmap.getHeight() >> 1);
        dst.set(left, top, left + bitmap.getWidth(), top + bitmap.getHeight());
        analyzeDone = false;
        analyze();
    }

    public void startShakeAndExplode() {
        if (!analyzeDone) {
            return;
        }
        shakeAnim.cancel();
        dropAnim.cancel();
        shakeAnim.start();
    }

    @Override
    protected void onDataUpdate() {

    }

    @Override
    protected void onRefresh(Canvas canvas) {

    }

    @Override
    protected void draw(Canvas canvas, Object data) {
        if (data instanceof Bitmap) {
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap((Bitmap) data, null, dst, mPaint);
        } else if (data instanceof Float) {
            canvas.rotate(-6 + (float) data * 12, getMeasuredWidth() >> 1, getMeasuredHeight() >> 1);
            canvas.drawBitmap(bitmap, null, dst, mPaint);
        } else if (data instanceof String) {
            mPaint.setStyle(Paint.Style.FILL);
            if (data.equals("drawExplode")) {
                for (Circle circle : list) {
                    mPaint.setColor(circle.color);
                    canvas.drawCircle(circle.nx, circle.ny, circle.radius, mPaint);
                }
            }
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        for (Circle circle : list) {
            circle.reset();
        }
        dropAnim.start();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            startShakeAndExplode();
        }
        return true;
    }

    private float randomRadius() {
        return random.nextFloat() * 5 + 8;
    }

    private float randomXSpeed() {
        return -160 + random.nextFloat() * 320;
    }

    private float randomYSpeed() {
        return 80 + random.nextFloat() * 100;
    }

    private class Circle {
        int color;
        float xSpeed, ySpeed;
        float x, y;
        float radius;
        float nx, ny;

        Circle(int color, float x, float y) {
            this.color = color;
            this.x = x;
            this.y = y;
            reset();
        }

        Circle(int color, float radius, float xSpeed, float ySpeed, float x, float y) {
            this.color = color;
            this.radius = radius;
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            this.x = x;
            this.y = y;
        }

        /**
         * @param time 时间：秒
         * @param g    重力加速度
         */
        void move(float time, float g) {
            nx = x + xSpeed * time;
            ny = getMeasuredHeight() - (y + ySpeed * time - g * time * time / 2);
        }

        void reset() {
            radius = randomRadius();
            xSpeed = randomXSpeed();
            ySpeed = randomYSpeed();
        }
    }
}

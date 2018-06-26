package com.hyk.scratchcards.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hyk.scratchcards.R;

public class ScratchCard extends View {
    private static final String TAG = "ScratchCard";
    private Paint mOuterPaint;
    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;

    private int mLastX, mLastY;
    private Bitmap mOuterBitmap;


    private Paint mBackPaint;
    /*
     记录刮奖信息文本宽高
     */
    private Rect mTextBound;
    private String mText;
    private int mTextSize;
    private int mTextColor;

    private volatile boolean mComplete = false;

    private OnScratchCardCompleteListener mListener;

    public interface OnScratchCardCompleteListener {
        void complete();
    }

    public void setCompleteListener(OnScratchCardCompleteListener listener) {
        this.mListener = listener;
    }

    public ScratchCard(Context context) {
        this(context, null);
    }

    public ScratchCard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScratchCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.ScratchCard, defStyleAttr, 0);

        //Hour - Needle
        mTextColor = typedArray.getColor(R.styleable.ScratchCard_textColor,
                Color.parseColor("#333333"));
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.ScratchCard_textSize,
                30);
        mText = typedArray.getString(R.styleable.ScratchCard_text);
        typedArray.recycle();
    }

    /**
     * 进行一些初始化操作
     */
    private void init() {
        mOuterPaint = new Paint();
//        mCanvas = new Canvas();
        mPath = new Path();
        mOuterBitmap = BitmapFactory.decodeResource(getResources()
                , R.mipmap.fg_guaguaka);


        mText = "谢谢惠顾";
        mTextBound = new Rect();
        mBackPaint = new Paint();

        mTextSize = 30;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        setupOuterPaint();
        setupBackPaint();

//        mCanvas.drawColor(Color.parseColor("#c0c0c0"));
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mOuterPaint);
        mCanvas.drawBitmap(mOuterBitmap, null
                , new Rect(0, 0, width, height), null);
    }

    /**
     * 获奖信息 画笔
     */
    private void setupBackPaint() {
        mBackPaint.setColor(mTextColor);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setTextSize(mTextSize);
        //获取当前画笔绘制文本宽高
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    private void setupOuterPaint() {
        mOuterPaint.setColor(Color.parseColor("#c0c0c0"));
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setDither(true);
        mOuterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOuterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOuterPaint.setStyle(Paint.Style.FILL);
        mOuterPaint.setStrokeWidth(22);
    }

    public void setText(String text) {
        this.mText = text;
        //获取文本bound
        mBackPaint.getTextBounds(mText,0,mText.length(),mTextBound);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);

                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }

                mLastY = y;
                mLastX = x;

                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
        }

        invalidate();
        return true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();

            float wipeArea = 0;
            float totalArea = w * h;
            Bitmap bitmap = mBitmap;
            int[] mPixels = new int[w * h];

            //获取bitmap上所有的像素信息
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < mPixels.length; i++) {
                    if (mPixels[i] == 0) {
                        wipeArea++;
                    }
            }

            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.d(TAG, "percent: " + percent);

                if (percent > 60) {
                    mComplete = true;
                    postInvalidate();
                }
            }
        }
    };


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
                getHeight() / 2 + mTextBound.height() / 2, mBackPaint);
        if (!mComplete) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        } else {
            if (mListener != null) {
                mListener.complete();
            }
        }
    }

    private void drawPath() {
        mOuterPaint.setStyle(Paint.Style.STROKE);
        mOuterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOuterPaint);
    }
}

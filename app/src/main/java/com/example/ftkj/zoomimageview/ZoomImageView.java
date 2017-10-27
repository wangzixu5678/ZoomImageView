package com.example.ftkj.zoomimageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.icu.math.BigDecimal;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;


/**
 * Created by FTKJ on 2017/10/24.
 */

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

    private final int mTouchSlop;
    private Matrix mScaleMatrix;
    private boolean mOnce;
    private float mInitScale;
    private float mMaxScale;
    private float mMidScale;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mLastX;
    private float mLastY;
    private int mLastPointCount;
    private boolean isCanDrag;
    private boolean isCheckTopAndBottom;
    private boolean isCheckLeftAndRight;
    private GestureDetector mGestureDetector;
    private boolean isAutoScale;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /**
         * init()
         */
        mScaleMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                /**
                 * 如果正在处理 则点击无效
                 */
                if (isAutoScale) {
                    return true;
                }
                float scale = getScale();
                float x = e.getX();
                float y = e.getY();

//                if (scale>=mMidScale){
//                    /**
//                     * 缩小
//                     */
//                    mScaleMatrix.postScale(mInitScale*1.0f/scale,mInitScale*1.0f/scale,x,y);
//                }else if (scale<mMidScale){
//                    /**
//                     * 放大
//                     */
//                    mScaleMatrix.postScale(mMidScale*1.0f/scale,mMidScale*1.0f/scale,x,y);
//                }
//                checkBorderAndCenterWhenScale();
//                setImageMatrix(mScaleMatrix);
                if (scale >= mMidScale) {
                    postDelayed(new AutoScaleRunnable(x, y, mInitScale), 16);
                    isAutoScale = true;
                } else if (scale < mMidScale) {
                    postDelayed(new AutoScaleRunnable(x, y, mMidScale), 16);
                    isAutoScale = true;
                }
                return true;
            }
        });
        setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }


    /**
     * 处理双击放大 以至于不要显得太突兀 缓慢递增 or  递减改变。
     */
    public class AutoScaleRunnable implements Runnable {
        private float x;
        private float y;
        private float mTargetScale;
        private float mTempScale;
        private final float BIGGER = 1.07f;
        private final float SMALL = 0.93f;

        private AutoScaleRunnable(float x, float y, float targetScale) {
            this.x = x;
            this.y = y;
            mTargetScale = targetScale;
            if (getScale() > mTargetScale) {
                mTempScale = SMALL;
            }
            if (getScale() < mTargetScale) {
                mTempScale = BIGGER;
            }
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(mTempScale, mTempScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
            if ((mTempScale > 1.0f && getScale() < mTargetScale) || (mTempScale < 1.0f && getScale() > mTargetScale)) {
                postDelayed(this, 16);
            } else {
                float scale = mTargetScale / getScale();
                mScaleMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }

            /**
             * 自己的方法
             */
//            float currentScale = getScale();
//            if (mTargetScale == mMidScale) {
//                if (currentScale < mMidScale) {
//                    mTempScale = currentScale * BIGGER;
//                    mScaleMatrix.postScale(mTempScale * 1.0f / currentScale, mTempScale * 1.0f / currentScale, x, y);
//                    checkBorderAndCenterWhenScale();
//                    setImageMatrix(mScaleMatrix);
//                    postDelayed(this, 16);
//                } else {
//                    float scale = mTargetScale / currentScale;
//                    mScaleMatrix.postScale(scale, scale, x, y);
//                    checkBorderAndCenterWhenScale();
//                    setImageMatrix(mScaleMatrix);
//                    isAutoScale = false;
//                }
//            } else if (mTargetScale == mInitScale) {
//                if (currentScale > mInitScale) {
//                    mTempScale = currentScale * SMALL;
//                    mScaleMatrix.postScale(mTempScale * 1.0f / currentScale, mTempScale * 1.0f / currentScale, x, y);
//                    checkBorderAndCenterWhenScale();
//                    setImageMatrix(mScaleMatrix);
//                    postDelayed(this, 16);
//                } else {
//                    float scale = mTargetScale / currentScale;
//                    mScaleMatrix.postScale(scale, scale, x, y);
//                    checkBorderAndCenterWhenScale();
//                    setImageMatrix(mScaleMatrix);
//                    isAutoScale = false;
//                }
//            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void onGlobalLayout() {
        if (!mOnce) {
            int width = getWidth();
            int height = getHeight();
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();

            float scale = 1.0f;
            /**
             * 图片宽大于屏幕，图片高小于屏幕，应该缩小
             */
            if (dw > width && dh < height) {
                scale = width * 1.0f / dw;
            }
            /**
             * 图片高大于屏幕，图片宽小于屏幕，应该缩小
             */
            if (dw < width && dh > height) {
                scale = height * 1.0f / dh;
            }
            /**
             * 如果图片宽高都大于屏幕,或宽高都小于屏幕。
             */
            if ((dw > width && dh > height) || dw < width && dh < height) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            mInitScale = scale;
            mMaxScale = mInitScale * 4;
            mMidScale = mInitScale * 2;
            int dx = getWidth() / 2 - dw / 2;
            int dy = getHeight() / 2 - dh / 2;

            mScaleMatrix.postTranslate(dx, dy);
            mScaleMatrix.postScale(mInitScale, mInitScale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mScaleMatrix);
            mOnce = true;
        }
    }

    /**
     * 获取图片缩放比例
     *
     * @return
     */
    public float getScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();
        if (getDrawable() == null) {
            return true;
        }
        /**
         * 可放大条件 是放大倍数小于最大 且手势为放大手势。
         * 可缩小条件 是放大倍数大于最小 且手势为缩小手势。
         */
        if (scale < mMaxScale && scaleFactor > 1.0f || scale > mInitScale && scaleFactor < 1.0f) {
            if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale * 1.0f / scale;
            }
            if (scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale * 1.0f / scale;
            }

            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            /**
             * 调整scale之后位置移动问题
             */
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }

        return false;
    }

    private void checkBorderAndCenterWhenScale() {
        RectF rectF = getMatrixRectF();

        float delteX = 0;
        float delteY = 0;

        int width = getWidth();
        int height = getHeight();
        /**
         * 图片大于屏幕宽度的情况 进行不断地位置调整 确保图片在中间。
         * 切记带上等于号
         */
        if (rectF.width() >= width) {
            if (rectF.left > 0) {
                delteX = -rectF.left;
            }
            if (rectF.right < width) {
                delteX = width - rectF.right;
            }
        }
        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                delteY = -rectF.top;
            }
            if (rectF.bottom < height) {
                delteY = height - rectF.bottom;
            }
        }
        /**
         * 图片小于屏幕宽度的情况 进行不断地位置调整 确保图片在中间。
         */
        if (rectF.width() < width) {
            delteX = width * 1.0f / 2f - rectF.right + rectF.width() * 1.0f / 2f;
        }
        if (rectF.height() < height) {
            delteY = height * 1.0f / 2f - rectF.bottom + rectF.height() * 1.0f / 2f;
        }
        mScaleMatrix.postTranslate(delteX, delteY);
    }


    /**
     * 获取图片放大缩小以后的宽和高，以及l,r,t,b
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            /**
             * 参数3 图片实际宽
             * 参数4 图片实际高
             */
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        /**
         * 一定要retrun true;
         */
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        mScaleGestureDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        float x = 0;
        float y = 0;
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX();
            y += event.getY();
        }
        x /= pointerCount;
        y /= pointerCount;

        if (mLastPointCount != pointerCount) {
            mLastPointCount = pointerCount;
            mLastX = x;
            mLastY = y;
            isCanDrag = false;
        }
        RectF rect = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rect.width() > getWidth()+0.01 || rect.height() > getHeight()+0.01) {
                    if (getParent() instanceof ViewPager) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rect.width() > getWidth()+0.01 || rect.height() >getHeight()+0.01) {
                    if (getParent() instanceof ViewPager) {
                        /**
                         * 不让Viewpager拦截
                         */
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                float dx = x - mLastX;
                float dy = y - mLastY;
                if (!isCanDrag) {
                    isCanDrag = isMoveAction(dx, dy);
                }
                if (isCanDrag) {
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        if (rect.width() < getWidth()) {
                            isCheckLeftAndRight = false;
                            dx = 0;
                        }
                        if (rect.height() < getHeight()) {
                            isCheckTopAndBottom = false;
                            dy = 0;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }

                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                mLastPointCount = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                mLastPointCount = 0;
                break;
        }
        return true;
    }

    private void checkBorderWhenTranslate() {
        /**
         * 注释掉的代码是错误的
         * 因为如果图片宽高都大于屏幕，那么无法满足。
         */
//        RectF rectF = getMatrixRectF();
//        float deltaX = 0;
//        float deltaY = 0;
//        int width = getWidth();
//        int height = getHeight();
//        if (rectF.top < 0) {
//            deltaY = -rectF.top;
//        }
//        if (rectF.bottom > height) {
//            deltaY = height - rectF.bottom;
//        }
//        if (rectF.left<0){
//            deltaX = -rectF.left;
//        }
//        if (rectF.right>width){
//            deltaX = width - rectF.right;
//        }
//        mScaleMatrix.postTranslate(deltaX,deltaY);

        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;
        int width = getWidth();
        int height = getHeight();
        if (isCheckTopAndBottom && rect.top > 0) {
            deltaY = -rect.top;
        }
        if (isCheckTopAndBottom && rect.bottom < height) {
            deltaY = height - rect.bottom;
        }
        if (isCheckLeftAndRight && rect.left > 0) {
            deltaX = -rect.left;
        }
        if (isCheckLeftAndRight && rect.right < width) {
            deltaX = width - rect.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);

    }

    private boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx * dx + dy * dy) >= mTouchSlop;
    }
}

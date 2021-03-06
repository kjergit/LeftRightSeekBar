package com.example.simon.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * 可以设置起点、最小值，最大值，滑动间距的左右拖动条
 */
public class MiddleSeekBar extends View {
    private Scroller mScroller;
    private GestureDetector mGestureDetector;
    private Paint mThumbPaint;
    private Paint mThumbInPaint;
    private Paint mLinePaint1;
    private Paint mLinePaint2;
    private Paint mHighLightLinePaint;
    private Paint mNailPaint;
    private Paint mBackGroundPaint;
    private float mThumbRadius = 24.0f;
    private float mNailRadius = 8.0f;
    private float mNailStrokeWidth = 1.5f;
    private float mLineWidth = 1.5f;
    private float mDefaultAreaRadius
            = ((mThumbRadius - mNailRadius - mNailStrokeWidth) + mThumbRadius) / 2;

    private float mSeekLength;
    private float mSeekLineStart;
    private float mSeekLineEnd;
    private float mNailOffset;
    private float mThumbOffset;
    private int mMaxValue = 100;
    private int mCurrentValue = 50;
    private int mDefaultValue = 50;
    private int mStartValue;
    private OnSeekChangeListener mListener;
    private float mStep;
    private SeekBarGestureListener mGestureListener;
    private String mLineColor;

    private RectF mBackRectF;

    public MiddleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mThumbRadius = context.getResources().getDimension(R.dimen.aircraft_seekbar_thumb_radius);
        mNailRadius = context.getResources().getDimension(R.dimen.aircraft_seekbar_nail_radius);
        mNailStrokeWidth = context.getResources().getDimension(R.dimen.aircraft_seekbar_nail_stroke_width);
        mLineWidth = context.getResources().getDimension(R.dimen.aircraft_seekbar_line_width);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mGestureListener = new SeekBarGestureListener();
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);

        mNailPaint = new Paint();
        mNailPaint.setAntiAlias(true);
        mNailPaint.setColor(Color.parseColor("#ffffff"));
        mNailPaint.setStrokeWidth(mNailStrokeWidth);
        mNailPaint.setStyle(Paint.Style.STROKE);

        mThumbPaint = new Paint();
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setStrokeWidth(5);
        mThumbPaint.setColor(Color.parseColor("#ffffff"));
        mThumbPaint.setStyle(Paint.Style.STROKE);

        //绘制 内环圆
        mThumbInPaint = new Paint();
        mThumbInPaint.setAntiAlias(true);
        mThumbInPaint.setColor(Color.parseColor("#3A75F2"));
        mThumbInPaint.setStyle(Paint.Style.FILL);


        mLinePaint1 = new Paint();
        mLinePaint1.setAntiAlias(true);
        mLinePaint1.setColor(Color.parseColor("#000000"));
        mLinePaint1.setAlpha(0);

        mLinePaint2 = new Paint();
        mLinePaint2.setAntiAlias(true);
        mLinePaint2.setColor(Color.parseColor("#000000"));
        mLinePaint2.setAlpha(0);

        mHighLightLinePaint = new Paint();
        mHighLightLinePaint.setAntiAlias(true);
        mHighLightLinePaint.setColor(Color.parseColor("#ffffff"));
        mHighLightLinePaint.setAlpha(200);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int hmode = MeasureSpec.getMode(heightMeasureSpec);
        if (hmode == MeasureSpec.AT_MOST) {
            int hsize = Math.round(mThumbRadius * 2);
            hsize += getPaddingTop() + getPaddingBottom();
            int wsize = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(wsize, hsize);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mSeekLength == 0) {
            final int width = getWidth();
            mSeekLength = width - getPaddingLeft() - getPaddingRight() - mThumbRadius * 2;
            mSeekLineStart = getPaddingLeft() + mThumbRadius;
            mSeekLineEnd = width - getPaddingRight() - mThumbRadius;

            mNailOffset = mSeekLength * mDefaultValue / mMaxValue;
            if (mDefaultValue == 0
                    || mDefaultValue == mMaxValue) {
                mThumbOffset = mSeekLength * mCurrentValue / mMaxValue;
            } else {
                float defaultAreaLength = mDefaultAreaRadius * 2;
                if (mCurrentValue < mDefaultValue) {
                    mThumbOffset = (mSeekLength - defaultAreaLength) * mCurrentValue / mMaxValue;
                } else if (mCurrentValue > mDefaultValue) {
                    mThumbOffset = (mSeekLength - defaultAreaLength) * mCurrentValue / mMaxValue + mDefaultAreaRadius * 2;
                } else {
                    mThumbOffset = mNailOffset;
                }
            }
        }

        final float top = getMeasuredHeight() / 2 - mLineWidth / 2;
        final float bottom = top + mLineWidth;
        final float right1 = mSeekLineStart + mNailOffset + mNailStrokeWidth / 2 - mNailRadius;

        if (right1 > mSeekLineStart) {
            canvas.drawRect(mSeekLineStart, top, right1, bottom, mLinePaint1);
        }
        final float left2 = right1 + mNailRadius * 2;
        if (mSeekLineEnd > left2) {
            canvas.drawRect(left2, top, mSeekLineEnd, bottom, mLinePaint2);
        }

        //background
        mBackRectF = new RectF();
        mBackGroundPaint = new Paint();
        mBackGroundPaint.setColor(Color.BLUE);
        mBackGroundPaint.setAntiAlias(true);
        mBackGroundPaint.setStrokeWidth(3);
        mBackGroundPaint.setStyle(Paint.Style.STROKE);
        mBackRectF.top = top - 1.5f;
        mBackRectF.left = mSeekLineStart;
        mBackRectF.right = mSeekLineEnd;
        mBackRectF.bottom = bottom + 1.5f;
        canvas.drawRoundRect(mBackRectF, 15, 15, mBackGroundPaint);


        //draw thumb
        final float nailX = mSeekLineStart + mNailOffset;
        final float nailY = getMeasuredHeight() / 2;
        canvas.drawCircle(nailX, nailY, mNailRadius, mNailPaint);

        float thumbX = mSeekLineStart + mThumbOffset;
        final float thumbY = getMeasuredHeight() / 2;

        float highLightLeft = thumbX + mThumbRadius;
        float highLightRight = nailX - mNailRadius;
        if (thumbX > nailX) {
            highLightLeft = nailX + mNailRadius;
            highLightRight = thumbX - mThumbRadius;
        }

        canvas.drawRect(highLightLeft, top, highLightRight, bottom, mHighLightLinePaint);
        canvas.drawCircle(thumbX, thumbY, mThumbRadius, mThumbPaint);
        //add circle
        canvas.drawCircle(thumbX, thumbY, 22, mThumbInPaint);


        if (mScroller.computeScrollOffset()) {
            mThumbOffset = mScroller.getCurrY();
            invalidate();
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        if (MotionEvent.ACTION_UP == event.getAction()) {
            mGestureListener.onUp(event);
            if (null != mListener) {
                mListener.onSeekStopped((mCurrentValue + mStartValue) * mStep, mStep, this);
            }
            return true;
        }

        return false;
    }

    private float getThumbOffset(float pos) {
        if (pos < 0) {
            pos = 0;
        } else if (pos > mSeekLength) {
            pos = mSeekLength;
        }
        return pos;
    }

    public void setLineColor(String color) {
        mHighLightLinePaint.setColor(Color.parseColor(color));
        mNailPaint.setColor(Color.parseColor(color));
    }

    public void setThumbSize(float size) {
        mThumbRadius = size;
    }

    public void setSeekLength(int startValue, int endValue, int circleValue, float step) {

        mDefaultValue = Math.round((float) (circleValue - startValue) / step);
        mMaxValue = Math.round((float) (endValue - startValue) / step);
        mStartValue = Math.round((float) startValue / step);
        mStep = step;
    }

    public float getValue() {
        return (mCurrentValue + mStartValue) * mStep;
    }

    /**
     *
     * @param value
     * @param needChange  true is exce onSeekChanged();
     */
    public void setValue(float value,boolean needChange) {

        int newValue = Math.round(value / mStep) - mStartValue;
        if (newValue == mCurrentValue) {
            return;
        }
        mCurrentValue = newValue;
        if (null != mListener && needChange) {
            mListener.onSeekChanged(value * mStep, mStep, this);
        }

        updateThumbOffset();
        invalidate();
    }

    public void setOnSeekChangeListener(OnSeekChangeListener listener) {
        mListener = listener;
    }

    private void setValueInternal(int value) {
        if (mCurrentValue == value) {
            return;
        }
        mCurrentValue = value;
        if (null != mListener) {
            mListener.onSeekChanged((value + mStartValue) * mStep, mStep, this);
        }
    }

    private void updateThumbOffset() {

        if (mDefaultValue == 0
                || mDefaultValue == mMaxValue) {
            if (mCurrentValue == 0) {
                mThumbOffset = 0;
            } else if (mCurrentValue == mMaxValue) {
                mThumbOffset = mSeekLineEnd - mSeekLineStart;
            } else if (mCurrentValue == mDefaultValue) {
                mThumbOffset = mNailOffset;
            } else {
                mThumbOffset = mCurrentValue * mSeekLength / mMaxValue;
            }
        } else {
            float defaultAreaLength = mDefaultAreaRadius * 2;
            if (mCurrentValue == 0) {
                mThumbOffset = 0;
            } else if (mCurrentValue == mMaxValue) {
                mThumbOffset = mSeekLineEnd - mSeekLineStart;
            } else if (mCurrentValue < mDefaultValue) {
                mThumbOffset = (mSeekLength - defaultAreaLength) * mCurrentValue / mMaxValue;
            } else if (mCurrentValue > mDefaultValue) {
                mThumbOffset = (mSeekLength - defaultAreaLength) * mCurrentValue / mMaxValue + defaultAreaLength;
            } else {
                mThumbOffset = mNailOffset;
            }
        }
    }

    public void reset() {
        mSeekLength = 0;
        mSeekLineStart = 0;
        mSeekLineEnd = 0;
        mNailOffset = 0;
        mThumbOffset = 0;
        mMaxValue = 0;
        mCurrentValue = Integer.MAX_VALUE;
        mDefaultValue = 0;
        mStartValue = 0;
        mStep = 0;
        mScroller.abortAnimation();
    }

    public interface OnSeekChangeListener {
        void onSeekChanged(float currentValue, float step, MiddleSeekBar seekBar);

        void onSeekStopped(float currentValue, float step, MiddleSeekBar seekBar);
    }

    private class SeekBarGestureListener extends GestureDetector.SimpleOnGestureListener {

        public boolean onUp(MotionEvent e) {
            float initThumbOffset = mThumbOffset;
            updateThumbOffset();
            mScroller.startScroll(0, Math.round(initThumbOffset), 0, Math.round(mThumbOffset - initThumbOffset), 400);
            mThumbOffset = initThumbOffset;
            invalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            mThumbOffset -= distanceX;
            if (mThumbOffset < mSeekLineStart - mThumbRadius) {
                mThumbOffset = mSeekLineStart - mThumbRadius;
            }

            if (mThumbOffset > mSeekLineEnd - mThumbRadius) {
                mThumbOffset = mSeekLineEnd - mThumbRadius;
            }
            float newValue;
            if (mDefaultValue == 0
                    || mDefaultValue == mMaxValue) {
                newValue = mThumbOffset * mMaxValue / mSeekLength;
            } else {
                float defaultAreaLength = mDefaultAreaRadius * 2;
                if (mThumbOffset < mNailOffset - mDefaultAreaRadius) {
                    newValue = mThumbOffset * (mMaxValue - 2)
                            / (mSeekLength - defaultAreaLength);

                } else if (mThumbOffset > mNailOffset + mDefaultAreaRadius) {
                    newValue = mDefaultValue + (mThumbOffset - mNailOffset - mDefaultAreaRadius)
                            * (mMaxValue - 2) / (mSeekLength - defaultAreaLength) + 1;
                } else {
                    newValue = mDefaultValue;
                }
            }

            if (newValue < 0) {
                newValue = 0;
            }

            if (newValue > mMaxValue) {
                newValue = mMaxValue;
            }

            setValueInternal(Math.round(newValue));
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            int newValue = mCurrentValue - 1;
            if (e.getX() > mThumbOffset) {
                newValue = mCurrentValue + 1;
            }

            if (newValue < 0) {
                newValue = 0;
            }

            if (newValue > mMaxValue) {
                newValue = mMaxValue;
            }

            setValueInternal(newValue);

            float initThumbOffset = mThumbOffset;
            updateThumbOffset();
            mScroller.startScroll(0, Math.round(initThumbOffset), 0, Math.round(mThumbOffset - initThumbOffset), 400);
            mThumbOffset = initThumbOffset;
            invalidate();

            if (null != mListener) {
                mListener.onSeekStopped((mCurrentValue + mStartValue) * mStep, mStep, MiddleSeekBar.this);
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            final int offsetLength = (int) getThumbOffset(e.getX() - mSeekLineStart);
            mScroller.startScroll(0, (int) mThumbOffset, 0, (int) (offsetLength - mThumbOffset), 400);
            setValueInternal((int) (offsetLength * mMaxValue / mSeekLength));
            updateThumbOffset();
            invalidate();
        }
    }
}

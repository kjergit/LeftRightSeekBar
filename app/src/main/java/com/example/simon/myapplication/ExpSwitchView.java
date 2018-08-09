package com.example.simon.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Exp 曲线view
 */
public class ExpSwitchView extends View {
    private float gesOriginX;  //手势起点X
    private float gesOriginY; //手势起点Y
    private int DrawOriginX = 0; //画图起点Y
    private int DrawOriginY = 0; //画图起点Y
    private int DrawEndX; //画图终点X
    private int DrawEndY; //画图终点Y
    private int DrawRadii;//画图半径
    private int controlX1 = 0; //1控制点X
    private int controlY1 = 0; //1控制点Y
    private int controlX2 = 0; //2控制点X
    private int controlY2 = 0; //2控制点Y
    private int limi1;//限定值1
    private int limi2;//限定值1
    private int moveDS = 1;//控制点移动距离
    private Paint paint;
    private Paint paint1;
    private Paint paint2;
    private OnExpChangeListener listener;

    Context context;

    private Paint mCenterPaint;
    private Paint mTextPaint;
    private Paint mCenterIncludePoint;

    private int mCenterPointSize = 3;
    private int mTextSize = 12;

    private boolean isChecked = false;
    private Paint mDiagonalLinePaint;

    private Paint mBackGroundPaint;

    public ExpSwitchView(Context context) {
        super(context);
    }

    public ExpSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        this.context = context;
        //线框笔
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.frame_line));
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        PathEffect effects = new DashPathEffect(new float[]{3, 3, 3, 3}, 1);
        paint.setPathEffect(effects);
        paint.setAntiAlias(true);

        //中线笔
        paint1 = new Paint();
        paint1.setColor(getResources().getColor(R.color.center_line));
        paint1.setStrokeWidth(3);
        paint1.setStyle(Paint.Style.STROKE);

        //贝塞尔曲线笔
        paint2 = new Paint();
        paint2.setColor(getResources().getColor(R.color.curve_line));
        paint2.setStrokeWidth(4);
        paint2.setAntiAlias(true);
        paint2.setStyle(Paint.Style.STROKE);
        initAdvance();
    }

    private void initAdvance() {
        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(R.color.center_line));
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setTextSize(Utils.dip2px(context, mTextSize));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);

        mCenterPaint = new Paint();
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setColor(Color.parseColor("#ffffff"));
        mCenterPaint.setStyle(Paint.Style.FILL);

        //include
        mCenterIncludePoint = new Paint();
        mCenterIncludePoint.setAntiAlias(true);
        mCenterIncludePoint.setColor(getResources().getColor(R.color.curve_line));
        mCenterIncludePoint.setStyle(Paint.Style.FILL);

        //diagonal line
        mDiagonalLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDiagonalLinePaint.setColor(getResources().getColor(R.color.aircraft_white));
        mDiagonalLinePaint.setStrokeWidth(3);
        mDiagonalLinePaint.setStyle(Paint.Style.STROKE);
        PathEffect effects = new DashPathEffect(new float[]{3, 3, 3, 3}, 1);
        mDiagonalLinePaint.setPathEffect(effects);
        mDiagonalLinePaint.setAntiAlias(true);

        //bg
        mBackGroundPaint = new Paint();
        mBackGroundPaint.setAntiAlias(true);
        mBackGroundPaint.setColor(getResources().getColor(R.color.back_ground));
        mBackGroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:// 明确指定了
                DrawEndX = specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                DrawEndX = 501;
                break;
        }
        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:// 明确指定了
                DrawEndY = specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                DrawEndY = 501;
                break;
        }
        //计算中心点
        DrawRadii = DrawEndX / 2;
        //计算边界点
        limi1 = (int) (DrawEndX * 0.8);
        limi2 = (int) (DrawEndY * 0.2);
        //计算控制点
        if (controlX1 == 0 && controlY2 == 0) {
            controlX1 = DrawRadii;
            controlY1 = DrawRadii;
            controlX2 = DrawRadii;
            controlY2 = DrawRadii;
        }
        if (DrawEndX != DrawEndY) {
            DrawEndY = DrawEndX;
        }
        setMeasuredDimension(DrawEndX, DrawEndY);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (isChecked) {
            mBackGroundPaint.setColor(getResources().getColor(R.color.back_ground));
            canvas.drawRect(DrawOriginX, DrawOriginY, DrawEndX, DrawEndY, mBackGroundPaint);
            //画矩形及辅助线
            canvas.drawRect(DrawOriginX, DrawOriginY, DrawEndX, DrawEndY, paint);
        } else {
            mBackGroundPaint.setColor(getResources().getColor(R.color.default_back_ground));
            canvas.drawRect(DrawOriginX, DrawOriginY, DrawEndX, DrawEndY, mBackGroundPaint);
        }

        Path lineP1 = new Path();
        lineP1.moveTo(DrawEndX, DrawOriginY);
        lineP1.lineTo(DrawRadii, DrawRadii);
        Path lineP2 = new Path();
        lineP2.moveTo(DrawOriginX, DrawEndY);
        lineP2.lineTo(DrawRadii, DrawRadii);
        canvas.drawPath(lineP1, mDiagonalLinePaint);
        canvas.drawPath(lineP2, mDiagonalLinePaint);
        canvas.drawText("X", DrawEndX - Utils.dip2px(context, mTextSize), DrawEndY / 2 +
                Utils.dip2px(context, mTextSize), mTextPaint);
        canvas.drawText("Y", DrawEndX / 2 + Utils.dip2px(context, mTextSize / 2),
                Utils.dip2px(context, mTextSize), mTextPaint);

//        画中间线
        canvas.drawLine(DrawRadii, DrawOriginY, DrawRadii, DrawEndY, paint1);
        canvas.drawLine(DrawOriginX, DrawRadii, DrawEndX, DrawRadii, paint1);
        //画出第一条贝塞尔曲线
        Path path1 = new Path();
        path1.moveTo(DrawEndX, DrawOriginY);
        path1.quadTo(controlX1, controlY1, DrawRadii, DrawRadii);
        canvas.drawPath(path1, paint2);
        //画出第二条贝塞尔曲线
        Path path2 = new Path();
        path2.moveTo(DrawRadii, DrawRadii);
        path2.quadTo(controlX2, controlY2, DrawOriginX, DrawEndY);
        canvas.drawPath(path2, paint2);

        //center point
        canvas.drawCircle(DrawEndX / 2, DrawEndY / 2, Utils.dip2px(context, mCenterPointSize), mCenterPaint);
        if (isChecked) {
            canvas.drawCircle(DrawEndX / 2, DrawEndY / 2,  Utils.dip2px(context, mCenterPointSize * 0.7f), mCenterIncludePoint);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            gesOriginX = event.getX();
            gesOriginY = event.getY();
            if (!isChecked) {
                isChecked = !isChecked;
                invalidate();
                if (listener != null) {
                    listener.onExpStatusChanged(this, true);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (gesOriginY - event.getY() > 5) {
                if (controlX1 > DrawRadii && controlX2 < DrawRadii) {
                    controlX1 -= moveDS;
                    controlX2 += moveDS;
                } else {
                    controlX1 = DrawRadii;
                    controlX2 = DrawRadii;
                }
                if (controlX1 == DrawRadii) {
                    if ((controlY2 + moveDS) < limi1 && controlY1 > limi2) {
                        controlY1 -= moveDS;
                        controlY2 += moveDS;
                    } else {
                        controlY2 = limi1;
                        controlY1 = limi2;
                    }
                }
                if (controlX1 == DrawRadii && listener != null) {
                    listener.onExpChange(this, getValue((double) controlY2 / DrawEndY));
                } else if (controlY1 == DrawRadii && listener != null) {
                    listener.onExpChange(this, getValue((double) controlX2 / DrawEndX));
                }
                gesOriginY = event.getY();
                invalidate();
            } else if (gesOriginY - event.getY() < -5) {
                if (controlY1 < DrawRadii && controlY2 > DrawRadii) {
                    controlY1 += moveDS;
                    controlY2 -= moveDS;
                } else {
                    controlY1 = DrawRadii;
                    controlY2 = DrawRadii;
                }
                if (controlY1 == DrawRadii) {
                    if (controlX1 < limi1 && controlX2 > limi2) {
                        controlX1 += moveDS;
                        controlX2 -= moveDS;
                    } else {
                        controlX1 = limi1;
                        controlX2 = limi2;
                    }
                }
                if (controlY1 == DrawRadii && listener != null) {
                    listener.onExpChange(this, getValue((double) controlX2 / DrawEndX));
                } else if (listener != null) {
                    listener.onExpChange(this, getValue((double) controlY2 / DrawEndY));
                }
                gesOriginY = event.getY();
                invalidate();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null)
                if (controlX1 == DrawRadii) {
                    listener.onExpChangeEnd(this, getValue((double) controlY2 / DrawEndY));
                } else if (controlY1 == DrawRadii) {
                    listener.onExpChangeEnd(this, getValue((double) controlX2 / DrawEndX));
                }
        }
        return true;
    }

    public interface OnExpChangeListener {
        void onExpChange(ExpSwitchView view, double y);

        void onExpChangeEnd(ExpSwitchView view, double y);

        void onExpStatusChanged(ExpSwitchView view, boolean isChecked);
    }


    public void setOnExpValue(float y) {
        if (y == Double.NaN) {
            y = 0.0f;
        }
        if (y <= -1) {
            y = -1;
        } else if (y >= 1) {
            y = 1;
        }
        double b1 = getHalfUp((y * 3 + 5) / 10);
        if (b1 < 0.5) {
            controlY1 = DrawRadii;
            controlY2 = DrawRadii;
            controlX1 = DrawRadii - (int) (DrawEndX * b1) + DrawRadii;
            controlX2 = (int) (DrawEndX * b1);
            invalidate();
        } else if (b1 > 0.5) {
            controlX1 = DrawRadii;
            controlX2 = DrawRadii;
            controlY1 = DrawRadii - (int) (DrawEndX * b1) + DrawRadii;
            controlY2 = (int) (DrawEndY * b1);
            invalidate();
        } else if (b1 == 0.5) {
            controlX1 = DrawRadii;
            controlX2 = DrawRadii;
            controlY1 = DrawRadii;
            controlY2 = DrawRadii;
            invalidate();
        }
    }

    //通过算法做值映射
    private double getValue(double value) {
//        BigDecimal b = new BigDecimal(value);
//        double f1 = b.setScale(2, RoundingMode.HALF_UP).doubleValue();
        return getHalfUp((value * 10 - 5) / 3);
    }

    public double getCurrentValue() {
        double currentValue = getValue((double) controlY2 / DrawEndY);
        return currentValue;
    }

    //四舍五入运算
    private double getHalfUp(double value) {
        if (value == Double.NaN) {
            value = 0.0;
        }
        double f1 = 0.0;
        try {
            BigDecimal b = new BigDecimal(value);
            f1 = b.setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f1;
    }

    /**
     * set background color
     *
     * @param checkedState default is false
     */
    public void setCheckedState(boolean checkedState) {
        if (checkedState == isChecked) {
            return;
        }
        this.isChecked = checkedState;
        this.invalidate();
    }

    public void setOnExpChangeListener(OnExpChangeListener listener) {
        this.listener = listener;
    }
}

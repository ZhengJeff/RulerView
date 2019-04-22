package com.jeff.ruler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RulerView extends View {

    /**
     * 默认的竖直标记的高度
     */
    private static final int DEFAULT_VERTICAL_SIZE = 10;
    /**
     * 文本与ruler的间距
     */
    private static final int DEFAULT_TEXT_MARGIN = 10;
    /**
     * 将竖线可点击范围进行一定的扩张
     */
    private static final int DEFAULT_VERTICAL_WIDTH_OFFSET = 15;
    /**
     * 默认的线条颜色
     */
    private static final int DEFAULT_PAINT_COLOR = Color.GRAY;
    /**
     * 线条宽度
     */
    private static final int OFFSET = 1;
    /**
     * 默认的平分个数
     */
    private static final int SCALE_SIZE = 4;
    /**
     * 绘制线条的画笔
     */
    private Paint mPaint;
    /**
     * 绘制文本的画笔
     */
    private Paint mTextPaint;
    /**
     * 存放线条的边界
     */
    private Rect mRect;
    /**
     * 竖直线条高度
     */
    private int verticalSize = DEFAULT_VERTICAL_SIZE;
    /**
     * 画笔颜色
     */
    private int paintColor = DEFAULT_PAINT_COLOR;
    /**
     * 竖直线条个数
     */
    private int partSize = SCALE_SIZE;
    /**
     * Ruler上的游标图片
     */
    private Drawable mButtonImage;
    /**
     * 图片的边界
     */
    private Rect imgRect;
    /**
     * 手指按下的初始位置
     */
    private float startX;
    /**
     * 记录每个标记指定的边界位置
     */
    private List<Rect> varReacts = new ArrayList<>();
    /**
     * 判断是否点击的是游标图片
     */
    private boolean isButton = false;
    /**
     * 数据集合
     */
    private List<RulerData> mData;
    /**
     * ruler监听
     */
    private OnRulerChangeListener mListener;
    private int maxLeft;
    private int maxRight;
    private int currentPosition = 0；

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //初始化画笔
        mPaint = new Paint();
        mPaint.setColor(paintColor);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics()));
        //线条的边界
        mRect = new Rect();

        mTextPaint = new Paint();
        mTextPaint.setColor(paintColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics()));
        //自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
        paintColor = typedArray.getColor(R.styleable.RulerView_rulerLineColor, Color.GRAY);
        verticalSize = typedArray.getInteger(R.styleable.RulerView_rulerVerticalHeight, 10);
        mButtonImage = typedArray.getDrawable(R.styleable.RulerView_buttonImageSrc);
        if (mButtonImage == null) {
            throw new RuntimeException("请设置游标图片");
        }
        //图片边界
        imgRect = new Rect();
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int hDode = MeasureSpec.getMode(heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        //计算控件宽高
        if (hDode == MeasureSpec.AT_MOST) {
            //如果是wrap_content，取最图片高度和竖直线条最大值然后加上文字margin和文本字体大小
            heightSize = (int) (Math.max(mButtonImage.getIntrinsicHeight(), verticalSize)
                    + DEFAULT_TEXT_MARGIN + mTextPaint.getTextSize());
        }
        if (wMode == MeasureSpec.AT_MOST) {
            //如果宽度是wrap_content，通过数据的展示文本最大值加上图片宽度设置为宽度
            if (mData != null) {
                int lengthSum = 0;
                for (RulerData mDatum : mData) {
                    lengthSum += mDatum.getText().length();
                }
                widthSize = (int) (lengthSum * mTextPaint.getTextSize())
                        + mButtonImage.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight();
            }
        }
        //设置宽高
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        if (mData == null || mData.isEmpty()) {
            return;
        }

        //计算横线的边界数值
        mRect.top = imgRect.centerY() - OFFSET;
        mRect.bottom = imgRect.centerY() + OFFSET;
        mRect.left = getPaddingLeft() + maxLeft;
        mRect.right = width - maxRight - getPaddingRight();
        //绘制横线
        canvas.drawRect(mRect, mPaint);
        //计算每份的距离大小
        int priceSize = mRect.width() / (partSize - 1);
        //设置竖线的顶部边界
        mRect.top = mRect.top - OFFSET - DEFAULT_VERTICAL_SIZE;
        //遍历创建所有竖线
        for (int i = 0; i < partSize; i++) {
            //最后一个竖线绘制地方需要往里面移动
            if (i == priceSize - 1) {
                mRect.left = priceSize * i + maxLeft + getPaddingLeft() - OFFSET * 2;
                mRect.right = priceSize * i + maxLeft + getPaddingLeft();
            } else {
                mRect.left = priceSize * i  + maxLeft + getPaddingLeft();
                mRect.right = mRect.left + OFFSET * 2;
            }
            //将每个竖线的边界记录
            varReacts.get(i).set(mRect.left - DEFAULT_VERTICAL_WIDTH_OFFSET
                    , 0, mRect.right + DEFAULT_VERTICAL_WIDTH_OFFSET, getMeasuredHeight());
            //绘制竖线
            canvas.drawRect(mRect, mPaint);
            //绘制竖线上展示的文本
            String text = mData.get(i).getText();
            canvas.drawText(text, mRect.centerX() - text.length() * 1.0f / 2 * mTextPaint.getTextSize(), imgRect.top - 10, mTextPaint);
        }
        if(currentPosition != -1){
            Rect rect = varReacts.get(currentPosition);
            imgRect.left = rect.centerX() - mButtonImage.getIntrinsicWidth() / 2;
            imgRect.right = rect.centerX() + mButtonImage.getIntrinsicWidth() / 2;
        }
        //为游标设置边界
        mButtonImage.setBounds(imgRect);
        //绘制游标
        mButtonImage.draw(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断触摸下去的是不是游标图片
                if (!imgRect.contains((int) event.getX(), (int) event.getY())) {
                    isButton = false;
                    break;
                }
                currentPosition = -1;
                //如果是，记录点击的X坐标
                isButton = true;
                startX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //move事件只有确定是点击的游标才会触发移动游标事件
                if (isButton) {
                    //计算移动的距离
                    float distance = event.getX() - startX;
                    //记录移动后的X坐标
                    startX = event.getX();
                    //计算移动后的左右坐标
                    float left = imgRect.left + distance;
                    float right = imgRect.right + distance;
                    //如果左右坐标超出了横线左右位置则不做移动
                    if (left < getPaddingLeft() + maxLeft - mButtonImage.getIntrinsicWidth() / 2
                            || right > getMeasuredWidth() - getPaddingRight() - maxRight + mButtonImage.getIntrinsicWidth() / 2) {
                        break;
                    }
                    //重新设置游标的位置
                    imgRect.left = (int) left;
                    imgRect.right = (int) right;

                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isButton) {
                    //如果点击的是游标，则在停止移动后将游标设置到最近的竖线位置
                    //横线的宽度
                    int lineWidth = getMeasuredWidth() - maxLeft - maxRight - getPaddingLeft() - getPaddingRight();
                    //每份间距大小
                    int price = lineWidth / (partSize - 1);
                    //计算当前游标中心点在那个比例
                    double v = (imgRect.centerX() - getPaddingLeft()) * 1.0 / price;
                    //四舍五入计算位置
                    int size = (int) Math.round(v);
                    //设置游标边界
                    imgRect.left = price * size + maxLeft + getPaddingLeft() - mButtonImage.getIntrinsicWidth() / 2;
                    imgRect.right = price * size + maxLeft + getPaddingLeft() + mButtonImage.getIntrinsicWidth() / 2;
                    //监听回调移动数据
                    currentPosition = size;
                    if (mListener != null) {
                        mListener.onRulerChange(size, mData.get(size));
                    }
                    invalidate();
                } else {
                    //单机点击后移动游标到指定位置
                    for (int i = 0; i < varReacts.size(); i++) {
                        Rect rect = varReacts.get(i);
                        //计算点击位置是哪个竖线
                        if (rect.contains((int) event.getX(), (int) event.getY())) {
                            currentPosition = i;
                            this.imgRect.left = rect.centerX() - mButtonImage.getIntrinsicWidth() / 2;
                            this.imgRect.right = rect.centerX() + mButtonImage.getIntrinsicWidth() / 2;
                            if (mListener != null) {
                                mListener.onRulerChange(i, mData.get(i));
                            }
                            invalidate();
                            break;
                        }
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 设置线的颜色
     * @param color 颜色值
     */
    public void setLineColor(@ColorInt int color) {
        paintColor = color;
        invalidate();
    }

    /**
     * 设置竖线的高度
     * @param height 高度值
     */
    public void setVerticalSize(int height) {
        verticalSize = height;
        invalidate();
    }

    /**
     * 设置数据
     * @param data 数据集合
     */
    public void setData(List<RulerData> data) {
        //数据不能设置为null或者空数据
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("Ruler的数据不能为空");
        }
        mData = data;
        //设置竖线个数
        partSize = data.size();
        //清除里面的数据
        varReacts.clear();
        //创建指定数量的记录
        for (int i = 0; i < partSize; i++) {
            varReacts.add(new Rect());
        }
        maxLeft = Math.max(mButtonImage.getIntrinsicWidth() / 2
                ,(int)(mData.get(0).getText().length() * mTextPaint.getTextSize() / 2));
        maxRight = Math.max(mButtonImage.getIntrinsicWidth() / 2
                ,(int)(mData.get(mData.size() - 1).getText().length() * mTextPaint.getTextSize() / 2));
        imgRect.set(maxLeft - mButtonImage.getIntrinsicWidth() / 2 + getPaddingLeft()
                ,(int) (getPaddingTop() + DEFAULT_TEXT_MARGIN + mTextPaint.getTextSize())
                , maxLeft + getPaddingLeft() + mButtonImage.getIntrinsicWidth() / 2
                ,(int) (getPaddingTop() + DEFAULT_TEXT_MARGIN + mTextPaint.getTextSize()) +
                        mButtonImage.getIntrinsicHeight());
        invalidate();
    }
    
    /**
    * 设置当前游标所在位置
    **/
    public void setCurrentPosition(final int position) {
        if (mData == null || mData.isEmpty()) {
            throw new RuntimeException("当前没有设置数据");
        }
        if (position < 0 || position >= mData.size()) {
            throw new IndexOutOfBoundsException();
        }
        currentPosition = position;
        invalidate();
    }
    
    /**
    * 获取当前游标所在位置
    **/
    public int getCurrentPosistion(){
        return currentPosition;
    }

    /**
     * Ruler的回调
     */
    public interface OnRulerChangeListener {
        void onRulerChange(int position, RulerData curData);

    }

    /**
     * 设置回调
     */
    public void setOnRulerChangeListener(OnRulerChangeListener listener) {
        this.mListener = listener;
    }
}

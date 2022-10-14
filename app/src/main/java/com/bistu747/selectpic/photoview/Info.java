package com.bistu747.selectpic.photoview;

import android.graphics.PointF;
import android.graphics.RectF;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by liuheng on 2015/8/19.
 */
public class Info {

    // 内部图片在整个手机界面的位置
    RectF mRect = new RectF();

    // 控件在窗口的位置
    RectF mImgRect = new RectF();

    RectF mWidgetRect = new RectF();

    RectF mBaseRect = new RectF();

    PointF mScreenCenter = new PointF();

    float mScale;

    float mDegrees;

    AppCompatImageView.ScaleType mScaleType;

    public Info(RectF rect, RectF img, RectF widget, RectF base, PointF screenCenter, float scale, float degrees, AppCompatImageView.ScaleType scaleType) {
        mRect.set(rect);
        mImgRect.set(img);
        mWidgetRect.set(widget);
        mScale = scale;
        mScaleType = scaleType;
        mDegrees = degrees;
        mBaseRect.set(base);
        mScreenCenter.set(screenCenter);
    }
}
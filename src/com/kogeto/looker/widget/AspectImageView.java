package com.kogeto.looker.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectImageView extends ImageView {

	
    public AspectImageView(Context context) {
        super(context);
    }

    
    public AspectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    
    public AspectImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Drawable drawable = getDrawable();
        if (drawable != null)
        {
            int width =  MeasureSpec.getSize(widthMeasureSpec);
            int diw = drawable.getIntrinsicWidth();
            if (diw > 0)
            {
                int height = width * drawable.getIntrinsicHeight() / diw;
                setMeasuredDimension(width, height);
            }
            else
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }}
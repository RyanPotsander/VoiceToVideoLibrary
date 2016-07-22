package com.ryanpotsander.androidcv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Ryan on 7/18/16.
 */
public class GreyItemDecoration extends RecyclerView.ItemDecoration{


    Drawable mItemDivider;

    public GreyItemDecoration(Context context, int drawableResource){

        mItemDivider = ContextCompat.getDrawable(context, drawableResource);


    }


    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {


        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mItemDivider.getIntrinsicHeight();

            mItemDivider.setBounds(left, top, right, bottom);
            mItemDivider.draw(c);
        }

    }
}

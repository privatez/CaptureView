package com.laifu.scan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.CheckBox;

/**
 * Created by private on 2017/2/17.
 */

class CaptureEventView extends CheckBox {

    private Context mContext;

    private CaptureEventViewParameter mParameter;

    private int mButtonWidth;

    private boolean isFirst;

    public CaptureEventView(Context context, CaptureEventViewParameter parameter) {
        super(context);
        init(context, parameter);
    }

    private void init(Context context, CaptureEventViewParameter parameter) {
        mContext = context;
        mParameter = parameter;
        isFirst = true;

        setChecked(false);
        setButtonDrawable(null);
        Drawable drawable = createDrawableSelector(
                mContext, mParameter.mCheckedResId, mParameter.mUnCheckedResId, mParameter.mPressedResId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        setCompoundDrawables(drawable, null, null, null);
        setOnCheckedChangeListener(parameter.mOnCheckedChangeListener);

        mButtonWidth = drawable.getMinimumWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFirst) {
            int padding = (canvas.getWidth() - mButtonWidth) / 2;
            if (padding > 0) {
                setPadding(padding, 0, 0, 0);
                isFirst = false;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    private StateListDrawable createDrawableSelector(Context context, int checkedResId, int unCheckedResId, int pressedResId) {
        Drawable checked = context.getResources().getDrawable(checkedResId);
        Drawable unchecked = context.getResources().getDrawable(unCheckedResId);
        Drawable pressed = context.getResources().getDrawable(pressedResId);
        StateListDrawable stateList = new StateListDrawable();
        int statePressed = android.R.attr.state_pressed;
        int stateChecked = android.R.attr.state_checked;
        if (pressed != null) {
            stateList.addState(new int[]{statePressed}, pressed);
        }
        if (checked != null) {
            stateList.addState(new int[]{stateChecked}, checked);
        }
        if (unchecked != null) {
            stateList.addState(new int[]{}, unchecked);
        }
        return stateList;
    }
}

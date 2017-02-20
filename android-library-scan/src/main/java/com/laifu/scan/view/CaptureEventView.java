package com.laifu.scan.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.CheckBox;

/**
 * Created by private on 2017/2/17.
 */

class CaptureEventView extends CheckBox {

    private Context mContext;

    private CaptureEventViewParameter mParameter;

    public CaptureEventView(Context context, CaptureEventViewParameter parameter) {
        super(context);
        init(context, parameter);
    }

    private void init(Context context, CaptureEventViewParameter parameter) {
        mContext = context;
        mParameter = parameter;

        setChecked(false);
        setButtonDrawable(null);

        setBackgroundDrawable(createDrawableSelector(
                mContext, mParameter.mCheckedResId, mParameter.mUnCheckedResId, mParameter.mPressedResId));
        setOnCheckedChangeListener(parameter.mOnCheckedChangeListener);
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

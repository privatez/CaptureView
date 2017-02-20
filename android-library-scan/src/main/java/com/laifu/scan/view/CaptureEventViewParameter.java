package com.laifu.scan.view;

/**
 * Created by private on 2017/2/20.
 */

class CaptureEventViewParameter {
    public int mCheckedResId;
    public int mUnCheckedResId;
    public int mPressedResId;
    public OnCaptureEventViewCheckedChangeListener mOnCheckedChangeListener;

    public CaptureEventViewParameter(int checkedResId, int unCheckedResId, int pressedResId,
                                     OnCaptureEventViewCheckedChangeListener onCheckedChangeListener) {
        mCheckedResId = checkedResId;
        mUnCheckedResId = unCheckedResId;
        mPressedResId = pressedResId;
        mOnCheckedChangeListener = onCheckedChangeListener;
    }
}

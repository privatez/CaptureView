package com.onehash.utils;

import android.content.res.Resources;

/**
 * Created by private on 2017/2/17.
 */

public class ScreenUtils {
    private ScreenUtils() {

    }

    /**
     * 获得屏幕宽度
     *
     * @returns
     */
    public static int getScreenWidth() {

        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    /**
     * 获得屏幕高度
     *
     * @return
     */
    public static int getScreenHeight() {

        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static float getScreenDensity() {

        return Resources.getSystem().getDisplayMetrics().density;
    }

    public static int getScreenDpi() {

        return Resources.getSystem().getDisplayMetrics().densityDpi;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        final float scale = getScreenDensity();
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(float pxValue) {
        final float scale = getScreenDensity();
        return (int) (pxValue / scale + 0.5f);
    }


}

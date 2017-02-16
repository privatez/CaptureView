package com.onehash.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by private on 2017/2/8.
 */

public class ToastHelper {
    public static boolean isShow = true;

    private static Toast mToast;

    private static Context mContext;

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort(CharSequence message) {
        show(message, Toast.LENGTH_SHORT);
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort(int message) {
        show(message, Toast.LENGTH_SHORT);
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(CharSequence message) {
        show(message, Toast.LENGTH_LONG);
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(int message) {
        show(message, Toast.LENGTH_LONG);
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show(int message, int duration) {
        show(String.valueOf(message), duration);
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show(CharSequence message, int duration) {
        if (mContext == null) {
            LogHelper.log("mContext is null!");
            return;
        }

        if (isShow) {
            if (mToast == null) {
                mToast = Toast.makeText(mContext, message, duration);
            } else {
                mToast.setText(message);
            }
            mToast.show();
        }
    }

}

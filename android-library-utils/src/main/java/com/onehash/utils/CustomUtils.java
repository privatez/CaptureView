package com.onehash.utils;

import android.graphics.Paint;

import java.util.UUID;

/**
 * Created by private on 2017/2/17.
 */

public class CustomUtils {

    private CustomUtils() {

    }

    public static int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

}

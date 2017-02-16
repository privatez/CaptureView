package com.laifu.scan.utils;

import com.onehash.utils.ToastHelper;

/**
 * Created by private on 2017/2/8.
 */

public class CustomUtil {
    public static void showErrorHint(String hint) {
        ToastHelper.showLong(hint);
    }

    public static byte[] rotateImage(byte[] data, int width, int height) {
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }

        return rotatedData;
    }
}

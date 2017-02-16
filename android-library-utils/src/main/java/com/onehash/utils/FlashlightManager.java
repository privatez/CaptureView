package com.onehash.utils;

import android.hardware.Camera;

/**
 * Created by private on 2017/2/14.
 */

public class FlashlightManager {
    private FlashlightManager() {
    }

    public static void openFlashlight(Camera camera) {
        handlerFlashlight(camera, true);
    }

    public static void closeFlashlight(Camera camera) {
        handlerFlashlight(camera, false);
    }

    private static void handlerFlashlight(Camera camera, boolean enable) {
        Camera.Parameters parameters = camera.getParameters();
        if (enable) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        camera.setParameters(parameters);
    }
}

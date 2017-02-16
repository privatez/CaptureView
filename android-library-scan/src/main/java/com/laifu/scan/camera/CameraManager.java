/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.laifu.scan.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.view.SurfaceHolder;

import java.io.IOException;

import static com.laifu.scan.utils.Constant.DECODE_OCR;
import static com.laifu.scan.utils.Constant.DECODE_QECODE;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
public final class CameraManager {

/*    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 520;
    private static final int MAX_FRAME_HEIGHT = 520;*/

    private static final int MIN_QRCODE_FRAME_WIDTH = 240;
    private static final int MIN_QRCODE_FRAME_HEIGHT = 240;
    private static final int MAX_QRCODE_FRAME_WIDTH = 520;
    private static final int MAX_QRCODE_FRAME_HEIGHT = 520;

    private static final int MIN_OCR_FRAME_WIDTH = 120;
    private static final int MIN_OCR_FRAME_HEIGHT = 360;
    private static final int MAX_OCR_FRAME_WIDTH = 160;
    private static final int MAX_OCR_FRAME_HEIGHT = 580;

    private static volatile CameraManager sCameraManager;

    private final CameraConfigurationManager configManager;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;

    private int mDecodeMode = DECODE_QECODE;
    private boolean mFramingRectModifyed;

    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private final PreviewCallback previewCallback;
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which requested them.
     */
    private final AutoFocusCallback autoFocusCallback;

    /**
     * Initializes this static object with the Context of the calling Activity.
     *
     * @param context The Activity which wants to use the camera.
     */
    public static void init(Context context) {
        if (sCameraManager == null) {
            synchronized (CameraManager.class) {
                if (sCameraManager == null) {
                    sCameraManager = new CameraManager(context);
                }
            }
        }
    }

    /**
     * Gets the CameraManager singleton instance.
     *
     * @return A reference to the CameraManager singleton.
     */
    public static CameraManager get() {
        return sCameraManager;
    }

    private CameraManager(Context context) {
        configManager = new CameraConfigurationManager(context);
        previewCallback = new PreviewCallback(configManager);
        autoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public void openDriver(SurfaceHolder holder) throws IOException {
        if (camera == null) {
            camera = Camera.open();
            if (camera == null) {
                throw new IOException();
            }
            camera.setPreviewDisplay(holder);

            if (!initialized) {
                initialized = true;
                configManager.initFromCameraParameters(camera);
            }
            configManager.setDesiredCameraParameters(camera);
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public void startPreview() {
        if (camera != null && !previewing) {
            camera.startPreview();
            previewing = true;
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public void stopPreview() {
        if (camera != null && previewing) {
            camera.stopPreview();
            previewCallback.removeHandler();
            autoFocusCallback.removeHanler();
            previewing = false;
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message);
            camera.setOneShotPreviewCallback(previewCallback);
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message);
            camera.autoFocus(autoFocusCallback);
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {
        if (framingRect == null || mFramingRectModifyed) {
            if (camera == null) {
                return null;
            }
            initFramingRect();
            setFramingRectInPreview(framingRect);
            mFramingRectModifyed = false;
        }
        return framingRect;
    }

    private void initFramingRect() {
        int scale = 4;
        int minFrameWidth = MIN_QRCODE_FRAME_WIDTH;
        int minFrameHeight = MIN_QRCODE_FRAME_HEIGHT;
        int maxFrameWidth = MAX_QRCODE_FRAME_WIDTH;
        int maxFrameHeight = MAX_QRCODE_FRAME_HEIGHT;

        if (DECODE_OCR == mDecodeMode) {
            scale = 5;
            minFrameWidth = MIN_OCR_FRAME_WIDTH;
            minFrameHeight = MIN_OCR_FRAME_HEIGHT;
            maxFrameWidth = MAX_OCR_FRAME_WIDTH;
            maxFrameHeight = MAX_OCR_FRAME_HEIGHT;
        }

        Point screenResolution = configManager.getScreenResolution();
        int width = screenResolution.x * 3 / scale;
        if (width < minFrameWidth) {
            width = minFrameWidth;
        } else if (width > maxFrameWidth) {
            width = maxFrameWidth;
        }
        int height = screenResolution.y * 3 / scale;
        if (height < minFrameHeight) {
            height = minFrameHeight;
        } else if (height > maxFrameHeight) {
            height = maxFrameHeight;
        }
        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 2;
        framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    public Rect getFramingRectInPreview() {
        return framingRectInPreview;
    }

    private void setFramingRectInPreview(Rect rectInPreview) {
        Rect rect = new Rect(rectInPreview);
        Point cameraResolution = configManager.getCameraResolution();
        Point screenResolution = configManager.getScreenResolution();
        rect.left = rect.left * cameraResolution.y / screenResolution.x;
        rect.right = rect.right * cameraResolution.y / screenResolution.x;
        rect.top = rect.top * cameraResolution.x / screenResolution.y;
        rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
        framingRectInPreview = rect;
    }


    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     * 构建全屏解析图片信息 -> 这样不需要对准二维码即可完成扫描 加快二维码解析效率
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildScreenLuminanceSource(byte[] data, int width, int height) {

        return new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height);
    }

    /**
     * 构建只包含扫描区域图片信息 -> OCR 对于小图解析速度快,加快解析效率
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public PlanarYUVLuminanceSource buildTailorLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
    }

    public void setDecodeMode(int decodeMode) {
        mFramingRectModifyed = mDecodeMode != decodeMode;
        mDecodeMode = decodeMode;
        previewCallback.setDecodeMode(decodeMode);
    }

}

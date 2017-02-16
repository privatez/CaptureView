/*
 * Copyright (C) 2010 ZXing authors
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

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import com.laifu.scan.ocr.OcrResultCallback;
import com.laifu.scan.ocr.TessTwoAdapter;
import com.laifu.scan.utils.Constant;
import com.laifu.scan.utils.CustomUtil;
import com.onehash.utils.LogHelper;


final class PreviewCallback implements Camera.PreviewCallback {

    private final CameraConfigurationManager configManager;
    private Handler previewHandler;
    private int previewMessage;
    private int mDecodeMode = Constant.DECODE_QECODE;

    PreviewCallback(CameraConfigurationManager configManager) {
        this.configManager = configManager;
    }

    void setHandler(Handler previewHandler, int previewMessage) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    void removeHandler() {
        previewHandler = null;
        previewMessage = 0;
    }

    void setDecodeMode(int decodeMode) {
        mDecodeMode = decodeMode;
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = configManager.getCameraResolution();
        if (Constant.DECODE_QECODE == mDecodeMode) {
            notifyDecodeQrCode(data, cameraResolution.x, cameraResolution.y);
        } else if (Constant.DECODE_OCR == mDecodeMode) {
            startOCR(data, cameraResolution.x, cameraResolution.y);
        }
    }

    private void notifyDecodeQrCode(byte[] data, int width, int height) {
        if (previewHandler != null) {
            Message message = previewHandler.obtainMessage(previewMessage, width, height, data);
            message.sendToTarget();
            previewHandler = null;
        } else {
            LogHelper.log("Got preview callback, but no handler for it");
        }
    }

    private void startOCR(byte[] data, int width, int height) {
        LogHelper.log("ocr", "startOCR...");
       /* byte[] rotatedData = CustomUtil.rotateImage(data, width, height);*/

        if (configManager.mCamera == null) {
            return;
        }

       /* Rect rect = CameraManager.get().getFramingRectInPreview();

        int area = rect.width() * rect.height();
        byte[] matrix = new byte[area];

        int inputOffset = rect.top * width + rect.left;

        byte[] yuv = data;
        for (int y = 0; y < rect.height(); y++) {
            int outputOffset = y * rect.width();
            System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
            inputOffset += width;
        }

        data = matrix;

        Camera.Size previewSize = configManager.mCamera.getParameters().getPreviewSize();
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);//这里 80 是图片质量，取值范围 0-100，100为品质最高
        data = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Bitmap bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);*/

        byte[] rotatedData = CustomUtil.rotateImage(data, width, height);

        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildTailorLuminanceSource(rotatedData, width, height);

        OcrHelper ocrUtil = new OcrHelper(new TessTwoAdapter(), new OcrResultCallback() {
            @Override
            public void onOcrSuccess(String text) {
                LogHelper.log("ocr", "onOcrSuccess:" + text);
            }

            @Override
            public void onOcrFailed() {
                LogHelper.log("ocr", "onOcrFailed: onOcrFailed!");
            }
        });
        //ocrUtil.startTransaction(source.renderRotateCroppedGreyscaleBitmap());
    }

}

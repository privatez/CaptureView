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

package com.laifu.scan.decoding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.laifu.scan.R;
import com.laifu.scan.camera.CameraManager;
import com.laifu.scan.camera.PlanarYUVLuminanceSource;
import com.laifu.scan.utils.Constant;
import com.laifu.scan.utils.CustomUtil;
import com.onehash.utils.LogHelper;

import java.util.Hashtable;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private Handler mHandler;
    private final MultiFormatReader multiFormatReader;

    private int mDecodeMode = Constant.DECODE_QECODE;

    DecodeHandler(Handler handler, Hashtable<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        mHandler = handler;
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == Constant.DECODE_QECODE) {//Log.d(TAG, "Got decode message");
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if (message.what == Constant.DECODE_OCR) {
            startOcr((byte[]) message.obj, message.arg1, message.arg2);
        } else if (message.what == R.id.quit) {
            Looper.myLooper().quit();
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;

        //modify here

        byte[] rotatedData = CustomUtil.rotateImage(data, width, height);

        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildScreenLuminanceSource(rotatedData, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }

        if (rawResult != null) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
            Message message = Message.obtain(mHandler, R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
            message.setData(bundle);
            //Log.d(TAG, "Sending decode succeeded message...");
            message.sendToTarget();
        } else {
            Message message = Message.obtain(mHandler, R.id.decode_failed);
            message.sendToTarget();
        }
    }

    private void startOcr(byte[] data, int width, int height) {
        LogHelper.log("ocr", "startOcr...");

        byte[] rotatedData = CustomUtil.rotateImage(data, width, height);

        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildTailorLuminanceSource(rotatedData, width, height);

        /*OcrHelper ocrUtil = new OcrHelper(new TessTwoAdapter(), new OcrResultCallback() {
            @Override
            public void onOcrSuccess(String text) {
                LogHelper.log("ocr", "onOcrSuccess:" + text);
            }

            @Override
            public void onOcrFailed() {
                LogHelper.log("ocr", "onOcrFailed: onOcrFailed!");
            }
        });
        ocrUtil.startTransaction(source.renderRotateCroppedGreyscaleBitmap());*/
    }

    void setDecodeMode(int decodeMode) {
        mDecodeMode = decodeMode;
    }

}

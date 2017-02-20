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

package com.laifu.scan.decoding;

import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 * �����߳�
 */
public final class DecodeThread extends Thread {

    public static final String BARCODE_BITMAP = "barcode_bitmap";

    private Handler mDecodeHandler;
    private Handler mCaptureHandler;
    private final CountDownLatch mHandlerInitLatch;
    private final Hashtable<DecodeHintType, Object> mHints;

    public DecodeThread(Handler handler, ResultPointCallback resultPointCallback) {
        this(handler, null, null, resultPointCallback);
    }

    public DecodeThread(Handler handler,
                        Vector<BarcodeFormat> decodeFormats,
                        String characterSet,
                        ResultPointCallback resultPointCallback) {

        mCaptureHandler = handler;
        mHandlerInitLatch = new CountDownLatch(1);

        mHints = new Hashtable<>(3);

        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<>();
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }

        mHints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null) {
            mHints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }

        mHints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    }

    public Handler getDecodeHandler() {
        try {
            mHandlerInitLatch.await();
        } catch (InterruptedException ie) {
        }
        return mDecodeHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mDecodeHandler = new DecodeHandler(mCaptureHandler, mHints);
        mHandlerInitLatch.countDown();
        Looper.loop();
    }

}

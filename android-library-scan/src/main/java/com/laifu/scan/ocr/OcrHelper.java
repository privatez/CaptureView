package com.laifu.scan.ocr;

import android.graphics.Bitmap;

import com.laifu.scan.ocr.model.OcrImgParameter;

/**
 * Created by private on 2017/2/21.
 */

public class OcrHelper {

    private OcrAdapter mOcrAdapter;
    private OcrResultCallback mResultCallback;

    public OcrHelper(OcrAdapter ocrAdapter, OcrResultCallback resultCallback) {
        mOcrAdapter = ocrAdapter;
        mResultCallback = resultCallback;
    }

    public void getOcrText(String imgPath) {
        getOcrText(new OcrImgParameter(OcrImgParameter.OCR_FILE, imgPath));
    }

    public void getOcrText(Bitmap img) {
        getOcrText(new OcrImgParameter(OcrImgParameter.OCR_BITMAP, img));
    }

    public void getOcrText(byte[] data, int width, int height) {
        getOcrText(new OcrImgParameter(OcrImgParameter.OCR_BYTE, data, width, height));
    }

    private void getOcrText(OcrImgParameter param) {

        mOcrAdapter.getOcrText(param, mResultCallback);
    }

    public void stop() {
        mOcrAdapter.stop();
    }

    public void close() {
        mOcrAdapter.close();
    }
}

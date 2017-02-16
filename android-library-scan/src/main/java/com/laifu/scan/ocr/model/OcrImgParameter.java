package com.laifu.scan.ocr.model;

import android.graphics.Bitmap;

/**
 * Created by private on 2017/2/13.
 */

public class OcrImgParameter {

    public static final int OCR_FILE = 1;
    public static final int OCR_BITMAP = 2;
    public static final int OCR_BYTE = 3;

    public int mOcrMode;
    public Bitmap mImgBmp;
    public String mImgPath;
    public byte[] mImgData;
    public int mImgWidth;
    public int mImgHeight;
    public String mId;

    public OcrImgParameter(int ocrMode, Bitmap imgBmp) {
        mOcrMode = ocrMode;
        mImgBmp = imgBmp;
    }

    public OcrImgParameter(int ocrMode, String imgPath) {
        mOcrMode = ocrMode;
        mImgPath = imgPath;
    }

    public OcrImgParameter(int ocrMode, byte[] imgData, int imgWidth, int imgHeight) {
        mOcrMode = ocrMode;
        mImgData = imgData;
        mImgWidth = imgWidth;
        mImgHeight = imgHeight;
    }

    public void setId(String id) {
        mId = id;
    }
}

package com.laifu.scan.ocr.model;

import android.os.AsyncTask;

/**
 * Created by private on 2017/2/16.
 */

public class OcrResult {
    public String mId;
    public String mOcrText;
    public AsyncTask mAsyncTask;

    public OcrResult(String id, String ocrText, AsyncTask asyncTask) {
        mId = id;
        mOcrText = ocrText;
        mAsyncTask = asyncTask;
    }
}

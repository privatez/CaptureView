package com.laifu.scan.ocr;

import com.laifu.scan.ocr.model.OcrResult;

/**
 * Created by private on 2017/2/13.
 */

public interface OcrResultCallback {
    void onOcrSuccess(OcrResult result);

    void onOcrFailed();
}

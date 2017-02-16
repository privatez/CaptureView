package com.laifu.scan.ocr;

import com.laifu.scan.ocr.model.OcrImgParameter;

/**
 * Created by private on 2017/2/13.
 */

public interface OcrAdapter {
    void initThirdApi();

    void getOcrText(OcrImgParameter param, OcrResultCallback callback);

    void stop();

    void close();
}

package com.laifu.scan.ocr;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.laifu.scan.ocr.model.OcrImgParameter;

/**
 * Created by private on 2017/2/13.
 */

public class TessTwoAdapter extends OcrBaseAdapter {

    private static final String TESSDATA_PARENT_FOLDER = "/mnt/sdcard/tesseract/";

    private static final String TESSDATA_ENGLISH = "eng";

    private TessBaseAPI mTessBaseAPI;

    @Override
    public void initThirdApi() {
        mTessBaseAPI = new TessBaseAPI();
        //记得要在你的sd卡的tessdata文件夹下放对应的字典文件,例如我这里就放的是custom.traineddata
        mTessBaseAPI.init(TESSDATA_PARENT_FOLDER, TESSDATA_ENGLISH);
        mTessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        isApiInited = true;
    }

    @Override
    public void getOcrText(OcrImgParameter param, OcrResultCallback callback) {
        checkApiInited();
        OcrAsyncTask task = new OcrAsyncTask(mTessBaseAPI, param, callback);
        task.execute();
    }

    @Override
    public void stop() {
        mTessBaseAPI.stop();
    }

    @Override
    public void close() {
        mTessBaseAPI.end();
    }

}
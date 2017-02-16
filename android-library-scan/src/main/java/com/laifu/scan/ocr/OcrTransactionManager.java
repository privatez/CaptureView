package com.laifu.scan.ocr;

import android.graphics.Bitmap;

import com.laifu.scan.ocr.model.OcrImgParameter;
import com.laifu.scan.ocr.model.OcrResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by private on 2017/2/14.
 */

public class OcrTransactionManager implements OcrResultCallback {

    private static final int MIN_OCRING_MAX_TOTAL = 1;
    private static final int MAX_OCRING_MAX_TOTAL = 5;
    private static final int NOMAL_OCRING_MAX_TOTAL = 3;

    private static final int MIN_WAITING_MAX_TOTAL = 5;
    private static final int MAX_WAITING_MAX_TOTAL = 10;

    private List<OcrImgParameter> mWaitingOcrModels;
    private Map<String, OcrImgParameter> mOcringModels;

    private int mMaxOcring;
    private int mMaxWaiting;
    private int mOcringTotal;

    private OcrAdapter mOcrAdapter;

    public OcrTransactionManager() {
        init();
    }

    private void init() {
        mMaxOcring = NOMAL_OCRING_MAX_TOTAL;
        mMaxWaiting = MIN_WAITING_MAX_TOTAL;
        mWaitingOcrModels = new ArrayList<>(mMaxOcring);
        mOcringModels = new HashMap<>(mMaxWaiting);
        mOcrAdapter = new TessTwoAdapter();
    }

    @Override
    public void onOcrSuccess(OcrResult result) {

    }

    @Override
    public void onOcrFailed() {

    }

    private void clearTransaction() {

    }

    public void startTransaction(String imgPath) {
        startTransaction(new OcrImgParameter(OcrImgParameter.OCR_FILE, imgPath));
    }

    public void startTransaction(Bitmap img) {
        startTransaction(new OcrImgParameter(OcrImgParameter.OCR_BITMAP, img));
    }

    public void startTransaction(byte[] data, int width, int height) {
        startTransaction(new OcrImgParameter(OcrImgParameter.OCR_BYTE, data, width, height));
    }

    private void startTransaction(OcrImgParameter param) {

        mOcrAdapter.getOcrText(param, this);
    }

    public void stop() {
        mOcrAdapter.stop();
    }

    public void close() {
        mOcrAdapter.close();
    }

    private void release() {
        mOcringModels.clear();
        mWaitingOcrModels.clear();
    }

    private String getUUID() {
        return UUID.randomUUID().toString();
    }

    public void setMaxOcring(int maxOcring) {
        if (maxOcring < MIN_OCRING_MAX_TOTAL) {
            mMaxOcring = MIN_OCRING_MAX_TOTAL;
        } else if (maxOcring > MAX_OCRING_MAX_TOTAL) {
            mMaxOcring = MAX_OCRING_MAX_TOTAL;
        } else {
            mMaxOcring = maxOcring;
        }
    }

    public void setMaxWaiting(int maxWaiting) {
        if (maxWaiting < MIN_WAITING_MAX_TOTAL) {
            mMaxWaiting = MIN_WAITING_MAX_TOTAL;
        } else if (maxWaiting > MAX_WAITING_MAX_TOTAL) {
            mMaxWaiting = MAX_WAITING_MAX_TOTAL;
        } else {
            mMaxWaiting = maxWaiting;
        }
    }

}

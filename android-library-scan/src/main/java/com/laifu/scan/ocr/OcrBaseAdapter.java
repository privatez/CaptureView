package com.laifu.scan.ocr;

/**
 * Created by private on 2017/2/13.
 */

public abstract class OcrBaseAdapter implements OcrAdapter {

    protected boolean isApiInited = false;

    public OcrBaseAdapter() {
        initThirdApi();
    }

    protected void checkApiInited() {
        if (!isApiInited) {
            throw new OcrRuntimeException("The third party api must init!");
        }
    }

    protected class OcrRuntimeException extends RuntimeException {
        public OcrRuntimeException(String msg) {
            super(msg);
        }
    }

}

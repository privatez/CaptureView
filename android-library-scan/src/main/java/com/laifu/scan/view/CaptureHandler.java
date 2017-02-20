package com.laifu.scan.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;
import com.laifu.scan.R;
import com.laifu.scan.decoding.DecodeThread;

/**
 * Created by private on 2017/2/20.
 */

class CaptureHandler extends Handler {

    private static final int STATE_PREVIEW = 1;
    private static final int STATE_DONE = 2;

    private CaptureView view;
    private int state;

    public CaptureHandler(CaptureView view) {
        this.view = view;
        state = STATE_PREVIEW;
    }

    @Override
    public void handleMessage(Message message) {
        int id = message.what;
        if (id == R.id.auto_focus) {
            if (state == STATE_PREVIEW) {
                view.requestAutoFocus();
            }
        } else if (id == R.id.restart_preview) {
            state = STATE_PREVIEW;
        } else if (id == R.id.decode_succeeded) {
            state = STATE_DONE;
            Bundle bundle = message.getData();
            Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
            view.handleDecode((Result) message.obj, barcode);
        } else if (id == R.id.decode_failed) {
            state = STATE_PREVIEW;
            view.requestPreviewFrame();
        }
    }

}

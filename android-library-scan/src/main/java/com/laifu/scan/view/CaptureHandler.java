package com.laifu.scan.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.laifu.scan.R;
import com.laifu.scan.decoding.DecodeThread;
import com.laifu.scan.utils.Constant;

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
            Bitmap bitmap = null;
            int decodeMode = Constant.DECODE_QECODE;
            if (bundle != null) {
                bitmap = bundle.getParcelable(DecodeThread.CAPTURE_BITMAP);
                decodeMode = bundle.getInt(DecodeThread.DECODE_MODE);
            }
            view.handleDecode(decodeMode, message.obj.toString(), bitmap);
        } else if (id == R.id.decode_failed) {
            state = STATE_PREVIEW;
            view.requestPreviewFrame();
        }
    }

}

package com.laifu.scan.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.google.zxing.Result;
import com.laifu.scan.R;
import com.laifu.scan.camera.CameraManager;
import com.laifu.scan.decoding.DecodeThread;
import com.laifu.scan.decoding.InactivityTimer;
import com.laifu.scan.sound.SoundVibratingPlayer;
import com.laifu.scan.utils.CustomUtil;

/**
 * Created by private on 2017/2/8.
 */

public class ScanCodeView extends RelativeLayout implements SurfaceHolder.Callback {

    private static final String TAG = "ScanCodeView";

    public static final int SCAN_SINGLE = 0;               //只扫描一个
    public static final int SCAN_MULTIPLE = 1;             //扫描多个

    private SurfaceView surfaceView;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;

    private CaptureHandler handler;
    private SoundVibratingPlayer player;
    private InactivityTimer inactivityTimer;
    private DecodeThread decodeThread;
    private DecodeListener decodeListener;
    private Context mContext;

    private OnResumeCaptureViewListener resumeListener;   //显示完毕事件

    public interface DecodeListener {
        void handleDecode(Result result, Bitmap barcode);
    }

    public ScanCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_scancode_view, this);
        mContext = context;

        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        hasSurface = false;
        player = new SoundVibratingPlayer(context);
        inactivityTimer = new InactivityTimer((Activity) context);
        CameraManager.init(context.getApplicationContext());
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        hasSurface = true;
        startCamera(surfaceHolder);
        doResume();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    private void doResume() {
        if (resumeListener != null) {
            Rect rect = CameraManager.get().getFramingRectInPreview();
            resumeListener.OnResumeCaptureView(hasSurface, rect);
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void setDecodeListener(DecodeListener listener) {
        decodeListener = listener;
    }

    public void setOnResumeCaptureViewListener(OnResumeCaptureViewListener listener) {
        resumeListener = listener;
    }

    public void onResume() {
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            startCamera(surfaceHolder);
            doResume();
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    public void onPause() {
        stopCamera();
    }

    public void onDestroy() {
        inactivityTimer.shutdown();
    }

    public void restartCamera() {
        startCamera(surfaceView.getHolder());
    }

    private void startCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            CustomUtil.showErrorHint("无法打开相机，请检查是否授予摄像头权限");
            return;
        }

        if (handler == null) {
            handler = new CaptureHandler(this);
            decodeThread = new DecodeThread(handler, null, null, new ViewfinderResultPointCallback(viewfinderView));
            decodeThread.start();
        }

        Message quit = Message.obtain(handler, R.id.restart_preview);
        quit.sendToTarget();
        CameraManager.get().startPreview();
        CameraManager.get().requestPreviewFrame(decodeThread.getDecodeHandler(), R.id.decode);
        CameraManager.get().requestAutoFocus(handler, R.id.auto_focus);
        viewfinderView.drawViewfinder();

        doResume();
    }


    private void stopCamera() {

        CameraManager.get().stopPreview();
        CameraManager.get().closeDriver();

        if (decodeThread == null || handler == null) {
            return;
        }

        Message quit = Message.obtain(decodeThread.getDecodeHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }
        handler.removeMessages(R.id.decode_succeeded);
        handler.removeMessages(R.id.decode_failed);
        handler = null;
        decodeThread = null;
    }


    public void requestPreviewFrame() {
        CameraManager.get().requestPreviewFrame(decodeThread.getDecodeHandler(), R.id.decode);
    }


    public void requestAutoFocus() {
        CameraManager.get().requestAutoFocus(handler, R.id.auto_focus);
    }


    private void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        player.playBeepSoundAndVibrate();
        if (decodeListener != null) {
            decodeListener.handleDecode(result, barcode);
        }
    }


    public static final class CaptureHandler extends Handler {

        private ScanCodeView view;
        private State state;

        private enum State {
            PREVIEW,
            DONE
        }

        public CaptureHandler(ScanCodeView view) {
            this.view = view;
            state = State.PREVIEW;
        }

        @Override
        public void handleMessage(Message message) {
            int id = message.what;
            if (id == R.id.auto_focus) {
                if (state == State.PREVIEW) {
                    view.requestAutoFocus();
                }
            } else if (id == R.id.restart_preview) {
                state = State.PREVIEW;
            } else if (id == R.id.decode_succeeded) {
                state = State.DONE;
                Bundle bundle = message.getData();
                Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
                view.handleDecode((Result) message.obj, barcode);
            } else if (id == R.id.decode_failed) {
                state = State.PREVIEW;
                view.requestPreviewFrame();
            }

        }

    }

    public interface OnResumeCaptureViewListener {
        void OnResumeCaptureView(boolean hasSurface, Rect rect);
    }
}

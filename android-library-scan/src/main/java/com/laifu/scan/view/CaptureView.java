package com.laifu.scan.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.laifu.scan.R;
import com.laifu.scan.camera.CameraManager;
import com.laifu.scan.decoding.DecodeThread;
import com.laifu.scan.decoding.InactivityTimer;
import com.laifu.scan.sound.SoundVibratingPlayer;
import com.laifu.scan.utils.Constant;
import com.onehash.utils.FlashlightManager;
import com.onehash.utils.ToastHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by private on 2017/2/8.
 */

public class CaptureView extends RelativeLayout implements SurfaceHolder.Callback {

    private static final String TAG = "ScanCodeView";

    private ImageView ivBack;
    private SurfaceView surfaceView;
    private ViewfinderView viewfinderView;
    private View vLine;
    private LinearLayout llBottomEvents;

    private Context mContext;
    private CaptureHandler mCaptureHandler;
    private SoundVibratingPlayer mPlayer;
    private InactivityTimer mTimer;
    private DecodeThread mDecodeThread;

    private int mDecodeMode;
    private int mEventViewSize;
    private boolean hasSurface;

    private DecodeListener mDecodeListener;
    private OnResumeCaptureViewListener mResumeListener;   //显示完毕事件

    public CaptureView(Context context) {
        this(context, null);
    }

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_scancode_view, this);
        mContext = context;

        ivBack = (ImageView) findViewById(R.id.iv_back);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        vLine = findViewById(R.id.v_line);
        llBottomEvents = (LinearLayout) findViewById(R.id.ll_bottom_events);

        hasSurface = false;
        mDecodeMode = Constant.DECODE_QECODE;
        mPlayer = new SoundVibratingPlayer(context);
        mTimer = new InactivityTimer((Activity) context);
        CameraManager.init(context.getApplicationContext());

        mEventViewSize = llBottomEvents.getLayoutParams().height;

        addBackView();
    }

    private void addBackView() {
        CaptureEventViewParameter parameter = new CaptureEventViewParameter(
                CaptureViewConstant.BACK_CHECKED, CaptureViewConstant.BACK_UNCHECKED,
                CaptureViewConstant.BACK_PRESSED, new OnCaptureEventViewCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    doBack(mContext);
                }
            }
        });
        CaptureEventView eventView = new CaptureEventView(mContext, parameter);
        addView(eventView);
    }

    public void addFalshlightView() {
        CaptureEventViewParameter parameter = new CaptureEventViewParameter(
                CaptureViewConstant.FLASHLIGHT_CHECKED, CaptureViewConstant.FLASHLIGHT_UNCHECKED,
                CaptureViewConstant.FLASHLIGHT_PRESSED, new OnCaptureEventViewCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FlashlightManager.handlerFlashlight(CameraManager.get().getCamera(), isChecked);
            }
        });
        addEventView(parameter);
    }

    public void addSystemAlbumView() {
        CaptureEventViewParameter parameter = new CaptureEventViewParameter(
                CaptureViewConstant.SYSTEM_ALBUM_CHECKED, CaptureViewConstant.SYSTEM_ALBUM_UNCHECKED,
                CaptureViewConstant.SYSTEM_ALBUM_PRESSED, new OnCaptureEventViewCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        addEventView(parameter);
    }

    public void addOCRView() {
        CaptureEventViewParameter parameter = new CaptureEventViewParameter(
                CaptureViewConstant.OCR_CHECKED, CaptureViewConstant.OCR_UNCHECKED,
                CaptureViewConstant.OCR_PRESSED, new OnCaptureEventViewCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int mode = Constant.DECODE_OCR;
                if (!isChecked) {
                    mode = Constant.DECODE_QECODE;
                }
                setDecodeMode(mode);
                drawViewfinder();
            }
        });
        addEventView(parameter);
    }

    public void addEventView(int checkedResId, int unCheckedResId, int pressedResId, OnCaptureEventViewCheckedChangeListener listener) {
        CaptureEventViewParameter parameter = new CaptureEventViewParameter(checkedResId, unCheckedResId, pressedResId, listener);
        addEventView(parameter);
    }

    public void addEventView(CaptureEventViewParameter parameter) {
        CaptureEventView eventView = new CaptureEventView(mContext, parameter);
        llBottomEvents.addView(eventView, mEventViewSize, mEventViewSize);
        setBottomViewVisibilty(true);
    }

    private void setBottomViewVisibilty(boolean visibility) {
        if (visibility) {
            vLine.setVisibility(VISIBLE);
            llBottomEvents.setVisibility(VISIBLE);
        } else {
            vLine.setVisibility(GONE);
            llBottomEvents.setVisibility(GONE);
        }
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
        if (mResumeListener != null) {
            Rect rect = CameraManager.get().getFramingRectInPreview();
            mResumeListener.OnResumeCaptureView(hasSurface, rect);
        }
    }

    private void doBack(Context context) {
        Class c = context.getClass();
        try {
            Method method = c.getMethod("onBackPressed");
            method.invoke(context);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void setDecodeListener(DecodeListener listener) {
        mDecodeListener = listener;
    }

    public void setOnResumeCaptureViewListener(OnResumeCaptureViewListener listener) {
        mResumeListener = listener;
    }

    public void onResume() {
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            startCamera(surfaceHolder);
            doResume();
        } else {
            surfaceHolder.addCallback(this);
        }
    }

    public void onPause() {
        stopCamera();
    }

    public void onDestroy() {
        mTimer.shutdown();
    }

    public void restartCamera() {
        startCamera(surfaceView.getHolder());
    }

    private void startCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (Exception e) {
            showErrorHint("无法打开相机，请检查是否授予摄像头权限");
            return;
        }

        if (mCaptureHandler == null) {
            mCaptureHandler = new CaptureHandler(this);
            mDecodeThread = new DecodeThread(mCaptureHandler, new ViewfinderResultPointCallback(viewfinderView));
            mDecodeThread.start();
        }

        Message quit = Message.obtain(mCaptureHandler, R.id.restart_preview);
        quit.sendToTarget();
        CameraManager.get().startPreview();
        requestPreviewFrame();
        requestAutoFocus();
        viewfinderView.drawViewfinder();

        doResume();
    }

    private void stopCamera() {

        CameraManager.get().stopPreview();
        CameraManager.get().closeDriver();

        if (mDecodeThread == null || mCaptureHandler == null) {
            return;
        }

        Message quit = Message.obtain(mDecodeThread.getDecodeHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            mDecodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }
        mCaptureHandler.removeMessages(R.id.decode_succeeded);
        mCaptureHandler.removeMessages(R.id.decode_failed);
        mCaptureHandler = null;
        mDecodeThread = null;
    }


    public void requestPreviewFrame() {
        CameraManager.get().requestPreviewFrame(mDecodeThread.getDecodeHandler(), mDecodeMode);
    }


    public void requestAutoFocus() {
        CameraManager.get().requestAutoFocus(mCaptureHandler, R.id.auto_focus);
    }

    public void handleDecode(int decodeMode, String result, Bitmap barcode) {
        mTimer.onActivity();
        mPlayer.playBeepSoundAndVibrate();
        if (mDecodeListener != null) {
            mDecodeListener.handleDecode(decodeMode, result, barcode);
        }
    }

    private void showErrorHint(String hint) {
        ToastHelper.showLong(hint);
    }

    public void setDecodeMode(int decodeMode) {
        mDecodeMode = decodeMode;
        CameraManager.get().setDecodeMode(mDecodeMode);
    }

    public interface DecodeListener {
        void handleDecode(int decodeMode, String result, Bitmap barcode);
    }

    public interface OnResumeCaptureViewListener {
        void OnResumeCaptureView(boolean hasSurface, Rect rect);
    }

}

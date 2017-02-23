package com.laifu.scan.viewmanager;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.laifu.scan.view.CaptureView;

/**
 * Created by private on 2017/2/23.
 */

public class CaptureViewManager extends SimpleViewManager<CaptureView> implements LifecycleEventListener {

    private static final String REACT_CLASS = "RCTCaptureView";

    private CaptureView mCaptureView;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected CaptureView createViewInstance(ThemedReactContext reactContext) {
        reactContext.addLifecycleEventListener(this);
        if (mCaptureView == null) {
            mCaptureView = new CaptureView(reactContext);
        }
        return mCaptureView;
    }

    @Override
    public void onHostResume() {
        mCaptureView.onResume();
    }

    @Override
    public void onHostPause() {
        mCaptureView.onPause();
    }

    @Override
    public void onHostDestroy() {
        mCaptureView.onDestroy();
    }

}

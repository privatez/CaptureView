package com.laifu.onehash;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.Result;
import com.laifu.scan.camera.CameraManager;
import com.laifu.scan.utils.Constant;
import com.laifu.scan.utils.ImageDecodeHelper;
import com.laifu.scan.view.DisplayQrCodeView;
import com.laifu.scan.view.ScanCodeView;
import com.onehash.utils.LogHelper;
import com.onehash.utils.ToastHelper;

public class MainActivity extends Activity implements ScanCodeView.DecodeListener, View.OnClickListener {


    private RelativeLayout activityMain;
    private ScanCodeView sTest;
    private DisplayQrCodeView qrTest;
    private ImageView ivTest;

    private ImageDecodeHelper mImageDecodeHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityMain = (RelativeLayout) findViewById(R.id.activity_main);
        sTest = (ScanCodeView) findViewById(R.id.s_test);
        qrTest = (DisplayQrCodeView) findViewById(R.id.qr_test);
        ivTest = (ImageView) findViewById(R.id.iv_test);

        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);

        qrTest.setQrSize(450)
                .setQrText("hello!!!hello!!!hello!!!hello!!!hello!!!hello!!!hello!!!")
                .setLogoSize(100)
                .setLogoBmp(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .notifyDataChange();

        sTest.setDecodeListener(this);

        mImageDecodeHandler = new ImageDecodeHelper(this);
        mImageDecodeHandler.setOnDecodedListener(new ImageDecodeHelper.OnDecodeListener() {
            @Override
            public void onDecoding() {
                LogHelper.log("正在解析中...");
            }

            @Override
            public void onFailed() {
                LogHelper.log("失败");
            }

            @Override
            public void onSuccess(String qrCode, Bitmap qrBmp) {
                ToastHelper.showLong(qrCode);
            }

            @Override
            public void onTest(Bitmap bitmap) {
                qrTest.setImageBitmap(bitmap);
            }
        });

        qrTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageDecodeHandler.openSystemPhotoAlbum();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1:
                CameraManager.get().setDecodeMode(Constant.DECODE_QECODE);
                sTest.drawViewfinder();
                break;
            case R.id.btn_2:
                sTest.restartCamera();
                CameraManager.get().setDecodeMode(Constant.DECODE_OCR);
                sTest.drawViewfinder();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageDecodeHandler.onSystemPhotoAlbumActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sTest.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sTest.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sTest.onDestroy();
    }

    @Override
    public void handleDecode(Result result, Bitmap barcode) {
        LogHelper.log(result.getText());
        sTest.restartCamera();
        ivTest.setImageBitmap(barcode);
    }
}

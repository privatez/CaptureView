package com.laifu.scan.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.laifu.scan.utils.QrCodeUtil;


/**
 * Created by private on 2017/2/8.
 */

public class DisplayQrCodeView extends ImageView {

    private static final int QR_SIZE = 320;
    private static final int WHITE_PADDING = 1;

    private int mQrSize = QR_SIZE;
    private String mQrText;
    private Bitmap mQrBmp;

    private int mLogoSize;
    private Bitmap mLogoBmp;

    public DisplayQrCodeView(Context context) {
        super(context);
    }

    public DisplayQrCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisplayQrCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DisplayQrCodeView setQrSize(int size) {
        if (mQrSize != size) {
            mQrSize = size;
        }

        return this;
    }

    public DisplayQrCodeView setQrText(@Nullable String text) {
        if (mQrText != text) {
            mQrText = text;
        }

        return this;
    }

    public DisplayQrCodeView setLogoSize(int size) {
        if (mLogoSize != size) {
            mLogoSize = size;
        }

        return this;
    }

    public DisplayQrCodeView setLogoBmp(Bitmap bmp) {
        if (mLogoBmp != bmp) {
            mLogoBmp = bmp;
        }

        return this;
    }

    public void notifyDataChange() {
        Log.v("Update QR", String.valueOf(mQrSize) + " |  " + mQrText + " ---- " + this.hashCode());
        recycleBmp(mQrBmp);
        if (mQrSize > 0 && !TextUtils.isEmpty(mQrText)) {
            Bitmap qrTempBmp = QrCodeUtil.encodeBitmap(mQrText, mQrSize, Color.GRAY, Color.RED);
            final int qrWidth = qrTempBmp.getWidth();
            final int qrHeight = qrTempBmp.getHeight();
            Bitmap contentBmp = createEmptyBitmap(qrWidth, qrHeight);
            Canvas canvas = new Canvas(contentBmp);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(qrTempBmp, 0, 0, null);
            qrTempBmp.recycle();

            // draw logo
            if (mLogoSize > 0 && mLogoBmp != null) {
                final int logoOffset = (qrWidth - mLogoSize) / 2;
                final int whiteOffset = logoOffset - WHITE_PADDING;
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                canvas.drawRect(whiteOffset, whiteOffset, mQrSize - whiteOffset, mQrSize - whiteOffset, paint);
                Bitmap logoTempBmp = Bitmap.createScaledBitmap(mLogoBmp, mLogoSize, mLogoSize, false);
                canvas.drawBitmap(logoTempBmp, logoOffset, logoOffset, null);
                logoTempBmp.recycle();
            }

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            mQrBmp = contentBmp;
        } else {
            mQrBmp = createEmptyBitmap(mQrSize, mQrSize);
        }
        setImageBitmap(mQrBmp);
    }

    /**
     * 释放图片资源
     *
     * @param bitmap
     */
    private void recycleBmp(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private Bitmap createEmptyBitmap(int width, int height) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    public void destroy() {
        recycleBmp(mQrBmp);
        recycleBmp(mLogoBmp);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }
}

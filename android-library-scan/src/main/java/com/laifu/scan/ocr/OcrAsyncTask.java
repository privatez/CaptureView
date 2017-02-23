package com.laifu.scan.ocr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.laifu.scan.ocr.model.OcrImgParameter;
import com.laifu.scan.ocr.model.OcrResult;
import com.onehash.utils.LogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by private on 2017/2/13.
 */

public class OcrAsyncTask extends AsyncTask<Object, Integer, String> {

    private TessBaseAPI mTessBaseAPI;
    private OcrImgParameter mOcrImgParam;
    private OcrResultCallback mOcrResultCallback;

    public OcrAsyncTask(TessBaseAPI tessBaseAPI, OcrImgParameter param, OcrResultCallback callback) {
        mTessBaseAPI = tessBaseAPI;
        mOcrImgParam = param;
        mOcrResultCallback = callback;
    }

    @Override
    protected String doInBackground(Object... params) {
        int ocrMode = mOcrImgParam.mOcrMode;
        if (OcrImgParameter.OCR_BYTE == ocrMode) {
            // TODO
            //mTessBaseAPI.setImage(data, width, height, );
        } else if (OcrImgParameter.OCR_FILE == ocrMode) {
            mTessBaseAPI.setImage(new File(mOcrImgParam.mImgPath));
        } else if (OcrImgParameter.OCR_BITMAP == ocrMode) {
            /*Bitmap bitmap = mOcrImgParam.mImgBmp;
            int pix = bitmap.getByteCount() / (bitmap.getWidth() * bitmap.getHeight());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mTessBaseAPI.setImage(data, bitmap.getWidth(), bitmap.getHeight(), pix, bitmap.getRowBytes());*/
            mTessBaseAPI.setImage(mOcrImgParam.mImgBmp);
        }
        return mTessBaseAPI.getUTF8Text();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mOcrResultCallback != null) {
            if (TextUtils.isEmpty(result)) {
                mOcrResultCallback.onOcrFailed();
            } else {
                mOcrResultCallback.onOcrSuccess(new OcrResult(mOcrImgParam.mId, result, this));
            }
            // TODO
            saveCroppedImage(mOcrImgParam.mImgBmp);
        } else {
            LogHelper.log("onPostExecute: OcrResultCallback is null!");
        }
    }

    private void saveCroppedImage(Bitmap bmp) {
        String newFilePath = "/sdcard/download/temp_ocr.jpg";
        File file = new File(newFilePath);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            LogHelper.log("ocr", e.getMessage());
        }
    }

}

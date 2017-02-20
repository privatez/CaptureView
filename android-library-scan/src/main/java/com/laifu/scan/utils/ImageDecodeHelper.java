package com.laifu.scan.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.onehash.utils.FileUtils;
import com.onehash.utils.LogHelper;
import com.onehash.utils.ToastHelper;

import java.util.Hashtable;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;

/**
 * Created by private on 2017/2/8.
 */

public class ImageDecodeHelper {

    public static final String RESULT_BMP = "bitmap";
    public static final String RESULT_CODE = "code";

    private static final Vector<BarcodeFormat> PRODUCT_FORMATS;
    private static final Vector<BarcodeFormat> ONE_D_FORMATS;
    private static final Vector<BarcodeFormat> QR_CODE_FORMATS;
    private static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;

    static {
        PRODUCT_FORMATS = new Vector<>(5);
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
        // PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
        ONE_D_FORMATS = new Vector<>(PRODUCT_FORMATS.size() + 4);
        ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
        ONE_D_FORMATS.add(BarcodeFormat.ITF);
        QR_CODE_FORMATS = new Vector<>(1);
        QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
        DATA_MATRIX_FORMATS = new Vector<>(1);
        DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
    }

    private static final int REQ_IMAGE = 1;

    private static final int DECODE_SUCCESS = 200;
    private static final int DECODE_FAIL = 400;

    private static final String DECODE_FAIL_HINT = "解析二维码失败";

    private Activity mActivity;

    private String mChoosedImagePath;

    private Bitmap mChoosedImageBmp;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DECODE_SUCCESS:
                    onResultHandler((String) msg.obj, mChoosedImageBmp);
                    break;
                case DECODE_FAIL:
                    showDecodeFail();
                    break;
                case 1:
                    mOnDecodedListener.onTest(mChoosedImageBmp);
                    break;
            }
        }
    };

    private OnDecodeListener mOnDecodedListener;

    public ImageDecodeHelper(Activity activity) {
        mActivity = activity;
    }

    public void openSystemPhotoAlbum() {
        //打开手机中的相册
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
        mActivity.startActivityForResult(wrapperIntent, REQ_IMAGE);
    }

    public void onSystemPhotoAlbumActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQ_IMAGE) {
            //获取选中图片的路径
            mChoosedImagePath = FileUtils.getUriPath(mActivity.getApplicationContext(), data.getData());
            if (mOnDecodedListener != null) {
                mOnDecodedListener.onDecoding();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Result result = analyzeBitmap(mChoosedImagePath);
                    if (result != null) {
                        Message m = mHandler.obtainMessage();
                        m.what = DECODE_SUCCESS;
                        m.obj = result.getText();
                        mHandler.sendMessage(m);
                    } else {
                        Message m = mHandler.obtainMessage();
                        m.what = DECODE_FAIL;
                        mHandler.sendMessage(m);
                    }
                }
            }).start();
        }
    }

    /**
     * 扫描二维码图片
     *
     * @param path
     * @return
     */
    @Deprecated
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); //设置二维码内容的编码
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        mChoosedImageBmp = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        mChoosedImageBmp = BitmapFactory.decodeFile(path, options);
        LogHelper.log("size1:" + mChoosedImageBmp.getByteCount());
        mChoosedImageBmp = Bitmap.createScaledBitmap(mChoosedImageBmp, 200, 200, false);
        LogHelper.log("size2:" + mChoosedImageBmp.getByteCount());
        int width = mChoosedImageBmp.getWidth();
        int height = mChoosedImageBmp.getHeight();
        int[] pixels = new int[width * height];
        mChoosedImageBmp.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, new int[width * height]);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Message m = mHandler.obtainMessage();
            m.what = 1;
            mHandler.sendMessage(m);
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析二维码图片
     */
    public Result analyzeBitmap(final String path) {
        /**
         * 首先判断图片的大小,若图片过大,则执行图片的裁剪操作,防止OOM
         */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        mChoosedImageBmp = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小

        int sampleSize = (int) (options.outHeight / (float) 400);

        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        mChoosedImageBmp = BitmapFactory.decodeFile(path, options);

        MultiFormatReader multiFormatReader = new MultiFormatReader();

        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>(2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<>();

            // 这里设置可扫描的类型，我这里选择了都支持
            decodeFormats.addAll(ONE_D_FORMATS);
            decodeFormats.addAll(QR_CODE_FORMATS);
            decodeFormats.addAll(DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        // 设置继续的字符编码格式为UTF8
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        // 设置解析配置参数
        multiFormatReader.setHints(hints);

        // 开始对图像资源解码
        Result rawResult = null;
        int width = mChoosedImageBmp.getWidth();
        int height = mChoosedImageBmp.getHeight();
        int[] pixels = new int[width * height];
        mChoosedImageBmp.getPixels(pixels, 0, width, 0, 0, width, height);
        try {
            rawResult = multiFormatReader.decodeWithState(new BinaryBitmap(
                    new HybridBinarizer(new RGBLuminanceSource(width, height, pixels))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rawResult;
    }

    private void onResultHandler(String code, Bitmap bitmap) {
        if (TextUtils.isEmpty(code)) {
            showDecodeFail();
            return;
        }
        if (mOnDecodedListener != null) {
            mOnDecodedListener.onSuccess(code, bitmap);
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_BMP, bitmap);
        resultIntent.putExtra(RESULT_CODE, code);
        mActivity.setResult(RESULT_OK, resultIntent);
    }

    private void showDecodeFail() {
        if (mOnDecodedListener != null) {
            mOnDecodedListener.onFailed();
        } else {
            ToastHelper.showLong(DECODE_FAIL_HINT);
        }
    }

    public void setOnDecodedListener(OnDecodeListener onDecodedListener) {
        mOnDecodedListener = onDecodedListener;
    }

    public interface OnDecodeListener {
        void onDecoding();

        void onFailed();

        void onSuccess(String qrCode, Bitmap qrBmp);

        void onTest(Bitmap bitmap);
    }

}

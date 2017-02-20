package com.laifu.scan.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Version;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by private on 2017/2/8.
 */
public class QrCodeUtil {

    private final static QRCodeWriter QR_CODE_WRITER = new QRCodeWriter();

    private static final Integer QR_DEFAULT_COLOR = Color.BLACK;

    /**
     * 生成右下角定位带颜色
     *
     * @param content
     * @param size
     * @param contentColor
     * @param bottomRightColor
     * @return
     */
    public static Bitmap encodeBitmap(String content,
                                      int size,
                                      Integer contentColor,
                                      Integer bottomRightColor) {

        return encodeBitmap(content, size, 0, contentColor, null, null, null, bottomRightColor, true);
    }

    /**
     * 生成二维码
     *
     * @param content          内容
     * @param size             大小 / 正方形
     * @param margin           二维码 margin
     * @param contentColor     内容颜色
     * @param topLeftColor     左上角定位颜色
     * @param topRightColor    右上角定位颜色
     * @param bottomLeftColor  左下角定位颜色
     * @param bottomRightColor 右下角定位颜色
     * @param all              定位是否全部改变颜色 false: 中心改变、true:全部改变
     * @return
     */
    public static Bitmap encodeBitmap(String content,
                                      int size,
                                      int margin,
                                      Integer contentColor,
                                      Integer topLeftColor,
                                      Integer topRightColor,
                                      Integer bottomLeftColor,
                                      Integer bottomRightColor,
                                      boolean all) {

        // 每个定位点中心距离定位最外层 2 个模块
        final int posCenterOffset = 2;

        int bigPosStartModel = 2;
        int bigPosEndModel = 5;

        int smallPosStartModelMarginRight = 7;
        int smallPosEndModelMarginRight = 6;

        if (all) {
            bigPosStartModel -= posCenterOffset;
            bigPosEndModel += posCenterOffset;
            smallPosStartModelMarginRight += posCenterOffset;
            smallPosEndModelMarginRight -= posCenterOffset;
        }

        if (margin < 0) {
            margin = 0;
        }

        contentColor = checkColorNull(contentColor);
        topLeftColor = checkColorNull(topLeftColor);
        topRightColor = checkColorNull(topRightColor);
        bottomLeftColor = checkColorNull(bottomLeftColor);
        bottomRightColor = checkColorNull(bottomRightColor);

        Map<EncodeHintType, Object> hints = getEncodeSetting(margin);
        ErrorCorrectionLevel level = (ErrorCorrectionLevel) hints.get(EncodeHintType.ERROR_CORRECTION);
        QRCode code;
        try {
            code = Encoder.encode(content, level, hints);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

        Map<String, Object> result = renderResult(code, size, size, margin);

        BitMatrix matrix = (BitMatrix) result.get("BitMatrix");
        Version version = (Version) result.get("Version");

        final int[] tl = matrix.getTopLeftOnBit();
        final int totalModelNum = (version.getVersionNumber() - 1) * 4 + 21;    //获取单边模块数
        final int resultWidth = size - 2 * (tl[0]);
        final int modelWidth = resultWidth / totalModelNum;   //得到每个模块长度

        //得到四个基准点的起始与终点
        final int topStartX = tl[0] + modelWidth * bigPosStartModel;
        final int topEndX = tl[0] + modelWidth * bigPosEndModel;
        final int topStartY = tl[0] + modelWidth * bigPosStartModel;
        final int topEndY = tl[0] + modelWidth * bigPosEndModel;
        final int rightStartX = (totalModelNum - bigPosEndModel) * modelWidth + tl[0];
        final int rightEndX = size - modelWidth * bigPosStartModel - tl[0];
        final int leftStartY = size - modelWidth * bigPosEndModel - tl[1];
        final int leftEndY = size - modelWidth * bigPosStartModel - tl[1];

        final int smallPosStartX = (totalModelNum - smallPosStartModelMarginRight) * modelWidth + tl[0];
        final int smallPosEndX = (totalModelNum - smallPosEndModelMarginRight) * modelWidth + tl[0];

        int[] pixels = new int[size * size];

        Integer pixelColor;

        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (checkInArea(x, y, topStartX, topEndX, topStartY, topEndY)) {
                    //左上角
                    pixelColor = topLeftColor;
                } else if (checkInArea(x, y, rightStartX, rightEndX, topStartY, topEndY)) {
                    //右上角
                    pixelColor = topRightColor;
                } else if (checkInArea(x, y, topStartX, topEndX, leftStartY, leftEndY)) {
                    //左下角
                    pixelColor = bottomLeftColor;
                } else if (checkInArea(x, y, smallPosStartX, smallPosEndX, smallPosStartX, smallPosEndX)) {
                    //右下角
                    pixelColor = bottomRightColor;
                } else {
                    //其他
                    pixelColor = contentColor;
                }
                pixels[y * size + x] = matrix.get(x, y) ? pixelColor : Color.TRANSPARENT;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
        return bitmap;
    }

    private static Map<EncodeHintType, Object> getEncodeSetting(int margin) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, margin);

        return hints;
    }

    private static Integer checkColorNull(Integer color) {
        if (color == null) {
            return QR_DEFAULT_COLOR;
        } else {
            return color;
        }
    }

    private static boolean checkInArea(int x, int y, int startX, int endX, int startY, int endY) {

        return x >= startX && x < endX && y >= startY && y < endY;
    }

    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    private static Map<String, Object> renderResult(QRCode code, int width, int height, int quietZone) {
        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + (quietZone * 2);
        int qrHeight = inputHeight + (quietZone * 2);
        int outputWidth = Math.max(width, qrWidth);
        int outputHeight = Math.max(height, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
        // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

        BitMatrix output = new BitMatrix(outputWidth, outputHeight);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            // Write the contents of this row of the barcode
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                if (input.get(inputX, inputY) == 1) {
                    output.setRegion(outputX, outputY, multiple, multiple);
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("BitMatrix", output);
        map.put("Version", code.getVersion());
        return map;
    }

    public static String encodeCompressBinary(final byte[] bytes) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
            final GZIPOutputStream gos = new GZIPOutputStream(bos);
            gos.write(bytes);
            gos.close();

            final byte[] gzippedBytes = bos.toByteArray();
            final boolean useCompressioon = gzippedBytes.length < bytes.length;

            final StringBuilder str = new StringBuilder();
            str.append(useCompressioon ? 'Z' : '-');
            str.append(Base43.encode(useCompressioon ? gzippedBytes : bytes));

            return str.toString();
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
    }

    public static String encodeBinary(final byte[] bytes) {
        return Base43.encode(bytes);
    }

    public static byte[] decodeDecompressBinary(final String content) throws IOException {
        final boolean useCompression = content.charAt(0) == 'Z';
        final byte[] bytes = Base43.decode(content.substring(1));

        InputStream is = new ByteArrayInputStream(bytes);
        if (useCompression)
            is = new GZIPInputStream(is);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final byte[] buf = new byte[4096];
        int read;
        while (-1 != (read = is.read(buf)))
            baos.write(buf, 0, read);
        baos.close();
        is.close();

        return baos.toByteArray();
    }

    public static byte[] decodeBinary(final String content) throws IOException {
        return Base43.decode(content);
    }
}

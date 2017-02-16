package com.onehash.utils.luban;

import java.io.File;

/**
 * Created by private on 2017/2/9.
 */

public interface OnCompressListener {
    /**
     * Fired when the compression is started, override to handle in your own code
     */
    void onStart();

    /**
     * Fired when a compression returns successfully, override to handle in your own code
     */
    void onSuccess(File file);

    /**
     * Fired when a compression fails to complete, override to handle in your own code
     */
    void onError(Throwable e);
}

package com.onehash.utils.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by private on 2017/2/8.
 */

@TargetApi(Build.VERSION_CODES.M)
public class PermissionShadowActivity extends Activity {

    public static final String EXTRA_PERMISSIONS =  "permissions";

    private final static int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String[] permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS);
        requestPermissions(permissions, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.getInstance(this).onRequestPermissionsResult(permissions, grantResults);
        finish();
    }
}

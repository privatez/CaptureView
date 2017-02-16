package com.onehash.utils.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;

/**
 * Created by private on 2017/2/8.
 */

public class PermissionManager {

    //相机权限
    public static final String CAMERA = Manifest.permission.CAMERA;
    //电话权限
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    //录音权限
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    //定位权限
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    //存储权限
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static PermissionManager sSingleton;

    private Context mContext;
    private GrantedPermissionReceiver mReceiver;

    public static PermissionManager getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new PermissionManager(context.getApplicationContext());
        }
        return sSingleton;
    }

    private PermissionManager(Context context) {
        mContext = context;
    }


    /**
     * 请求各种权限
     *
     * @param permissions
     */
    public void requestPermissions(String... permissions) {

        //只针对Android 6.0的动态权限进行检查
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            handlePermissions(permissions);
            return;
        }

        boolean hasPermission = true;

        //检查是否已经拥有该权限，避免重复申请
        for (String name : permissions) {
            if (!isGranted(name)) {
                hasPermission = false;
                break;
            }
        }

        if (hasPermission) {
            handlePermissions(permissions);
        } else {
            requestPermissionsByActivity(permissions);
        }


    }

    /**
     * 接受权限请求结果, 给PermissionShadowActivity调用
     *
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(String permissions[], int[] grantResults) {

        ArrayList<String> permissionList = new ArrayList<>();

        if (permissions == null || grantResults == null) {
            handlePermissions(permissionList);
            return;
        }

        for (int i = 0, size = permissions.length; i < size; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }

        handlePermissions(permissionList);
    }


    /**
     * 已授权权限的接收器,
     * <p>
     * 权限若通过申请,则在onReceive中返回
     * 权限若被拒绝,则不在onReceive中返回
     */
    public interface GrantedPermissionReceiver {
        void onReceive(String... permissions);
    }


    /**
     * 设置权限监听
     *
     * @param receiver
     */
    public void setPermissionReceiver(GrantedPermissionReceiver receiver) {
        mReceiver = receiver;
    }


    /**
     * 处理permission申请结果
     *
     * @param permissions
     * @param <T>
     */
    private <T> void handlePermissions(T permissions) {
        String[] results = convertToStringArray(permissions);
        if (mReceiver != null) {
            mReceiver.onReceive(results);
        }
    }


    /**
     * 转换为String数组
     *
     * @param permissions
     * @param <T>
     * @return
     */
    public static <T> String[] convertToStringArray(T permissions) {

        String[] results = new String[]{};

        if (permissions == null) {
            return results;
        }

        if (permissions instanceof ArrayList<?>) {
            ArrayList<String> permissionList = (ArrayList<String>) permissions;
            results = permissionList.toArray(new String[permissionList.size()]);
        } else if (permissions instanceof String[]) {
            results = (String[]) permissions;
        }

        return results;

    }


    /**
     * 启动影子Activity申请权限
     *
     * @param permissions
     */
    private void requestPermissionsByActivity(String[] permissions) {
        Intent intent = new Intent(mContext, PermissionShadowActivity.class);
        intent.putExtra(PermissionShadowActivity.EXTRA_PERMISSIONS, permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    /**
     * 判断权限是否通过申请
     *
     * @param permission
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean isGranted(String permission) {
        return mContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}

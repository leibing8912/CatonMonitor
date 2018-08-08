package com.yy.mobile.catonmonitorsdk.log;

import android.util.Log;

/**
 * @className: CatonLogs
 * @classDescription: 卡顿日志管理
 * @author: leibing
 * @email: leibing@yy.com
 * @createTime:2018/8/8
 */
public class CatonLogs {
    // TAG
    private final static String TAG = "CatonLogs#";
    // 是否打开日志开关
    public static boolean isLogsSwitchOpen = false;

    /**
     * debug日志
     *
     * @param tag
     * @param msg
     */
    public static void debug(String tag, String msg) {
        if (isLogsSwitchOpen) {
            Log.v(TAG + tag, msg);
        }
    }

    /**
     * info日志
     *
     * @param tag
     * @param msg
     */
    public static void info(String tag, String msg) {
        Log.v(TAG + tag, msg);
    }
}

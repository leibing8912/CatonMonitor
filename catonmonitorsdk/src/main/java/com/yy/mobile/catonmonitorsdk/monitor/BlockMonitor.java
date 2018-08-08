package com.yy.mobile.catonmonitorsdk.monitor;

import android.os.Build;

import com.yy.mobile.catonmonitorsdk.choreographer.ChoreographerDetectByPrinter;
import com.yy.mobile.catonmonitorsdk.log.CatonLogs;
import com.yy.mobile.catonmonitorsdk.looper.LooperLogsDetectByPrinter;
import com.yy.mobile.catonmonitorsdk.upload.UploadCatonStack;

/**
 * @className: BlockMonitor
 * @classDescription: 卡顿监控
 * @author: leibing
 * @email: leibing@yy.com
 * @createTime:2018/7/25
 */
public class BlockMonitor {

    /**
     * 初始化
     */
    public static void init(String appId, String appName, String appVersion, boolean isDebug) {
        // 日志开关
        CatonLogs.isLogsSwitchOpen = isDebug;
        // 设置上传参数
        UploadCatonStack.getInstance().setUploadParam(appId, appName, appVersion);
        // 上传卡顿堆栈
        UploadCatonStack.getInstance().uploadCatonFile();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // 通过android系统每隔16ms发出VSYNC(帧同步)信号，触发对UI进行渲染回调方法，监视ui性能
            ChoreographerDetectByPrinter.start();
        } else {
            // 通过looper日志打印监视ui性能
            LooperLogsDetectByPrinter.start();
        }
    }
}

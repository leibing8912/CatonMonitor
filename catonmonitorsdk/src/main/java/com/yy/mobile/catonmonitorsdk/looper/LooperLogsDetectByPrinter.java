package com.yy.mobile.catonmonitorsdk.looper;

import android.os.Looper;
import android.util.Printer;

import com.yy.mobile.catonmonitorsdk.monitor.CatonStackCollect;

/**
 * @className: LooperLogsDetectByPrinter
 * @classDescription: 通过looper日志打印监视ui性能
 * @author: leibing
 * @createTime: 2017/3/1
 */
public class LooperLogsDetectByPrinter {
    // 主线程处理任务耗时80ms以上就当卡顿
    private final static long DEFAULT_DIFF_MS = 80L;

    /**
     * 开始监测ui线程
     */
    public static void start() {
        Looper.getMainLooper().setMessageLogging(new Printer() {
            // 起始时间
            private long startTimeMills = 0;
            // 结束时间
            private long endTimeMills = 0;
            // 是否打印开始
            private boolean isPrinterStart = false;

            @Override
            public void println(String s) {
                if (!isPrinterStart) {
                    if (CatonStackCollect.getInstance().isMonitor()) {
                        CatonStackCollect.getInstance().removeMonitor();
                    }
                    CatonStackCollect.getInstance().startMonitor();

                    isPrinterStart = true;
                    startTimeMills = System.currentTimeMillis();
                } else {
                    isPrinterStart = false;
                    endTimeMills = System.currentTimeMillis();
                    long diffMs = endTimeMills - startTimeMills;
                    // 发生卡顿
                    if (diffMs >= DEFAULT_DIFF_MS) {
                        // 卡顿堆栈存储到本地
                        CatonStackCollect.getInstance().setStackTraceToLocal(diffMs);
                    }

                    CatonStackCollect.getInstance().removeMonitor();
                }
            }
        });
    }
}

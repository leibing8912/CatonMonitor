package com.yy.mobile.catonmonitorsdk.choreographer;

import android.os.Build;
import android.view.Choreographer;

import com.yy.mobile.catonmonitorsdk.monitor.CatonStackCollect;

import java.util.concurrent.TimeUnit;

/**
 * @className: ChoreographerDetectByPrinter
 * @classDescription: 通过android系统每隔16ms发出VSYNC(帧同步)信号，触发对UI进行渲染回调方法，监视ui性能
 * @author: leibing
 * @createTime: 2017/3/1
 */
public class ChoreographerDetectByPrinter {

    /**
     * 开始监测ui线程
     */
    public static void start() {
        // 要求minSdkVersion > 15
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
                long lastFrameTimeNanos = 0;
                long curFrameTimeNanos = 0;
                long startTimeMills = 0;

                @Override
                public void doFrame(long frameTimeNanos) {
                    if (lastFrameTimeNanos == 0) {
                        lastFrameTimeNanos = frameTimeNanos;
                        startTimeMills = System.currentTimeMillis();
                    }
                    curFrameTimeNanos = frameTimeNanos;
                    long diffMs = TimeUnit.MILLISECONDS
                            .convert(curFrameTimeNanos - lastFrameTimeNanos,
                                    TimeUnit.NANOSECONDS);
                    long droppedCount = 0;
                    if (diffMs > 16.6f) {
                        droppedCount = (int) (diffMs / 16.6);
                    }
                    // 发生卡顿
                    if (droppedCount >= 5) {
                        // 卡顿堆栈存储到本地
                        CatonStackCollect.getInstance().setStackTraceToLocal(diffMs);
                    }

                    if (CatonStackCollect.getInstance().isMonitor()) {
                        CatonStackCollect.getInstance().removeMonitor();
                    }
                    CatonStackCollect.getInstance().startMonitor();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Choreographer.getInstance().postFrameCallback(this);
                    }
                    lastFrameTimeNanos = curFrameTimeNanos;
                    startTimeMills = System.currentTimeMillis();
                }
            });
        }
    }
}

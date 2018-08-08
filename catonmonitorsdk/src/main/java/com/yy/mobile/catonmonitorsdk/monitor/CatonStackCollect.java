package com.yy.mobile.catonmonitorsdk.monitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.yy.mobile.catonmonitorsdk.file.CatonFileUtils;
import com.yy.mobile.catonmonitorsdk.log.CatonLogs;
import com.yy.mobile.catonmonitorsdk.upload.UploadCatonStack;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @className: CatonStackCollect
 * @classDescription: 卡顿收集，用于监测ui性能
 * @author: leibing
 * @email: leibing@yy.com
 * @createTime:2018/7/25
 */
public class CatonStackCollect {
    // 日志标识
    private final static String TAG = "CatonStackCollect";
    // 单例实例
    private volatile static CatonStackCollect instance;
    // 检测ui性能间隔时间
    private static final long DETECT_PERFORMANCE_TIME = 53L;
    // 卡顿文件最大阀值(900kb)
    public final static int CATON_FILE_MAX = 900;
    // 卡顿堆栈存储带looper线程
    private HandlerThread mStoreCatonStackThread;
    // 卡顿堆栈存储handler
    private Handler mStoreCatonStackHandler;
    // 卡顿堆栈采集带looper线程
    private HandlerThread mCollectCatonStackThread;
    // 卡顿堆栈采集handler
    private Handler mCollectCatonStackHandler;
    // 堆栈列表
    private List<String> mStackTraceList;
    // 卡顿耗时
    private long catonDiffMs = 0;
    // 是否正在上传文件中
    private boolean isUpdateFiling = false;
    // 是否监视中
    private boolean isMonitoring = false;
    // 卡顿堆栈存储Runnable
    private Runnable mStoreCatonStackRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStackTraceList == null) {
                return;
            }
            // 重复最多的堆栈索引
            int repeatAtMostIndex = 0;
            // 重复次数
            int maxRepeatCount = 0;
            int size = mStackTraceList.size();
            for (int i = 0; i < size; i++) {
                int repeatCount = Collections.frequency(mStackTraceList, mStackTraceList.get(i));
                if (repeatCount > maxRepeatCount) {
                    maxRepeatCount = repeatCount;
                    repeatAtMostIndex = i;
                }
            }
            if (mStackTraceList.size() != 0 && repeatAtMostIndex < mStackTraceList.size()) {
                CatonLogs.debug(TAG, "#maxRepeatCount = " + maxRepeatCount
                        + " size = " + size + " catonDiffMs = " + catonDiffMs);
                if (maxRepeatCount == 1) {
                    repeatAtMostIndex = (int) (Math.random() * mStackTraceList.size());
                }
                String repeatAtMostStackTrace = mStackTraceList.get(repeatAtMostIndex);
                long actualCostTime = (maxRepeatCount * catonDiffMs) / size;
                // 往本地文件写数据
                writeDataToLocalFile(repeatAtMostStackTrace, actualCostTime);
            }
            // 清空列表
            mStackTraceList.clear();
        }
    };
    // 卡顿堆栈采集Runnable
    private Runnable mCollectCatonStackRunnable = new Runnable() {
        @Override
        public void run() {
            if (isMonitoring) {
                StringBuilder sb = new StringBuilder();
                StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
                for (StackTraceElement s : stackTrace) {
                    sb.append(s.toString() + "\n");
                }

                if (mStackTraceList == null) {
                    mStackTraceList = new CopyOnWriteArrayList();
                }

                mStackTraceList.add(sb.toString());

                mCollectCatonStackHandler.postDelayed(mCollectCatonStackRunnable,
                        DETECT_PERFORMANCE_TIME);
            }
        }
    };

    /**
     * 实例化
     */
    private CatonStackCollect() {
        mStoreCatonStackThread = new HandlerThread("StoreCatonStackThread");
        mCollectCatonStackThread = new HandlerThread("CollectCatonStackThread");
        mStoreCatonStackThread.start();
        mCollectCatonStackThread.start();
        mStoreCatonStackHandler = new Handler(mStoreCatonStackThread.getLooper());
        mCollectCatonStackHandler = new Handler(mCollectCatonStackThread.getLooper());
        mStackTraceList = new CopyOnWriteArrayList<>();
    }

    /**
     * 获取单例实例
     */
    public static CatonStackCollect getInstance() {
        if (instance == null) {
            synchronized (CatonStackCollect.class) {
                instance = new CatonStackCollect();
            }
        }
        return instance;
    }

    /**
     * 设置是否正在上传状态
     */
    public void setIsUpdateFiling(boolean isUpdateFiling) {
        this.isUpdateFiling = isUpdateFiling;
    }

    /**
     * 是否正在上传状态
     *
     * @return
     */
    public boolean isUpdateFiling() {
        return isUpdateFiling;
    }

    /**
     * 记录堆栈日志到本地
     */
    public void setStackTraceToLocal(long diffMs) {
        if (mStoreCatonStackHandler != null && mStoreCatonStackRunnable != null) {
            catonDiffMs = diffMs;
            mStoreCatonStackHandler.post(mStoreCatonStackRunnable);
        }
    }

    /**
     * 往本地文件写数据
     */
    private void writeDataToLocalFile(String repeatAtMostStackTrace, long actualCostTime) {
        CatonLogs.debug(TAG, "#actualCostTime = " + actualCostTime + "ms"
                + "\nisUpdateFiling = " + isUpdateFiling
                + "\nrepeatAtMostStackTrace = " + repeatAtMostStackTrace);
        double catonFileSize = CatonFileUtils.getFileOrFilesSize(CatonFileUtils.CATON_FILE_PATH);
        CatonLogs.debug(TAG, "#writeDataToLocalFile catonFileSize = " + catonFileSize + " kb");
        boolean isOverMaxFileSize = catonFileSize > CATON_FILE_MAX ? true : false;
        if (!isUpdateFiling && !isOverMaxFileSize) {
            // 拼接写入数据
            StringBuilder writeSb = new StringBuilder();
            writeSb.append("###block start");
            writeSb.append("\n");
            writeSb.append("###costtime ");
            writeSb.append(actualCostTime);
            writeSb.append("\n");
            writeSb.append("###date ");
            writeSb.append(System.currentTimeMillis());
            writeSb.append("\n");
            writeSb.append(repeatAtMostStackTrace);
            writeSb.append("###block end");
            CatonFileUtils.writeTxtToFile(writeSb.toString(),
                    CatonFileUtils.CATON_FILE_PATH);
        } else {
            UploadCatonStack.getInstance().uploadCatonFile();
        }
    }

    /**
     * 是否监视中
     *
     * @return
     */
    public boolean isMonitor() {
        return isMonitoring;
    }

    /**
     * 开启监视器
     */
    public void startMonitor() {
        if (mCollectCatonStackHandler != null && mCollectCatonStackRunnable != null) {
            isMonitoring = true;
            mCollectCatonStackHandler.postDelayed(mCollectCatonStackRunnable, DETECT_PERFORMANCE_TIME);
        }
    }

    /**
     * 移除监视器
     */
    public void removeMonitor() {
        if (mCollectCatonStackHandler != null && mCollectCatonStackRunnable != null) {
            isMonitoring = false;
            mCollectCatonStackHandler.removeCallbacks(mCollectCatonStackRunnable);
        }
    }
}

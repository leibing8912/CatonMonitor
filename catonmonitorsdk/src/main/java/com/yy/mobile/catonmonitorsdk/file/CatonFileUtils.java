package com.yy.mobile.catonmonitorsdk.file;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.zip.GZIPOutputStream;

/**
 * @className: CatonFileUtils
 * @classDescription: 卡顿堆栈写入本地文件工具
 * @author: leibing
 * @email: leibing@yy.com
 * @createTime:2018/8/2
 */
public class CatonFileUtils {
    // TAG
    private final static String TAG = "CatonFileUtils";
    // 目录
    public final static String DIRECTORY_PATH = "/sdcard/caton/";
    // 卡顿文件名称
    private final static String CATON_FILE_NAME = "catonmonitor.txt";
    // 卡顿压缩文件名称
    private final static String CATON_COMPRESS_FILE_NAME = "catonmonitor.zip";
    // 卡顿文件路径
    public final static String CATON_FILE_PATH = DIRECTORY_PATH + CATON_FILE_NAME;
    // 卡顿压缩文件路径
    public final static String CATON_COMPRESS_FILE_PATH = DIRECTORY_PATH + CATON_COMPRESS_FILE_NAME;
    // buffer
    public static final int BUFFER = 1024;

    /**
     * 将字符串写入到文本文件中
     *
     * @param contentStr
     * @param filePath
     */
    public static void writeTxtToFile(String contentStr, String filePath) {
        // 生成文件夹之后，再生成文件，不然会出错
        makeFilePath(DIRECTORY_PATH, filePath);

        // 每次写入时，都换行写
        String strContent = contentStr + "\r\n";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.v(TAG, "#writeTxtToFile e = " + e);
        }
    }

    /**
     * 生成文件
     *
     * @param directoryPath 目录
     * @param filePath      文件路径
     * @return
     */
    private static File makeFilePath(String directoryPath, String filePath) {
        File file = null;
        makeRootDirectory(directoryPath);
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            Log.v(TAG, "#makeFilePath e = " + e);
        }
        return file;
    }

    /**
     * 生成文件夹
     *
     * @param directoryPath 目录
     */
    private static void makeRootDirectory(String directoryPath) {
        File file;
        try {
            file = new File(directoryPath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.v(TAG, "#makeRootDirectory e = " + e);
        }
    }

    /**
     * 是否文件存在
     *
     * @param filePath
     */
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    public static void deleteSingleFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    /**
     * 转换文件大小（kb为单位）
     *
     * @param fileS
     * @return
     */
    private static double formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.valueOf(df.format((double) fileS / 1024));
    }

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath) {
        long blockSize = 0;
        File file = new File(filePath);
        if (!file.exists()) {
            return blockSize;
        }
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            Log.v(TAG, "#getFileOrFilesSize e = " + e);
        }
        return formetFileSize(blockSize);
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
        }
        return size;
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 数据压缩
     *
     * @param is
     * @param os
     * @throws Exception
     */
    public static void compress(InputStream is, OutputStream os)
            throws Exception {
        GZIPOutputStream gos = new GZIPOutputStream(os);
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = is.read(data, 0, BUFFER)) != -1) {
            gos.write(data, 0, count);
        }
        gos.finish();
        gos.flush();
        gos.close();
    }

    /**
     * 文件压缩
     *
     * @param originPath
     * @param targetPath
     * @throws Exception
     */
    public static void compressFile(String originPath, String targetPath) throws Exception {
        FileInputStream fis = new FileInputStream(originPath);
        FileOutputStream fos = new FileOutputStream(targetPath);
        compress(fis, fos);
        fis.close();
        fos.flush();
        fos.close();
    }
}

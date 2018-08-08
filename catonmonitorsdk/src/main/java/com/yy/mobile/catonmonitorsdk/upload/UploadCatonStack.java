package com.yy.mobile.catonmonitorsdk.upload;

import com.yy.mobile.catonmonitorsdk.file.CatonFileUtils;
import com.yy.mobile.catonmonitorsdk.log.CatonLogs;
import com.yy.mobile.catonmonitorsdk.monitor.CatonStackCollect;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @className: UploadCatonStack
 * @classDescription: 上传卡顿堆栈到服务器
 * @author: leibing
 * @email: leibing@yy.com
 * @createTime:2018/8/2
 */
public class UploadCatonStack {
    // TAG
    private final static String TAG = "UploadCatonStack";
    // 单例
    private static UploadCatonStack instance;
    // 堆栈上传地址
    public static final String UPLOAD_URL = "";
    // appId
    private String appId = "appId";
    // appName
    private String appName = "appName";
    // appVersion
    private String appVersion = "appVersion";
    // 待以后扩展json，先传""
    private String data = "";
    // OkHttpClient
    private OkHttpClient mOkHttpClient;

    private UploadCatonStack() {
        mOkHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 设置上传参数
     *
     * @param appId
     * @param appName
     * @param appVersion
     * @param data
     */
    public void setUploadParam(String appId, String appName, String appVersion, String data) {
        setUploadParam(appId, appName, appVersion, "");
    }

    /**
     * 设置上传参数
     *
     * @param appId
     * @param appName
     * @param appVersion
     */
    public void setUploadParam(String appId, String appName, String appVersion) {
        this.appId = appId;
        this.appName = appName;
        this.appVersion = appVersion;
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static UploadCatonStack getInstance() {
        if (null == instance) {
            synchronized (UploadCatonStack.class) {
                if (null == instance) {
                    instance = new UploadCatonStack();
                }
            }
        }

        return instance;
    }

    /**
     * 通过上传的文件的完整路径生成RequestBody
     *
     * @param filePath 完整的文件路径
     * @return
     */
    private RequestBody getRequestBody(String filePath) {
        // 创建MultipartBody.Builder，用于添加请求的数据
        MultipartBody.Builder builder = new MultipartBody.Builder();
        File file = new File(filePath); // 生成文件
        // 根据文件的后缀名，获得文件类型
        builder.addFormDataPart( // 给Builder添加上传的文件
                "files",  // 请求的名字
                file.getName(), // 文件的文字，服务器端用来解析的
                RequestBody.create(MediaType.parse("multipart/form-data"), file) // 创建RequestBody，把上传的文件放入
        );
        // 添加参数
        builder.addFormDataPart("appId", appId);
        builder.addFormDataPart("appName", appName);
        builder.addFormDataPart("appVersion", appVersion);
        builder.addFormDataPart("data", data);

        return builder.build(); // 根据Builder创建请求
    }

    /**
     * 获得Request实例
     *
     * @param url
     * @param filePath 完整的文件路径
     * @return
     */
    private Request getRequest(String url, String filePath) {
        Request.Builder builder = new Request.Builder();
        builder.url(url)
                .post(getRequestBody(filePath));
        return builder.build();
    }

    /**
     * 根据url，发送异步Post请求
     *
     * @param uploadUrl 提交到服务器的地址
     * @param filePath  完整的上传的文件的路径名
     * @param callback  OkHttp的回调接口
     */
    public void upLoadFile(String uploadUrl, String filePath, Callback callback) {
        if (mOkHttpClient != null) {
            CatonStackCollect.getInstance().setIsUpdateFiling(true);
            Call call = mOkHttpClient.newCall(getRequest(uploadUrl, filePath));
            call.enqueue(callback);
        }
    }

    /**
     * 上传卡顿堆栈文件
     */
    public synchronized void uploadCatonFile() {
        double catonFileSize = CatonFileUtils.getFileOrFilesSize(CatonFileUtils.CATON_FILE_PATH);
        CatonLogs.debug(TAG, "#uploadCatonFile catonFileSize = " + catonFileSize + " kb");
        // 判断是否达到上传文件最大阀值
        boolean isOverMaxFileSize = catonFileSize > CatonStackCollect.CATON_FILE_MAX ? true : false;
        if (!CatonStackCollect.getInstance().isUpdateFiling()
                && isOverMaxFileSize) {
            CatonStackCollect.getInstance().setIsUpdateFiling(true);
            try {
                // 压缩卡顿堆栈文件，用于上传
                CatonFileUtils.compressFile(
                        CatonFileUtils.CATON_FILE_PATH,
                        CatonFileUtils.CATON_COMPRESS_FILE_PATH);
            } catch (Exception e) {
                CatonLogs.debug(TAG, "#writeDataToLocalFile compressFile e = " + e);
            }
            CatonLogs.debug(TAG, "#upload caton file......");
            // 上传
            UploadCatonStack.getInstance().upLoadFile(UPLOAD_URL,
                    CatonFileUtils.CATON_COMPRESS_FILE_PATH, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            CatonLogs.debug(TAG, "#upLoadFile onFailure e = " + e);
                            CatonStackCollect.getInstance().setIsUpdateFiling(false);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            CatonLogs.debug(TAG, "#upLoadFile onSuccess");
                            // 删除卡顿堆栈文件
                            CatonFileUtils.deleteSingleFile(CatonFileUtils.CATON_FILE_PATH);
                            CatonFileUtils.deleteSingleFile(CatonFileUtils.CATON_COMPRESS_FILE_PATH);
                            CatonStackCollect.getInstance().setIsUpdateFiling(false);
                        }
                    });
        }
    }
}

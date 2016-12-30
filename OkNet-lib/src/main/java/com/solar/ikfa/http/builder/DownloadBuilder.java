package com.solar.ikfa.http.builder;

import android.support.annotation.NonNull;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.callback.DownloadCallback;
import com.solar.ikfa.http.exception.NotFoundException;
import com.solar.ikfa.http.interceptor.ProgressInterceptor;
import com.solar.ikfa.http.response.DownloadResponseHandler;

import java.io.File;
import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author wujunjie
 * @date 2016/12/28
 */

public class DownloadBuilder {

    protected String url;
    protected Object tag;
    protected Headers.Builder headers;

    protected OkNet okNet;
    protected OkHttpClient.Builder innerBuilder;

    protected int connectTimeout = 10;
    protected int readTimeout = 30;
    protected int writeTimeout = 20;

    private String mFileDir = "";        //文件dir
    private String mFileName = "";       //文件名
    private String mFilePath = "";       //文件路径 （如果设置该字段则上面2个就不需要）
    private long mCompleteBytes = 0L;    //已经完成的字节数 用于断点续传

    public DownloadBuilder url(String url) {
        this.url = url;
        return this;
    }

    public DownloadBuilder header(String key, String value) {
        if (headers == null) {
            headers = new Headers.Builder();
        }
        headers.set(key, value);
        return this;
    }

    public DownloadBuilder addHeader(String key, String value) {
        if (headers == null) {
            headers = new Headers.Builder();
        }
        headers.add(key, value);
        return this;
    }

    public DownloadBuilder cacheControl(CacheControl cacheControl) {
        String value = cacheControl.toString();
        return addHeader("Cache-Control", value);
    }

    public DownloadBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public DownloadBuilder connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    /**
     * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public DownloadBuilder readTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    /**
     * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public DownloadBuilder writeTimeout(int timeout) {
        this.writeTimeout = timeout;
        return this;
    }

    public DownloadBuilder(OkNet okNet) {
        this.okNet = okNet;
    }

    /**
     * set file storage dir
     *
     * @param fileDir file directory
     * @return
     */
    public DownloadBuilder fileDir(@NonNull String fileDir) {
        this.mFileDir = fileDir;
        return this;
    }

    /**
     * set file storage name
     *
     * @param fileName file name
     * @return
     */
    public DownloadBuilder fileName(@NonNull String fileName) {
        this.mFileName = fileName;
        return this;
    }

    /**
     * set file path
     *
     * @param filePath file path
     * @return
     */
    public DownloadBuilder filePath(@NonNull String filePath) {
        this.mFilePath = filePath;
        return this;
    }

    public DownloadBuilder completeBytes(long completeBytes) {
        if (completeBytes > 0L) {
            this.mCompleteBytes = completeBytes;
            addHeader("RANGE", "bytes=" + completeBytes + "-");     //添加断点续传header
        }
        return this;
    }

    public void enqueue(final DownloadResponseHandler responseHandler) {
        try {
            if (url == null || url.length() == 0) {
                throw new IllegalArgumentException("Url can not be null !");
            }

            if (mFilePath.length() == 0) {
                if (mFileDir.length() == 0 || mFileName.length() == 0) {
                    throw new IllegalArgumentException("FilePath can not be null !");
                } else {
                    mFilePath = mFileDir + mFileName;
                }
            }
            checkFilePath(mFilePath, mCompleteBytes);

            Request.Builder builder = new Request.Builder();
            builder.url(url);
            if (headers != null) {
                builder.headers(headers.build());
            }
            if (tag != null) {
                builder.tag(tag);
            }
            Request request = builder.build();
            innerBuilder = okNet.client().newBuilder();
            innerBuilder.addNetworkInterceptor(new ProgressInterceptor(responseHandler, mCompleteBytes));

            responseHandler.onStart();
            innerBuilder.build().newCall(request).enqueue(new DownloadCallback(responseHandler, mFilePath, mCompleteBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查下载路径有效性
     *
     * @param filePath
     * @param completeBytes
     * @throws Exception
     */
    private void checkFilePath(String filePath, Long completeBytes) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            return;
        }

        if (completeBytes > 0L) {       //如果设置了断点续传 则必须文件存在
            throw new NotFoundException("断点续传文件" + filePath + "不存在！");
        }

        if (filePath.endsWith(File.separator)) {
            throw new IOException("创建文件" + filePath + "失败，目标文件不能为目录！");
        }

        //判断目标文件所在的目录是否存在
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("创建目标文件所在目录失败！");
            }
        }
    }
}

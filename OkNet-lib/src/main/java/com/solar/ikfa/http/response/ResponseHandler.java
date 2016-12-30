package com.solar.ikfa.http.response;

import okhttp3.Request;

/**
 * @author wujunjie
 * @date 2016/12/28
 */

public abstract class ResponseHandler {

    /**
     * 网络请求开始
     */
    public abstract void onStart();

    /**
     * 网络请求结束,包括请求成功,失败,异常完成
     *
     * @param request
     */
    public abstract void onFinish(Request request);

    public abstract void onFailure(Request request, int code, Exception e);

    public abstract void onResponse(Request request, String result);

    public abstract void onProgress(String param, long bytesRead, long contentLength, boolean done);
}

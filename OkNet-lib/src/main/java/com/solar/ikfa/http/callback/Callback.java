/**
 * Copyright (C) 2016 priscilla
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.solar.ikfa.http.callback;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.solar.ikfa.http.exception.CancelException;
import com.solar.ikfa.http.exception.NotFoundException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public abstract class Callback implements okhttp3.Callback {

    protected static final int SUCCESS_MESSAGE = 0;
    protected static final int FAILURE_MESSAGE = 1;
    protected static final int START_MESSAGE = 2;
    protected static final int FINISH_MESSAGE = 3;
    protected static final int PROGRESS_MESSAGE = 4;
    protected static final int RETRY_MESSAGE = 5;
    protected static final int CANCEL_MESSAGE = 6;

    private Handler handler;

    /**
     * 网络请求开始
     */
    public void onStart() {

    }

    /**
     * 网络请求结束,包括请求成功,失败,异常完成
     * @param request
     */
    public void onFinish(Request request) {

    }

    public abstract void onError(Exception e, Request request, int code);

    public void onProgress(String params, long bytesRead, long contentLength, boolean done) {
    }

    protected void onFailure(Exception e, Request request, int code) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, request, code}));
        sendMessage(obtainMessage(FINISH_MESSAGE, new Object[]{request}));
    }

    public void upload(String fileName, long bytesRead, long contentLength, boolean done) {
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[]{fileName, bytesRead, contentLength, done}));
    }

    public void download(String url, long bytesRead, long contentLength, boolean done) {
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[]{url, bytesRead, contentLength, done}));
    }

    protected void sendMessage(Message msg) {
        if (handler == null) {
            handler = new MainHandler();
        }
        if (!Thread.currentThread().isInterrupted()) {
            handler.sendMessage(msg);
        }
    }

    protected void handleMessage(Message msg) {
        Object[] objects = null;
        switch (msg.what) {
            case START_MESSAGE:

                break;
            case FINISH_MESSAGE:
                objects = (Object[]) msg.obj;
                onFinish((Request) objects[0]);
                break;
            case FAILURE_MESSAGE:
                objects = (Object[]) msg.obj;
                onError((Exception) objects[0], (Request) objects[1], (int) objects[2]);
                break;
            case PROGRESS_MESSAGE:
                objects = (Object[]) msg.obj;
                onProgress((String) objects[0], (long) objects[1], (long) objects[2], (boolean) objects[3]);
                break;
        }
    }

    /**
     * Helper method to create Message instance from handler
     *
     * @param responseMessageId   constant to identify Handler message
     * @param responseMessageData object to be passed to message receiver
     * @return Message instance, should not be null
     */
    protected Message obtainMessage(int responseMessageId, Object responseMessageData) {
        return Message.obtain(handler, responseMessageId, responseMessageData);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        onFailure(e, call.request(), -200);
    }

    /**
     * @param call
     * @param response
     * 异常说明:  exception BadRequestException
     *           用于处理 400 错误的请求(Bad Request)错误。
     *
     *           exception UnauthorizedException
     *           用于处理 401 未授权(Unauthorized)错误。
     *
     *           exception ForbiddenException
     *           用于处理 403 禁止访问(Forbidden)错误。
     *
     *           exception NotFoundException
     *           用于处理 404 未找到(Not found)错误。
     *
     *           exception MethodNotAllowedException
     *           用于处理 405 方法不被允许(Method Not Allowed)错误。
     *
     *           exception InternalErrorException
     *           用于处理 500 内部服务器错误(Internal Server Error)。
     *
     *           exception NotImplementedException
     *           用于处理 501 未实现(Not Implemented)错误。
     */
    @Override
    public void onResponse(Call call, Response response) {
        try {
            if (call.isCanceled()) {
                onFailure(new CancelException("Request were canceled."), call.request(), -200);
                return;
            }
            if (!response.isSuccessful()) {
                onFailure(new NotFoundException(response.code() + "error,server is not found."), response.request(), response.code());
                return;
            }
            //完成请求
            sendMessage(obtainMessage(FINISH_MESSAGE, new Object[]{response.request()}));
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, call.request(), response.code()}));
        }
    }

    /**
     * Avoid leaks by using a non-anonymous handler class.
     */
    private class MainHandler extends Handler {

        MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            com.solar.ikfa.http.callback.Callback.this.handleMessage(msg);
        }
    }


}

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

import com.solar.ikfa.http.exception.NotFoundException;
import com.solar.ikfa.http.response.ResponseHandler;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Callback implements okhttp3.Callback {

    private ResponseHandler mResponseHandler;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public Callback(ResponseHandler responseHandler) {
        this.mResponseHandler = responseHandler;
    }

    public void onProgress(final String params, final long bytesRead, final long contentLength, final boolean done) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mResponseHandler.onProgress(params, bytesRead, contentLength, done);
            }
        });
    }

    @Override
    public void onFailure(final Call call, final IOException e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mResponseHandler.onFailure(call.request(), -1, e);
                mResponseHandler.onFinish(call.request());
            }
        });
    }

    /**
     * @param call
     * @param response 异常说明:  exception BadRequestException
     *                 用于处理 400 错误的请求(Bad Request)错误。
     *                 <p>
     *                 exception UnauthorizedException
     *                 用于处理 401 未授权(Unauthorized)错误。
     *                 <p>
     *                 exception ForbiddenException
     *                 用于处理 403 禁止访问(Forbidden)错误。
     *                 <p>
     *                 exception NotFoundException
     *                 用于处理 404 未找到(Not found)错误。
     *                 <p>
     *                 exception MethodNotAllowedException
     *                 用于处理 405 方法不被允许(Method Not Allowed)错误。
     *                 <p>
     *                 exception InternalErrorException
     *                 用于处理 500 内部服务器错误(Internal Server Error)。
     *                 <p>
     *                 exception NotImplementedException
     *                 用于处理 501 未实现(Not Implemented)错误。
     */
    @Override
    public void onResponse(final Call call, final Response response) {
        final ResponseBody body = response.body();
        final Request request = response.request();
        final int code = response.code();
        try {
            if (response.isSuccessful()) {
                final String result = body.string();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mResponseHandler.onResponse(request, result);
                        mResponseHandler.onFinish(request);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mResponseHandler.onFailure(request, code, new NotFoundException(response.code() + "error,server is not found."));
                        mResponseHandler.onFinish(request);
                    }
                });
            }

        } catch (final Exception e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mResponseHandler.onFailure(request, code, e);
                    mResponseHandler.onFinish(request);
                }
            });
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }

}

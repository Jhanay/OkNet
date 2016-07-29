/**
 * Copyright (C) 2016 priscilla
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.solar.ikfa.http.callback;

import android.os.Message;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public abstract class StringCallback extends Callback {

    public abstract void onSuccess(String result, Request request);

    /**
     * @param response 转换成string
     */
    @Override
    public void onResponse(Call call, Response response) {
        super.onResponse(call, response);
        try {
            String result = response.body().string();
            sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{result, response.request()}));
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, call.request(), response.code()}));
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        Object[] objects = null;
        switch (msg.what) {
            case SUCCESS_MESSAGE:
                objects = (Object[]) msg.obj;
                onSuccess((String) objects[0], (Request) objects[1]);
                break;
        }
    }
}

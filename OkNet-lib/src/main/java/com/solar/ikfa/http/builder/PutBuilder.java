package com.solar.ikfa.http.builder;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.callback.Callback;
import com.solar.ikfa.http.response.ResponseHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author wujunjie
 * @date 2016/12/27
 */

public class PutBuilder extends RequestBuilder<PutBuilder> {

    protected Map<String, String> params;

    public PutBuilder(OkNet okNet) {
        super(okNet);
    }

    @Override
    public void enqueue(final ResponseHandler responseHandler) {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("url can not be null !");
        }

        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (headers != null) {
            builder.headers(headers.build());
        }
        if (tag != null) {
            builder.tag(tag);
        }
        builder.put(RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), ""));
        Request request = builder.build();

        if (connectTimeout > 0 || readTimeout > 0 || writeTimeout > 0) {
            innerBuilder = okNet.client().newBuilder();
            if (connectTimeout > 0) {
                innerBuilder.readTimeout(connectTimeout, TimeUnit.SECONDS);
            }
            if (readTimeout > 0) {
                innerBuilder.readTimeout(readTimeout, TimeUnit.SECONDS);
            }
            if (writeTimeout > 0) {
                innerBuilder.readTimeout(writeTimeout, TimeUnit.SECONDS);
            }
        }

        responseHandler.onStart();
        if (innerBuilder != null) {
            innerBuilder.build().newCall(request).enqueue(new Callback(responseHandler));
        } else {
            okNet.client().newCall(request).enqueue(new Callback(responseHandler));
        }
    }
}

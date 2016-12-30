package com.solar.ikfa.http.builder;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.callback.Callback;
import com.solar.ikfa.http.response.ResponseHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;

/**
 * @author wujunjie
 * @date 2016/12/27
 */

public class GetBuilder extends RequestBuilder<GetBuilder> {

    protected Map<String, String> params;

    public GetBuilder(OkNet okNet) {
        super(okNet);
    }

    public GetBuilder addParam(String key, String val) {
        if (params == null) {
            params = new LinkedHashMap<String, String>();
        }
        params.put(key, val);
        return this;
    }

    public GetBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    private String appendParams(Map<String, String> params) {
        String paramsEncoding = "UTF-8";
        StringBuilder encodedParams = new StringBuilder();
        encodedParams.append(url);
        encodedParams.append("?");
        try {
            Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                if (entries.hasNext()) {
                    encodedParams.append('&');
                }
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
        return encodedParams.toString();
    }

    @Override
    public void enqueue(final ResponseHandler responseHandler) {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("url can not be null !");
        }
        if (params != null && params.size() > 0) {
            url = appendParams(params);
        }
        Request.Builder builder = new Request.Builder();
        builder.url(url).get();
        if (headers != null) {
            builder.headers(headers.build());
        }
        if (tag != null) {
            builder.tag(tag);
        }
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

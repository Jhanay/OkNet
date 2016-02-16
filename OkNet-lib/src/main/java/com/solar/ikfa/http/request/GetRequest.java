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
package com.solar.ikfa.http.request;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import okhttp3.CacheControl;
import okhttp3.Request;

public class GetRequest {

    private Request.Builder builder = new Request.Builder();
    private String url;
    private Object tag;

    public GetRequest url(String url) {
        this.url = url;
        return this;
    }

    public GetRequest header(String key, String value) {
        builder.header(key, value);
        return this;
    }

    public GetRequest cacheControl(CacheControl cacheControl) {
        builder.cacheControl(cacheControl);
        return this;
    }

    public GetRequest tag(Object tag) {
        builder.tag(tag);
        return this;
    }

    public GetRequest map(WeakHashMap<String, String> params) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url is not set.");
        }
        if (params != null) {
            String paramsEncoding = "UTF-8";
            StringBuilder encodedParams = new StringBuilder();
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

                url += "?" + encodedParams.toString();
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
            }
        }
        return this;
    }

    public Request create() {
        builder.url(url);
        return builder.build();
    }

    @Deprecated
    private Request get(String url, Object... tag) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (tag.length > 0) {
            builder.tag(tag);
        }
        return builder.build();
    }

    @Deprecated
    private Request get(String url, WeakHashMap<String, String> params, Object... tag) {
        if (params != null) {
            String paramsEncoding = "UTF-8";
            StringBuilder encodedParams = new StringBuilder();
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

                url += "?" + encodedParams.toString();
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
            }
        }

        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (tag.length > 0) {
            builder.tag(tag);
        }
        return builder.build();
    }
}

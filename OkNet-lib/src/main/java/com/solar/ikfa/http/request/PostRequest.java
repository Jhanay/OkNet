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
package com.solar.ikfa.http.request;

import com.solar.ikfa.http.ProgressListener;
import com.solar.ikfa.http.ProgressRequestBody;
import com.solar.ikfa.http.callback.Callback;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostRequest {

    public static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream;charset=utf-8");
    public static final MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");
    public static final MediaType MEDIA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_IMG = MediaType.parse("image/*");
    public static final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");

    private Request.Builder builder = new Request.Builder();
    private Headers.Builder headers;
    private String url;
    private RequestBody requestBody;
    private Object tag;

    private Callback callback;

    public PostRequest url(String url) {
        this.url = url;
        return this;
    }

    public PostRequest header(String key, String value) {
        if (headers == null) {
            headers = new Headers.Builder();
        }
        headers.set(key, value);
        return this;
    }

    public PostRequest addHeader(String key, String value) {
        if (headers == null) {
            headers = new Headers.Builder();
        }
        headers.add(key, value);
        return this;
    }

    public PostRequest cacheControl(CacheControl cacheControl) {
        String value = cacheControl.toString();
        return addHeader("Cache-Control", value);
    }

    public PostRequest tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public PostRequest byteBuilder(byte[] bytes) {
        requestBody = RequestBody.create(MEDIA_TYPE_STREAM, bytes);
        return this;
    }

    public PostRequest strBuilder(String body) {
        requestBody = RequestBody.create(MEDIA_TYPE_FORM, body);
        return this;
    }

    public PostRequest jsonBuilder(String body) {
        requestBody = RequestBody.create(MEDIA_TYPE_JSON, body);
        return this;
    }

    public PostRequest fileBuilder(final File file) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_STREAM, file);
        requestBody = new ProgressRequestBody(body, new ProgressListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                if (callback != null) {
                    callback.upload(file.getName(), bytesRead, contentLength, done);
                }
            }
        });
        return this;
    }

    public PostRequest formBuilder(FormBody.Builder builder) {
        requestBody = builder.build();
        requestBody = new ProgressRequestBody(requestBody, new ProgressListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                if (callback != null) {
                    callback.upload("", bytesRead, contentLength, done);
                }
            }
        });
        return this;
    }

    public PostRequest multiBuilder(MultipartBody.Builder builder) {
        requestBody = builder.build();
        requestBody = new ProgressRequestBody(requestBody, new ProgressListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                if (callback != null) {
                    callback.upload("", bytesRead, contentLength, done);
                }
            }
        });
        return this;
    }

    public PostRequest request(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public Request create() {
        builder.url(url);
        builder.tag(tag);
        if (headers != null) {
            builder.headers(headers.build());
        }
        builder.post(requestBody);
        return builder.build();
    }

    public PostRequest callback(Callback callback) {
        this.callback = callback;
        return this;
    }

    @Deprecated
    private Request buildPostFormRequest(String url, WeakHashMap<String, String> params, Object... tag) {
        FormBody.Builder builder = new FormBody.Builder();
        Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = builder.build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.post(formBody);
        if (tag.length > 0) {
            requestBuilder.tag(tag[0]);
        }
        return requestBuilder.build();
    }

    public static String guessMediaType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    @Deprecated
    private Request buildMultipartFormRequest(String url, String[] fileKeys, File[] files, WeakHashMap<String, String> params, Object... tag) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (params != null) {
            Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                MediaType mediaType = MediaType.parse(guessMediaType(fileName));
                builder.addFormDataPart(fileKeys[i], fileName, RequestBody.create(mediaType, file));
            }
        }
        RequestBody requestBody = builder.build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.post(requestBody);
        if (tag.length > 0) {
            requestBuilder.tag(tag[0]);
        }
        return requestBuilder.build();
    }

    @Deprecated
    private Request buildMultipartFormRequest2(String url, String[] fileKeys, File[] files,
                                               WeakHashMap<String, String> params, Object... tag) {

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (params != null) {
            Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                        RequestBody.create(null, entry.getValue()));
            }
        }

        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMediaType(fileName)), file);
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }
        }

        RequestBody requestBody = builder.build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.post(requestBody);
        if (tag.length > 0) {
            requestBuilder.tag(tag[0]);
        }
        return requestBuilder.build();
    }

    @Deprecated
    private Request buildStringRequest(String url, String bodyStr, Object... tag) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_PLAIN, bodyStr);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(body);
        if (tag.length > 0) {
            builder.tag(tag[0]);
        }
        return builder.build();
    }

    @Deprecated
    private Request buildFileRequest(String url, File bodyFile, ProgressListener progressListener, Object... tag) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_STREAM, bodyFile);
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(body, progressListener);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(progressRequestBody);
        if (tag.length > 0) {
            builder.tag(tag[0]);
        }
        return builder.build();
    }

    @Deprecated
    private Request buildByteRequest(String url, byte[] bytes, Object... tag) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_STREAM, bytes);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(body);
        if (tag.length > 0) {
            builder.tag(tag[0]);
        }
        return builder.build();
    }
}

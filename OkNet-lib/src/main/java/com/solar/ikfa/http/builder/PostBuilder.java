package com.solar.ikfa.http.builder;

import android.os.Handler;
import android.os.Looper;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.body.ProgressListener;
import com.solar.ikfa.http.body.ProgressRequestBody;
import com.solar.ikfa.http.callback.Callback;
import com.solar.ikfa.http.response.ResponseHandler;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author wujunjie
 * @date 2016/12/27
 */

public class PostBuilder extends RequestBuilder<PostBuilder> {

    public static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream;charset=utf-8");
    public static final MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");
    public static final MediaType MEDIA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_IMG = MediaType.parse("image/*");
    public static final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");

    protected RequestBody requestBody;
    protected String fileName;
    private boolean progress;

    public PostBuilder(OkNet okNet) {
        super(okNet);
    }

    public PostBuilder bytes(byte[] bytes) {
        requestBody = RequestBody.create(MEDIA_TYPE_STREAM, bytes);
        return this;
    }

    public PostBuilder string(String body) {
        requestBody = RequestBody.create(MEDIA_TYPE_FORM, body);
        return this;
    }

    public PostBuilder json(String body) {
        requestBody = RequestBody.create(MEDIA_TYPE_JSON, body);
        return this;
    }

    public PostBuilder file(final File file) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_STREAM, file);
        fileName = file.getName();
        return this;
    }

    public PostBuilder form(FormBody.Builder builder) {
        requestBody = builder.build();
        return this;
    }

    public PostBuilder multipart(MultipartBody.Builder builder) {
        requestBody = builder.build();
        return this;
    }

    public PostBuilder progress(boolean progress) {
        this.progress = progress;
        return this;
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

        if (progress) {
            requestBody = new ProgressRequestBody(requestBody, new ProgressListener() {
                @Override
                public void onProgress(final long bytesRead, final long contentLength, final boolean done) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseHandler.onProgress(fileName, bytesRead, contentLength, done);
                        }
                    });
                }
            });
        }

        builder.post(requestBody);
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

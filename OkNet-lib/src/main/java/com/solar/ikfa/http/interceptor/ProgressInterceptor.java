package com.solar.ikfa.http.interceptor;

import android.os.Handler;
import android.os.Looper;

import com.solar.ikfa.http.body.ProgressListener;
import com.solar.ikfa.http.body.ProgressResponseBody;
import com.solar.ikfa.http.response.DownloadResponseHandler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author wujunjie
 */
public class ProgressInterceptor implements Interceptor {

    private DownloadResponseHandler mResponseHandler;
    private long mCompleteBytes = 0L;

    public ProgressInterceptor(DownloadResponseHandler responseHandler, long completeBytes) {
        this.mResponseHandler = responseHandler;
        this.mCompleteBytes = completeBytes;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final okhttp3.Request request = chain.request();
        final okhttp3.Response originalResponse = chain.proceed(request);

        return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(), new ProgressListener() {

            @Override
            public void onProgress(long bytesRead, long contentLength, final boolean done) {
                if (contentLength != -1) {
//                        System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
                    //断点续传的总长度为未完成部分长度
                    if (mCompleteBytes > 0) {
                        bytesRead += mCompleteBytes;
                        contentLength += mCompleteBytes;
                    }

                    final String url = request.url().toString();
                    final long finalByteRead = bytesRead;
                    final long finalContentLength = contentLength;

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mResponseHandler.onProgress(url, finalByteRead, finalContentLength, done);
                        }
                    });
                }
            }
        })).build();
    }
}

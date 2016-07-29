package com.solar.ikfa.http.interceptor;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.ProgressListener;
import com.solar.ikfa.http.ProgressResponseBody;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author wujunjie
 * @date 16/7/29
 */
public class ProgressInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        final okhttp3.Request request = chain.request();
        final okhttp3.Response originalResponse = chain.proceed(request);

        return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(), new ProgressListener() {

            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                if (contentLength != -1) {
//                        System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
                    OkNet.CALLBACK.get(request.tag()).download(request.url().toString(), bytesRead, contentLength, done);
                    if (done) {
                        OkNet.CALLBACK.remove(request.tag());
                    }
                }
            }
        })).build();
    }
}

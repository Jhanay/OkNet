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
package com.solar.ikfa.http;

import com.solar.ikfa.http.callback.Callback;
import com.solar.ikfa.http.interceptor.ProgressInterceptor;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkNet {

    private static OkNet singleton;
    private static OkHttpClient client = new OkHttpClient();
    public static Map<Object, Callback> CALLBACK = Collections.synchronizedMap(new WeakHashMap<Object, Callback>());

    private OkNet() {
        OkHttpClient.Builder builder = client.newBuilder();

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
        builder.cookieJar(new JavaNetCookieJar(cookieManager));
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        builder.addNetworkInterceptor(new ProgressInterceptor());

        builder.connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        client = builder.build();
    }

    public static OkNet singleton() {
        if (null == singleton) {
            synchronized (OkNet.class) {
                if (null == singleton) {
                    singleton = new OkNet();
                }
            }
        }
        return singleton;
    }

    /**
     * @return 构造新的client
     */
    public Configure newConfigure() {
        return new Configure();
    }

    public Builder with(Request request) {
        return new Builder(request);
    }

    public void cancel(Object tag) {
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) call.cancel();
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) call.cancel();
        }
    }

    private synchronized void sendAsyncRequest(final OkHttpClient client, final Request request, final Callback callback) {
        CALLBACK.put(request.tag() == null ? request : request.tag(), callback);

        if (callback != null) {
            callback.onStart();
            client.newCall(request).enqueue(callback);
        }
    }

    public OkHttpClient client() {
        return client;
    }

    public final class Builder {

        private Request request;
        private long connectTimeout;
        private long readTimeout;
        private long writeTimeout;
        private OkHttpClient.Builder innerBuilder;

        public Builder(Request request) {
            this.request = request;
        }

        public Builder connectTimeout(long timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        /**
         * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         */
        public Builder readTimeout(long timeout) {
            this.readTimeout = timeout;
            return this;
        }

        /**
         * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         */
        public Builder writeTimeout(long timeout) {
            this.writeTimeout = timeout;
            return this;
        }

        public void callback(Callback callback) {
            if (request == null) {
                throw new IllegalArgumentException("request is null");
            }
            if (callback == null) {
                throw new IllegalArgumentException("callback is null");
            }

            if (connectTimeout > 0 || readTimeout > 0 || writeTimeout > 0) {
                innerBuilder = client.newBuilder();
                if (connectTimeout > 0) {
                    innerBuilder.readTimeout(connectTimeout, TimeUnit.MILLISECONDS);
                }
                if (readTimeout > 0) {
                    innerBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
                }
                if (writeTimeout > 0) {
                    innerBuilder.readTimeout(writeTimeout, TimeUnit.MILLISECONDS);
                }
            }
            if (innerBuilder == null) {
                sendAsyncRequest(client, request, callback);
            } else {
                sendAsyncRequest(innerBuilder.build(), request, callback);
            }
        }
    }

    public static final class Configure {
        OkHttpClient.Builder builder;

        public Configure() {
            builder = new OkHttpClient.Builder();
        }

        public Configure cache(File cacheDir, long cacheSize) {
            builder.cache(new Cache(cacheDir, cacheSize));
            return this;
        }

        public Configure maxCacheAge(final long maxCacheAge) {
            if (maxCacheAge < 0)
                throw new IllegalArgumentException("maxCacheAge params is not correct. must > 0");
            builder.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .removeHeader("Pragma")
                            .header("Cache-Control", String.format("max-age=%d", maxCacheAge))
                            .build();
                }
            });
            return this;
        }

        /**
         * Sets the default connect timeout for new connections. A value of 0 means no timeout,
         * otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
         * milliseconds.
         */
        public Configure connectTimeout(long timeout) {
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
            return this;
        }

        /**
         * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         */
        public Configure readTimeout(long timeout) {
            builder.readTimeout(timeout, TimeUnit.SECONDS);
            return this;
        }

        /**
         * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         */
        public Configure writeTimeout(long timeout) {
            builder.writeTimeout(timeout, TimeUnit.SECONDS);
            return this;
        }

        /**
         * Sets the handler that can accept cookies from incoming HTTP responses and provides cookies to
         * outgoing HTTP requests.
         * <p>
         * <p>If unset, {@linkplain CookieJar#NO_COOKIES no cookies} will be accepted nor provided.
         */
        public Configure cookiePolicy(CookiePolicy cookiePolicy) {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(cookiePolicy);
            builder.cookieJar(new JavaNetCookieJar(cookieManager));
            return this;
        }

        /**
         * Sets the socket factory used to create connections. OkHttp only uses the parameterless {@link
         * SocketFactory#createSocket() createSocket()} method to create unconnected sockets. Overriding
         * this method, e. g., allows the socket to be bound to a specific local address.
         * <p>
         * <p>If unset, the {@link SocketFactory#getDefault() system-wide default} socket factory will
         * be used.
         */
        public Configure socketFactory(SocketFactory socketFactory) {
            if (socketFactory == null) throw new NullPointerException("socketFactory == null");
            builder.socketFactory(socketFactory);
            return this;
        }

        /**
         * Sets the socket factory used to secure HTTPS connections.
         * <p>
         * <p>If unset, a lazily created SSL socket factory will be used.
         */
        public Configure sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            if (sslSocketFactory == null)
                throw new NullPointerException("sslSocketFactory == null");
            builder.sslSocketFactory(sslSocketFactory);
            return this;
        }

        /**
         * Sets the verifier used to confirm that response certificates apply to requested hostnames for
         * HTTPS connections.
         * <p>
         * <p>If unset, a default hostname verifier will be used.
         */
        public Configure hostnameVerifier(HostnameVerifier hostnameVerifier) {
            if (hostnameVerifier == null)
                throw new NullPointerException("hostnameVerifier == null");
            builder.hostnameVerifier(hostnameVerifier);
            return this;
        }

        /**
         * Sets the certificate pinner that constrains which certificates are trusted. By default HTTPS
         * connections rely on only the {@link #sslSocketFactory SSL socket factory} to establish trust.
         * Pinning certificates avoids the need to trust certificate authorities.
         */
        public Configure certificatePinner(CertificatePinner certificatePinner) {
            if (certificatePinner == null)
                throw new NullPointerException("certificatePinner == null");
            builder.certificatePinner(certificatePinner);
            return this;
        }

        /**
         * Sets the authenticator used to respond to challenges from origin servers. Use {@link
         * #proxyAuthenticator} to set the authenticator for proxy servers.
         * <p>
         * <p>If unset, the {@linkplain Authenticator#NONE no authentication will be attempted}.
         */
        public Configure authenticator(Authenticator authenticator) {
            if (authenticator == null) throw new NullPointerException("authenticator == null");
            builder.authenticator(authenticator);
            return this;
        }

        /**
         * Sets the authenticator used to respond to challenges from proxy servers. Use {@link
         * #authenticator} to set the authenticator for origin servers.
         * <p>
         * <p>If unset, the {@linkplain Authenticator#NONE no authentication will be attempted}.
         */
        public Configure proxyAuthenticator(Authenticator proxyAuthenticator) {
            if (proxyAuthenticator == null)
                throw new NullPointerException("proxyAuthenticator == null");
            builder.proxyAuthenticator(proxyAuthenticator);
            return this;
        }

        /**
         * Sets the connection pool used to recycle HTTP and HTTPS connections.
         * <p>
         * <p>If unset, a new connection pool will be used.
         */
        public Configure connectionPool(ConnectionPool connectionPool) {
            builder.connectionPool(connectionPool);
            return this;
        }

        public Configure addInterceptor(Interceptor interceptor) {
            builder.addInterceptor(interceptor);
            return this;
        }

        public Configure addNetworkInterceptor(Interceptor interceptor) {
            builder.addNetworkInterceptor(interceptor);
            return this;
        }

        public OkNet config() {
            client = builder.build();
            return singleton;
        }

    }

}
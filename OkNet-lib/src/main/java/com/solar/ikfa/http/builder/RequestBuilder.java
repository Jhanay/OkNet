package com.solar.ikfa.http.builder;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.response.ResponseHandler;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * @author wujunjie
 * @date 2016/12/27
 */

public abstract class RequestBuilder<T extends RequestBuilder> {

    protected String url;
    protected Object tag;
    protected Headers.Builder headers;

    protected OkNet okNet;
    protected OkHttpClient.Builder innerBuilder;

    protected int connectTimeout;    //= 10;
    protected int readTimeout;       //= 30;
    protected int writeTimeout;      //= 20;

    public RequestBuilder(OkNet okNet) {
        this.okNet = okNet;
    }

    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    public T header(String key, String value) {
        if (headers == null) {
            headers = new Headers.Builder();
        }
        headers.set(key, value);
        return (T) this;
    }

    public T addHeader(String key, String value) {
        if (headers == null) {
            headers = new Headers.Builder();
        }
        headers.add(key, value);
        return (T) this;
    }

    public T cacheControl(CacheControl cacheControl) {
        String value = cacheControl.toString();
        return addHeader("Cache-Control", value);
    }

    public T tag(Object tag) {
        this.tag = tag;
        return (T) this;
    }

    public T connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return (T) this;
    }

    /**
     * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public T readTimeout(int timeout) {
        this.readTimeout = timeout;
        return (T) this;
    }

    /**
     * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public T writeTimeout(int timeout) {
        this.writeTimeout = timeout;
        return (T) this;
    }

    public abstract void enqueue(ResponseHandler responseHandler);

}

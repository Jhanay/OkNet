package com.solar.ikfa.http.response;

import okhttp3.Request;

/**
 * @author wujunjie
 * @date 2016/12/28
 */

public abstract class RawResponseHandler extends ResponseHandler {

    @Override
    public void onResponse(Request request, String result) {
        onSuccess(request, result);
    }

    public abstract void onSuccess(Request request, String result);

}

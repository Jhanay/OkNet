package com.solar.ikfa.http.response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Request;

/**
 * @author wujunjie
 * @date 2016/12/28
 */

public abstract class GsonResponseHandler<T> extends ResponseHandler {

    @Override
    public void onResponse(Request request, String result) {
        try {
            T t = new Gson().fromJson(result, getSuperclassTypeParameter(getClass()));
            onSuccess(request, t);
        } catch (JsonSyntaxException e) {
            onFailure(request, -1, e);
        }
    }

    public abstract void onSuccess(Request request, T result);

    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterizedType = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterizedType.getActualTypeArguments()[0]);
    }
}

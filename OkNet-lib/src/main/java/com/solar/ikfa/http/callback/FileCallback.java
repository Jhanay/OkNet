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
package com.solar.ikfa.http.callback;

import android.os.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public abstract class FileCallback extends Callback {

    private String destFileDir;

    public FileCallback(String destFileDir) {
        super();
        this.destFileDir = destFileDir;
    }

    public abstract void onSuccess(String result, Request request);

    /**
     * @param response 转换成file
     */
//    @Override
//    public void onResponse(Call call, Response response) {
//        super.onResponse(call, response);
//
//        InputStream is = null;
//        FileOutputStream fos = null;
//        byte[] buf = new byte[2048];
//        int len = 0;
//
//        try {
//
//            final long contentLength = response.body().contentLength();
//            long bytesRead = 0;
//
//            File dir = new File(destFileDir);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//            String url = response.request().url().url().toString();
//            File file = new File(dir, getFileName(url));
//
//            is = response.body().byteStream();
//            fos = new FileOutputStream(file);
//            while ((len = is.read(buf)) != -1) {
//                bytesRead += len;
//                fos.write(buf, 0, len);
//                download(response.request().url().url().toString(), bytesRead, contentLength, bytesRead == contentLength);
//            }
//            fos.flush();
//            sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{file.getAbsolutePath(), response.request()}));
//        } catch (IOException e) {
//            e.printStackTrace();
//            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, response.request(), response.code()}));
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//                if (fos != null) {
//                    fos.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, response.request(), response.code()}));
//            }
//        }
//    }

    @Override
    public void onResponse(Call call, Response response) {
        super.onResponse(call, response);

        BufferedSink sink = null;
        String url = response.request().url().toString();
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, getFileName(url));

        try {
            sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(response.body().source());

            sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{file.getAbsolutePath(), response.request()}));
        } catch (FileNotFoundException e) {
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, response.request(), response.code()}));
        } catch (IOException e) {
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, response.request(), response.code()}));
        } finally {
            if (sink != null) {
                try {
                    sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        Object[] objects = null;
        switch (msg.what) {
            case SUCCESS_MESSAGE:
                objects = (Object[]) msg.obj;
                onSuccess((String) objects[0], (Request) objects[1]);
                break;
        }
    }

    private String getFileName(String path) {
        int separatorIndex = path.lastIndexOf(File.separator);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }
}

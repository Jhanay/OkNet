package com.solar.ikfa.http.callback;

import android.os.Handler;
import android.os.Looper;

import com.solar.ikfa.http.response.DownloadResponseHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author wujunjie
 * @date 2016/12/28
 */

public class DownloadCallback implements okhttp3.Callback {

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private DownloadResponseHandler mDownloadResponseHandler;
    private String filePath;
    private long mCompleteBytes;  //已下载的字节

    public DownloadCallback(DownloadResponseHandler downloadResponseHandler, String filePath, long completeBytes) {
        this.mDownloadResponseHandler = downloadResponseHandler;
        this.filePath = filePath;
        this.mCompleteBytes = completeBytes;

    }

    @Override
    public void onFailure(final Call call, final IOException e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDownloadResponseHandler.onFailure(call.request(), -1, e);
                mDownloadResponseHandler.onFinish(call.request());
            }
        });
    }

//    @Override
//    public void onResponse(Call call, final Response response) throws IOException {
//        BufferedSink sink = null;
//        File file = new File(filePath);
//        final Request request = response.request();
//        final int code = response.code();
//        try {
//            sink = Okio.buffer(Okio.sink(file));
//            sink.writeAll(response.body().source());
//
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mDownloadResponseHandler.onSuccess(request, filePath);
//                }
//            });
//        } catch (final FileNotFoundException e) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mDownloadResponseHandler.onFailure(request, code, e);
//                }
//            });
//        } catch (final IOException e) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mDownloadResponseHandler.onFailure(request, code, e);
//                }
//            });
//        } finally {
//            if (sink != null) {
//                try {
//                    sink.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


    @Override
    public void onResponse(Call call, Response response) {
        ResponseBody body = response.body();
        final Request request = response.request();
        final int code = response.code();
        InputStream in = body.byteStream();
        FileChannel channelOut = null;
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(filePath, "rwd");
            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
            channelOut = randomAccessFile.getChannel();
            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, mCompleteBytes, body.contentLength());
            byte[] buffer = new byte[2 * 1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadResponseHandler.onSuccess(request, filePath);
                }
            });
        } catch (final IOException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadResponseHandler.onFailure(request, code, e);
                }
            });
        } finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

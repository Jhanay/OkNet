package com.solar.ikfa;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.response.DownloadResponseHandler;
import com.solar.ikfa.http.response.RawResponseHandler;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });

        /**
         OkNet.singleton().newConfigure()
         .connectTimeout(10).readTimeout(30).writeTimeout(20)
         .cache(cacheDir, 50 * 1024 * 1024) //设置缓存位置和大小
         .cookiePolicy(CookiePolicy.ACCEPT_ALL)
         .addInterceptor(interceptor)
         .addNetworkInterceptor(interceptor)
         .sslSocketFactory(sslSocketFactory) //设置私有https证书
         .config();
         */
    }

    RawResponseHandler responseHandler = new RawResponseHandler() {
        @Override
        public void onSuccess(Request request, String result) {
            System.out.print("result:" + result);
        }

        @Override
        public void onFailure(Request request, int code, Exception e) {
            System.out.print("code:" + code);
        }

        @Override
        public void onStart() {
            //TODO showDialog
        }

        @Override
        public void onFinish(Request request) {
            //TODO dismissDialog
        }

        @Override
        public void onProgress(String params, long bytesRead, long contentLength, boolean done) {
            System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
        }
    };

    private void getExample() {
        String url = "http://www.baidu.com";
//        Request request = new GetRequest().url(url).tag(this).create();
//        OkNet.singleton().with(request).callback(callback);

        OkNet.singleton().get().url(url).tag(this).enqueue(responseHandler);
    }

    private void postExample() {
        String url = "";
        OkNet.singleton().post()
                .form(new FormBody.Builder()
                        .add("account", "solar")
                        .add("password", "123456"))
                .tag(this)
                .enqueue(responseHandler);
    }

    private void download() {
        String url = "http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk";
        String filePath = Environment.getExternalStorageDirectory() + File.separator + "weixin_821.apk";
        System.out.println("filePath:" + filePath);
        OkNet.singleton().download().url(url).filePath(Environment.getExternalStorageDirectory() + File.separator + "weixin_821.apk")
                .tag(this).enqueue(new DownloadResponseHandler() {
            @Override
            public void onStart() {
                System.out.println("onStart:::::");
            }

            @Override
            public void onFinish(Request request) {
                System.out.println("onFinish:::::");
            }

            @Override
            public void onFailure(Request request, int code, Exception e) {
                System.out.println("onFailure:::::");
            }

            @Override
            public void onProgress(String fileName, long bytesRead, long contentLength, boolean done) {
                System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
            }

            @Override
            public void onSuccess(Request request, String result) {
                System.out.println("onSuccess:::::" + result);
            }
        });

    }
}

package com.solar.ikfa;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.solar.ikfa.http.OkNet;
import com.solar.ikfa.http.callback.Callback;
import com.solar.ikfa.http.callback.StringCallback;
import com.solar.ikfa.http.request.GetRequest;
import com.solar.ikfa.http.request.PostRequest;

import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

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
                getExample();
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
                .gzip()  //支持gzip解压数据
                .config();
         */
    }

    Callback callback = new StringCallback() {
        @Override
        public void onSuccess(String result, Request request) {
            System.out.print(result);
        }

        @Override
        public void onError(Exception e, Request request, int code) {

        }

        @Override
        public void onStart() {
            super.onStart();
            //TODO showDialog
        }

        @Override
        public void onFinish(Request request) {
            super.onFinish(request);
            //TODO dismissDialog
        }

        @Override
        public void onProgress(String params, long bytesRead, long contentLength, boolean done) {
            System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
        }
    };

    private void getExample() {
        String url = "http://www.baidu.com";
        Request request = new GetRequest().url(url).tag(this).create();
        OkNet.singleton()
                .with(request)
                .callback(callback)
                .get();
    }

    private void postExample() {
        String url = "";
        Request request = new PostRequest().url(url)
                .formBuilder(new FormBody.Builder()
                        .add("account", "solar")
                        .add("password", "123456"))
                .tag(this)
                .create();
        OkNet.singleton().with(request).callback(callback).post();
    }
}

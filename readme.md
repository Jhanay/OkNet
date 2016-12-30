#OkNet
**OkNet**是一个网络请求的工具类，是**OkHttp**的一个二次封装，所以具备**OkHttp**的所有优点。具体有哪些特性可以参考[OkHttp wiki](https://github.com/square/okhttp/wiki)。

##Gradle配置

	compile 'com.solar.ikfa:oknet:1.2.0'

##OkNet常用API介绍

**OkNet**初始化：

	OkNet.singleton();
	
采用单例模式生成实例，默认cookie策略为ACCEPT_ALL，连接时间10s，读时间30s，写时间20s。
可以在重新配置。实现如下:
	
	OkNet.singleton().newConfigure()
                .connectTimeout(10).readTimeout(30).writeTimeout(20)
                .cache(cacheDir, 50 * 1024 * 1024) //设置缓存位置和大小
                .cookiePolicy(CookiePolicy.ACCEPT_NONE)
                .addInterceptor(interceptor)
                .addNetworkInterceptor(interceptor)
                .sslSocketFactory(sslSocketFactory) //设置私有https证书
                .config();
	
*注意此方法需要在application中初始化，另外不要忘记注册该application。

###使用说明
#####Get方式：

	String url = "http://...";
	或
	Map<String,String> map = new WeakHashMap<String,String>();
	map.put("account","solar");
	map.put("password","123456");
	
	OkNet.singleton()
	     .get()
	     .url(url)
	     .map(map)
	     .tag(this)
	     .enqueue(responseHandler);
	
*map最终生成的url是URIEncode加密的，格式为&account=solar&password=123456;

#####Post方式：
1.表单提交

	OkNet.singleton()
	     .post()
	     .url(url)
	     .form(new FormBody.Builder()
                   .add("account", "solar")
                   .add("password", "123456"))
	     .tag(this)
	     .enqueue(responseHandler);
	
2.string提交

	OkNet.singleton()
	     .post()
	     .url(url)
	     .string(string)
	     .tag(this)
	     .enqueue(responseHandler);
	
3.json提交

	OkNet.singleton()
	     .post()
	     .url(url)
	     .json(jsonString)
	     .tag(this)
	     .enqueue(responseHandler);
	
4.file提交
	
	OkNet.singleton()
	     .post()
	     .url(url)
	     .file(file)
	     .progress(true) //添加进度监听
	     .tag(this)
	     .enqueue(responseHandler);
	
5.multi提交(表单及文件，多个文件等)
	
	 MultipartBody.Builder multiBuilder = new MultipartBody.Builder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(key, value)
                .addFormDataPart(key, fileName,RequestBody.create(mediaType, file));
     Request request = new PostRequest().url(url).multiBuilder(multiBuilder).tag(your tag).create();
     
     OkNet.singleton()
	     .post()
	     .url(url)
	     .multipart(new MultipartBody.Builder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(key, value)
                .addFormDataPart(key, fileName,RequestBody.create(mediaType, file))
	     .tag(this)
	     .enqueue(responseHandler);
                
*注意key是指服务器定义的键，fileName为文件名，mediaType有多种：图片、文件、音视频等。可以根据文件名后缀获得类型，如果没有后缀或找不到类型，默认为文件流类型。
	
	String mediaType = PostRequest.guessMediaType(fileName);
	
#####回调函数ResponseHandler

1. RawResponseHandler 字符串返回
2. GsonResponseHandler json返回
3. DownloadResponseHandler download返回

下面是返回的例子:

~~~
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
~~~

#####取消网络请求
在生命周期结束的时候，我们希望终止网络，避免网络回调引起的异常，可以执行如下语句：

	OkNet.singleton().cancle(your tag);


#####使用技巧
一个页面多个请求时，希望在第一个请求show加载框，最后一个请求结束加载框，可以用一个responseHandler，在start的时候show（此处记得是判断是否已经show），在finish的时候判断request等于request3的时候dismiss即可

	
#####结尾
基于OKhttp库封装的网络工具多种多样，本库也参考了多个大牛作品。

#####参考资源
[okhttp-utils](https://github.com/hongyangAndroid/okhttp-utils)

[MyOkHttp](https://github.com/tsy12321/MyOkHttp)
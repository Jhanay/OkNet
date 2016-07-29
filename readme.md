#OkNet
**OkNet**是一个网络请求的工具类，是**OkHttp**的一个二次封装，所以具备**OkHttp**的所有优点。具体有哪些特性可以参考[OkHttp wiki](https://github.com/square/okhttp/wiki)。

##Gradle配置

	compile 'com.solar.ikfa:oknet:1.0.2'

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
                .gzip()  //支持gzip解压数据
                .config();
	
*注意此方法需要在application中初始化，另外不要忘记注册该application。

###get方式
#####生成request

	String url = "http://...";
	Request request = new GetRequest().url(url).create();
	或
	Map<String,String> map = new WeakHashMap<String,String>();
	map.put("account","solar");
	map.put("password","123456");
	Request request = new GetRequest().map(map).create();
	
*map最终生成的url是URIEncode加密的，格式为&account=solar&password=123456;
#####发起请求
	OkNet.singleton().with(request).callback(listener);

###post方式
post方式请求body种类比较繁多，后面再介绍。
#####发起请求
	OkNet.singleton().with(request).callback(listener);
#####配置独立超时时间(本次请求有效，单位秒)
	OkNet.singleton().with(request)
	.connectTimeout(15)
	.readTimeout(30)
	.writeTimeout(20)
	.callback(listener);
#####表单提交

	FormBody.Builder formBuilder = new FormBody.Builder
                        .add("userName", "solar")
                        .add("password", "123456")
                        .add("client", "0");
   	Request request = new PostRequest().url(url).formBuilder(formBuilder).tag(your tag).create();
	
#####string提交
	Request request = new PostRequest().url(url).strBuilder(str).tag(your tag).create();
	
#####file提交
	Request request = new PostRequest().url(url).fileBuilder(file).tag(your tag).create();
	
#####multi提交
multi提交的案例主要包含：表单及文件，多个文件等
	
	 MultipartBody.Builder multiBuilder = new MultipartBody.Builder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(key, value)
                .addFormDataPart(key, fileName,RequestBody.create(mediaType, file));
     Request request = new PostRequest().url(url).multiBuilder(multiBuilder).tag(your tag).create();
                
*注意key是指服务器定义的键，fileName为文件名，mediaType有多种：图片、文件、音视频等。可以根据文件名后缀获得类型，如果没有后缀或找不到类型，默认为文件流类型。
	
	String mediaType = PostRequest.guessMediaType(fileName);
	
#####回调函数Callback
上述get/post提交数据返回采用回调方式。callback返回类型主要有**StringCallback**文本返回，**GsonCallback<T>**对象返回，**FileCallback**文件返回。
下面是返回的例子:

~~~
	StringCallback callback = new StringCallback(){
	
		@Override
            public void onStart() {
                
            }

            @Override
            public void onFinish(Request request) {
                
            }

            @Override
            public void onError(Exception e, Request request, int code) {

            }

            @Override
            public void onSuccess(String result, Request request) {
                //此处request可以区分是哪个网络请求，所以多个网络请求可以共用一个回调
            }
	}
	
	JsonCallback  callback = new JsonCallback<Result<User>>() {

            @Override
            public void onStart() {
               
            }

            @Override
            public void onFinish(Request request) {
                
            }

            @Override
            public void onError(Exception e, Request request, int code) {

            }

            @Override
            public void onSuccess(Result<User> result, Request request) {
                if (result != null) {
                    
                }
            }
        }
        
        FileCallback fileCallback = new FileCallback(your file path dir) {

        @Override
        public void onStart() {
        	  
        }

        @Override
        public void onFinish(Request request) {
            super.onFinish(request);
        }

        @Override
        public void onError(Exception e, Request request, int code) {

        }

        @Override
        public void onSuccess(String result, Request request) {
            
        }
        
        @Override
            public void onProgress(String params, long bytesRead, long contentLength, boolean done) {
                super.onProgress(params, bytesRead, contentLength, done);
            }
    }
~~~
*上传文件需要进度有两种处理方式：

~~~
	new PostRequest()...
		.callback(fileCallback);
		或
	在callback的onStart()方法中添加：
       postRequest.callback(this);
	
~~~
将callback赋给request即可。

#####取消网络请求
在生命周期结束的时候，我们希望终止网络，避免网络回调引起的异常，可以执行如下语句：

	OkNet.singleton().cancle(your tag);


#####使用技巧
一个页面多个请求时，希望在第一个请求show加载框，最后一个请求结束加载框，可以用一个callback，在start的时候show（此处记得是判断是否已经show），在finish的时候判断request等于request3的时候dismiss即可

	
#####结尾
网络工具使用不够简便的地方，需要大家在平常使用中暴露出来，并优化。有更好的架构思路会去迭代更新。

#####感谢
[okhttp-utils](https://github.com/hongyangAndroid/okhttp-utils)
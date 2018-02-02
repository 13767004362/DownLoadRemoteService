# DownLoadRemoteService

**介绍**：
>平台架构模块之远程下载服务。 多个apk运用程序可以通过进程通讯（添加远程服务通讯的API框架库DownloadServiceSDK），进而通讯到远程下载服务。
>
> 通过平台思维，采用共同服务的方式，避免了每个项目Module都添加下载库，更节省平台开发的时间。

**项目模块介绍**：

- 一个远程断点续传多线程下载的远程服务项目（DownloadRemoteService）,

- 一个对远程服务通讯的API框架库（DownloadServiceSDK）

- 一个测试案例客户端（app）。


**涉及的技术要点**：

- 采用OkHttp3作为传输层，替代HttpUrlConnection
- 采用远程服务，通过IPC通讯机制,这里使用aidl和binder作为跨进程通讯方式。

- 下载方式有两种

   - 第一种： 采用单文件直接下载 。
   - 第二种： 超大文件断点多线程续传下载。

----

#### 使用步骤

**1. 依赖DownloadServiceSDK框架**：

   在Module中libs目录下添加 DownloadService.jar，对着jar文件，
    点击右键  add as library.完成依赖该库，或者build.gradle方式依赖该库。

**2. 开启远程下载服务服务**:

先将DownloadRemoteService项目打包出apk，然后安装在手机上或者手机模拟器上。

通过代码开启远程服务：
```JAVA
   /**
     *
     *  开启远程下载服务，先安装远程下载服务的apk.
     *
     */
    private void initDownloadService() {
        try {
            Intent intent = new Intent("com.zongke.downloadservice.service.DownLoadService");
            intent.setPackage("com.zongke.downloadservice");
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
```
**3. 使用远程服务通讯的API框架库进行初始化**：

```
 ServiceClient serviceClient = DownloadServiceClient.getInstance();
 serviceClient.init(getApplicationContext());
```
初始化操作只需要执行一次，最好放在Application子类中的onCreate()中执行。

**4. 单文件直接下载**：

介绍一个下载过程状态的监听器(DownLoadResultListener )，具体下载进度，下载失败，下载成功，已经下载过的状态回调。
```
public interface DownLoadResultListener {
    /**
     * 任务下载中
     * @param url
     * @param filePath
     * @param progress
     */
    void taskProgress(String url,String filePath,int progress);
    /**
     * 任务完成
     * @param url
     * @param filePath
     */
    void taskFinish( String url,String filePath);

    /**
     * 任务失败
     */
    void taskFailure(String url);

    /**
     * 任务已经下载完成了
     * @param url
     */
    void taskAlreadyDownload(String url,String filePath);
}

```

可以通过实现该接口，或者匿名内部类的方式，获取到该接口对象。回调方法都是执行在UI线程中，可以直接更新UI操作。

接下来，进行下载操作。

```
//网络的url
 String url1 = "http://downpack.baidu.com/appsearch_AndroidPhone_1012271b.apk";
 //文件存放路径
 String filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "baidu.apk";
 //开启下载
 serviceClient.startSingleDownloadTask(url1,filePath , this);

```


**5. 超大文件断点续传多线程下载**：

调用方式和单文件直接下载类似，调用方法不同。这里支持暂停，续传下载的功能。

```
 String url2 = "http://yun.aiwan.hk/1441972507.apk";
 String filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "baihewang.apk";
 //第三个参数是回调监听器
 serviceClient.startMultiDownloadTask(url2,filePath , this);
```

原理： 分块+数据库+多线程方式实现。

**6. 删除以前文件，重新下载**：
```
 String url2 = "http://yun.aiwan.hk/1441972507.apk";
 String filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "baihewang.apk";
 serviceClient.againStartDownloadTask(url2,filePath , this);
```

注意点： 重新下载走的是多线程断点续传下载的方式，而不是走文件直接下载的方式。


PS:本Project涉及技术较多，进程通讯+通讯库方式实现，多客户端程序对应一个远程服务的需求。多线程断点续传下载功能，就不在一一详细介绍。




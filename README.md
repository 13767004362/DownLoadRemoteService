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






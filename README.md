# DownLoadRemoteService
厂商的平台架构模块之远程下载服务，这里包含一个远程断点续传下载的远程服务项目（DownloadRemoteService）,一个对远程服务通讯的API框架库（DownloadServiceSDK）,一个案例客户端（app）。


以下，三个重点的备注：

- 采用OkHttp3作为传输层，替代HttpUrlConnection

- 采用断点续传下载，分三个模块，数据库记录下载记录

- 采用远程服务，通过IPC通讯机制,这里使用aidl和binder作为跨进程通讯方式。

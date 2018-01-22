/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\ZongKeDownLoadService\\DownloadRemoteService\\src\\main\\aidl\\com\\zongke\\downloadservice\\DownLoadRemoteService.aidl
 */
package com.zongke.downloadservice;
// Declare any non-default types here with import statements

public interface DownLoadRemoteService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements DownLoadRemoteService
{
private static final String DESCRIPTOR = "com.zongke.downloadservice.DownLoadRemoteService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.zongke.downloadservice.DownLoadRemoteService interface,
 * generating a proxy if needed.
 */
public static DownLoadRemoteService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof DownLoadRemoteService))) {
return ((DownLoadRemoteService)iin);
}
return new Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_startDownLoadTask:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
android.support.v4.os.ResultReceiver _arg2;
if ((0!=data.readInt())) {
_arg2 = android.support.v4.os.ResultReceiver.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.startDownLoadTask(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_stopDownloadTask:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
this.stopDownloadTask(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_againStartDownloadTask:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
android.support.v4.os.ResultReceiver _arg2;
if ((0!=data.readInt())) {
_arg2 = android.support.v4.os.ResultReceiver.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.againStartDownloadTask(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements DownLoadRemoteService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * 开启一个下载任务
     */
@Override public void startDownLoadTask(String downloadUrl, String filePath, android.support.v4.os.ResultReceiver resultReceiver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(downloadUrl);
_data.writeString(filePath);
if ((resultReceiver!=null)) {
_data.writeInt(1);
resultReceiver.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_startDownLoadTask, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
      * 停止一个下载任务
      */
@Override public void stopDownloadTask(String downloadUrl) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(downloadUrl);
mRemote.transact(Stub.TRANSACTION_stopDownloadTask, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 删除旧的文件，从新下载
     */
@Override public void againStartDownloadTask(String downloadUrl, String filePath, android.support.v4.os.ResultReceiver resultReceiver) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(downloadUrl);
_data.writeString(filePath);
if ((resultReceiver!=null)) {
_data.writeInt(1);
resultReceiver.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_againStartDownloadTask, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_startDownLoadTask = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stopDownloadTask = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_againStartDownloadTask = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * 开启一个下载任务
     */
public void startDownLoadTask(String downloadUrl, String filePath, android.support.v4.os.ResultReceiver resultReceiver) throws android.os.RemoteException;
/**
      * 停止一个下载任务
      */
public void stopDownloadTask(String downloadUrl) throws android.os.RemoteException;
/**
     * 删除旧的文件，从新下载
     */
public void againStartDownloadTask(String downloadUrl, String filePath, android.support.v4.os.ResultReceiver resultReceiver) throws android.os.RemoteException;
}

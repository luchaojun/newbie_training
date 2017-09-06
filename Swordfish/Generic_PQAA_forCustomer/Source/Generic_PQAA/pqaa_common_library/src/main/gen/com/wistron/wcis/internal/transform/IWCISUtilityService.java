/*___Generated_by_IDEA___*/

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/luchaojun/workdocument/Swordfish/Generic_PQAA_forCustomer/Source/Generic_PQAA/pqaa_common_library/src/main/java/com/wistron/wcis/internal/transform/IWCISUtilityService.aidl
 */
package com.wistron.wcis.internal.transform;
public interface IWCISUtilityService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.wistron.wcis.internal.transform.IWCISUtilityService
{
private static final java.lang.String DESCRIPTOR = "com.wistron.wcis.internal.transform.IWCISUtilityService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.wistron.wcis.internal.transform.IWCISUtilityService interface,
 * generating a proxy if needed.
 */
public static com.wistron.wcis.internal.transform.IWCISUtilityService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.wistron.wcis.internal.transform.IWCISUtilityService))) {
return ((com.wistron.wcis.internal.transform.IWCISUtilityService)iin);
}
return new com.wistron.wcis.internal.transform.IWCISUtilityService.Stub.Proxy(obj);
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
case TRANSACTION_readConfigFile:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.Map _result = this.readConfigFile(_arg0);
reply.writeNoException();
reply.writeMap(_result);
return true;
}
case TRANSACTION_readGroupConfigFile:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List _result = this.readGroupConfigFile(_arg0);
reply.writeNoException();
reply.writeList(_result);
return true;
}
case TRANSACTION_writeLog:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
this.writeLog(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_deleteLog:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.deleteLog(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_eraseSDCard:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.eraseSDCard(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.wistron.wcis.internal.transform.IWCISUtilityService
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
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public java.util.Map readConfigFile(java.lang.String path) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.Map _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
mRemote.transact(Stub.TRANSACTION_readConfigFile, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readHashMap(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List readGroupConfigFile(java.lang.String path) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
mRemote.transact(Stub.TRANSACTION_readGroupConfigFile, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readArrayList(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void writeLog(int type, java.lang.String path, java.lang.String content) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
_data.writeString(path);
_data.writeString(content);
mRemote.transact(Stub.TRANSACTION_writeLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void deleteLog(java.lang.String path) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
mRemote.transact(Stub.TRANSACTION_deleteLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void eraseSDCard(java.lang.String path) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
mRemote.transact(Stub.TRANSACTION_eraseSDCard, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_readConfigFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_readGroupConfigFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_writeLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_deleteLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_eraseSDCard = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public java.util.Map readConfigFile(java.lang.String path) throws android.os.RemoteException;
public java.util.List readGroupConfigFile(java.lang.String path) throws android.os.RemoteException;
public void writeLog(int type, java.lang.String path, java.lang.String content) throws android.os.RemoteException;
public void deleteLog(java.lang.String path) throws android.os.RemoteException;
public void eraseSDCard(java.lang.String path) throws android.os.RemoteException;
}

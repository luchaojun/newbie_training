/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/payne/Payne/swordfish/Generic_PQAA_forCustomer/Source/Generic_PQAA/generic_pqaa/src/main/aidl/com/wistron/generic/wifi/IWirelessService.aidl
 */
package com.wistron.generic.wifi;
public interface IWirelessService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.wistron.generic.wifi.IWirelessService
{
private static final java.lang.String DESCRIPTOR = "com.wistron.generic.wifi.IWirelessService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.wistron.generic.wifi.IWirelessService interface,
 * generating a proxy if needed.
 */
public static com.wistron.generic.wifi.IWirelessService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.wistron.generic.wifi.IWirelessService))) {
return ((com.wistron.generic.wifi.IWirelessService)iin);
}
return new com.wistron.generic.wifi.IWirelessService.Stub.Proxy(obj);
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
case TRANSACTION_isWifiOn:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isWifiOn();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isWifiCanUse:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isWifiCanUse();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getMacAddress:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getMacAddress();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_disCurrentConnect:
{
data.enforceInterface(DESCRIPTOR);
this.disCurrentConnect();
reply.writeNoException();
return true;
}
case TRANSACTION_openWifi:
{
data.enforceInterface(DESCRIPTOR);
this.openWifi();
reply.writeNoException();
return true;
}
case TRANSACTION_getWifiConnectState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getWifiConnectState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isNeedReConnectAp:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isNeedReConnectAp();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_connectAp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
java.lang.String _arg2;
_arg2 = data.readString();
this.connectAp(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getWifiEnableState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getWifiEnableState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getConnectApSSID:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getConnectApSSID();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_ping:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
this.ping(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getWifiRssi:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getWifiRssi();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_closeWifi:
{
data.enforceInterface(DESCRIPTOR);
this.closeWifi();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.wistron.generic.wifi.IWirelessService
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
@Override public boolean isWifiOn() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isWifiOn, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isWifiCanUse() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isWifiCanUse, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getMacAddress() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMacAddress, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void disCurrentConnect() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_disCurrentConnect, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void openWifi() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_openWifi, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getWifiConnectState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getWifiConnectState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isNeedReConnectAp() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isNeedReConnectAp, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void connectAp(java.lang.String ssid, int security, java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(ssid);
_data.writeInt(security);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_connectAp, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getWifiEnableState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getWifiEnableState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getConnectApSSID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getConnectApSSID, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void ping(java.lang.String address, int count, int interval) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(address);
_data.writeInt(count);
_data.writeInt(interval);
mRemote.transact(Stub.TRANSACTION_ping, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getWifiRssi() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getWifiRssi, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void closeWifi() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_closeWifi, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_isWifiOn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_isWifiCanUse = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getMacAddress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_disCurrentConnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_openWifi = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getWifiConnectState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_isNeedReConnectAp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_connectAp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getWifiEnableState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getConnectApSSID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_ping = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getWifiRssi = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_closeWifi = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
}
public boolean isWifiOn() throws android.os.RemoteException;
public boolean isWifiCanUse() throws android.os.RemoteException;
public java.lang.String getMacAddress() throws android.os.RemoteException;
public void disCurrentConnect() throws android.os.RemoteException;
public void openWifi() throws android.os.RemoteException;
public int getWifiConnectState() throws android.os.RemoteException;
public boolean isNeedReConnectAp() throws android.os.RemoteException;
public void connectAp(java.lang.String ssid, int security, java.lang.String password) throws android.os.RemoteException;
public int getWifiEnableState() throws android.os.RemoteException;
public java.lang.String getConnectApSSID() throws android.os.RemoteException;
public void ping(java.lang.String address, int count, int interval) throws android.os.RemoteException;
public int getWifiRssi() throws android.os.RemoteException;
public void closeWifi() throws android.os.RemoteException;
}

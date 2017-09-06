/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/payne/Payne/swordfish/Generic_PQAA_forCustomer/Source/Generic_PQAA/generic_pqaa/src/main/aidl/com/wistron/generic/autotest/internal/bluetooth/IBluetoothService.aidl
 */
package com.wistron.generic.autotest.internal.bluetooth;
public interface IBluetoothService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.wistron.generic.autotest.internal.bluetooth.IBluetoothService
{
private static final java.lang.String DESCRIPTOR = "com.wistron.generic.autotest.internal.bluetooth.IBluetoothService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.wistron.generic.autotest.internal.bluetooth.IBluetoothService interface,
 * generating a proxy if needed.
 */
public static com.wistron.generic.autotest.internal.bluetooth.IBluetoothService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.wistron.generic.autotest.internal.bluetooth.IBluetoothService))) {
return ((com.wistron.generic.autotest.internal.bluetooth.IBluetoothService)iin);
}
return new com.wistron.generic.autotest.internal.bluetooth.IBluetoothService.Stub.Proxy(obj);
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
case TRANSACTION_isBluetoothCanUse:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isBluetoothCanUse();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isBluetoothEnable:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isBluetoothEnable();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getBluetoothAddress:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getBluetoothAddress();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_isDiscovering:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isDiscovering();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.wistron.generic.autotest.internal.bluetooth.IBluetoothService
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
@Override public boolean isBluetoothCanUse() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isBluetoothCanUse, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isBluetoothEnable() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isBluetoothEnable, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getBluetoothAddress() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBluetoothAddress, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isDiscovering() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isDiscovering, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_isBluetoothCanUse = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_isBluetoothEnable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getBluetoothAddress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_isDiscovering = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public boolean isBluetoothCanUse() throws android.os.RemoteException;
public boolean isBluetoothEnable() throws android.os.RemoteException;
public java.lang.String getBluetoothAddress() throws android.os.RemoteException;
public boolean isDiscovering() throws android.os.RemoteException;
}

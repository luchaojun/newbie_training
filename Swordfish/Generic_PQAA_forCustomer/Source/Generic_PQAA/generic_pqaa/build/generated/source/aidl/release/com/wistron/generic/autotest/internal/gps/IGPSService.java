/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/payne/Payne/swordfish/Generic_PQAA_forCustomer/Source/Generic_PQAA/generic_pqaa/src/main/aidl/com/wistron/generic/autotest/internal/gps/IGPSService.aidl
 */
package com.wistron.generic.autotest.internal.gps;
public interface IGPSService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.wistron.generic.autotest.internal.gps.IGPSService
{
private static final java.lang.String DESCRIPTOR = "com.wistron.generic.autotest.internal.gps.IGPSService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.wistron.generic.autotest.internal.gps.IGPSService interface,
 * generating a proxy if needed.
 */
public static com.wistron.generic.autotest.internal.gps.IGPSService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.wistron.generic.autotest.internal.gps.IGPSService))) {
return ((com.wistron.generic.autotest.internal.gps.IGPSService)iin);
}
return new com.wistron.generic.autotest.internal.gps.IGPSService.Stub.Proxy(obj);
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
case TRANSACTION_isHasGPSModule:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isHasGPSModule();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.wistron.generic.autotest.internal.gps.IGPSService
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
@Override public boolean isHasGPSModule() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isHasGPSModule, _data, _reply, 0);
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
static final int TRANSACTION_isHasGPSModule = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public boolean isHasGPSModule() throws android.os.RemoteException;
}

package com.wistron.generic.gps;


import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.wistron.generic.internal.gps.IGPSService;
import com.wistron.pqaa_common.jar.autotest.WisGPS_Service;
import com.wistron.pqaa_common.jar.global.WisGPS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GPSService extends Service {
    private LocationManager lmManager;
    private GpsStatus.Listener mSatellitesListener;
    boolean isHasGPSModule = false;

    IGPSService.Stub stub = new IGPSService.Stub() {

        @Override
        public boolean isHasGPSModule() throws RemoteException {
            // TODO Auto-generated method stub
            if (Looper.myLooper() == Looper.getMainLooper()) {
                detectModule();
            } else {
                try {
                    Looper.prepare();
                    final Handler quitHandler = new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            // TODO Auto-generated method stub
                            super.handleMessage(msg);
                            Looper.myLooper().quit();
                        }

                    };
                    detectModule();
                    quitHandler.sendEmptyMessage(0);
                    Looper.loop();
                } catch (RuntimeException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    detectModule();
                }
            }
            return isHasGPSModule;
        }

        private void detectModule() {
            WisGPS mGpsHandler = new WisGPS(GPSService.this);
            isHasGPSModule = mGpsHandler.isHasGPSModule();
        }
    };

    public GPSService() {

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        lmManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.i("GPSService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.i("GPSService", "onStartCommand");
        int action = intent.getIntExtra(WisGPS_Service.EXTRA_GPS_SERVICE_DO_ACTION, WisGPS_Service.GPS_SERVICE_OPEN_GPS);
        switch (action) {
            case WisGPS_Service.GPS_SERVICE_OPEN_GPS:
                openGPS();
                break;
            case WisGPS_Service.GPS_SERVICE_SEARCH_SATELLITE:
                startSearchSatellite();
                break;
            case WisGPS_Service.GPS_SERVICE_SEARCH_LOCATION:
                startSearchLocation();
                break;
            case WisGPS_Service.GPS_SERVICE_CLOSE_GPS:
                closeGPS();
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void openGPS() {
        // TODO Auto-generated method stub
        //Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
        Criteria cCriteria = new Criteria();
        cCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        cCriteria.setAltitudeRequired(false);
        cCriteria.setBearingRequired(false);
        cCriteria.setCostAllowed(true);
        cCriteria.setPowerRequirement(Criteria.POWER_LOW);
//		String sProvider = lmManager.getBestProvider(cCriteria, true);
//		lmManager.requestLocationUpdates(sProvider, 0, 0.0f, mLocationListener);
//		lmManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1f, mLocationListener);
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission
                        .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[]
            // permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the
            // documentation
            // for ActivityCompat#requestPermissions for more details.

            lmManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1f, mLocationListener);
            Log.i("W", "GPS service request location listener");
            return;
        } else {*/
            lmManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1f, mLocationListener);
            Log.i("W", "GPS service has location listener");
        /*}*/
    }

    public boolean isHasGPSModule() {
        // TODO Auto-generated method stub
        if (lmManager != null) {
            List<String> mProviders = lmManager.getAllProviders();
            if (mProviders != null && mProviders.contains(LocationManager.GPS_PROVIDER)) {
                return true;
            }
        }
        return false;
    }

    public void closeGPS() {
        // TODO Auto-generated method stub
//		lmManager.removeUpdates(mLocationListener);
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission
                        .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[]
            // permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the
            // documentation
            // for ActivityCompat#requestPermissions for more details.
            lmManager.removeUpdates(mLocationListener);
            Log.i("W", "GPS service remove updates location listener");
            return;
        } else {*/
            lmManager.removeUpdates(mLocationListener);
            Log.i("W", "GPS service has remove location listener");
        /*}*/
        if (mSatellitesListener != null) {
            lmManager.removeGpsStatusListener(mSatellitesListener);
        }
        //Settings.Secure.setLocationProviderEnabled(getContentResolver(),LocationManager.GPS_PROVIDER, false);
    }

    public void startSearchSatellite() {
        // TODO Auto-generated method stub
        mSatellitesListener = new GpsStatus.Listener() {

            public void onGpsStatusChanged(int event) {
                // TODO Auto-generated method stub
                if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                    // first location
                } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    Log.i("GPSService", "satellite has changed......");
                    ArrayList<Float> mSatelliteList = new ArrayList<Float>();
                    GpsStatus mGpsStatus = lmManager.getGpsStatus(null);
                    Iterator<GpsSatellite> mIterator = mGpsStatus.getSatellites().iterator();
                    while (mIterator.hasNext()) {
                        GpsSatellite mSatellite = mIterator.next();
                        mSatelliteList.add(mSatellite.getSnr());
                    }
                    Intent intent = new Intent(WisGPS_Service.ACTION_GPS_STATE_CHANGED);
                    intent.putExtra(WisGPS_Service.EXTRA_GPS_STATE, WisGPS_Service.GPS_STATE_SATELLITE_CHANGED);
                    intent.putExtra(WisGPS_Service.EXTRA_GPS_SATELLITE_LIST, mSatelliteList);
                    sendBroadcast(intent);
                } else if (event == GpsStatus.GPS_EVENT_STARTED) {
                    //
                } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
                    //
                }
            }
        };
        lmManager.addGpsStatusListener(mSatellitesListener);
    }

    public void startSearchLocation() {
        // TODO Auto-generated method stub

    }

    private LocationListener mLocationListener = new LocationListener() {

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            Intent intent = new Intent(WisGPS_Service.ACTION_GPS_STATE_CHANGED);
            intent.putExtra(WisGPS_Service.EXTRA_GPS_STATE, WisGPS_Service.GPS_STATE_LOCATION_CHANGED);
            intent.putExtra(WisGPS_Service.EXTRA_GPS_LOCATION_DATA, location.getLongitude() + ":" + location.getLatitude());
            sendBroadcast(intent);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return stub;
    }
}

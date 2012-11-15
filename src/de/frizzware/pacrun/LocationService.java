package de.frizzware.pacrun;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class LocationService implements LocationListener, SensorEventListener {
	private Context mContext;
	private LocationManager mLocationMgr;
	private  ArrayList<Location> mLocations = new ArrayList<Location>();
	private UpdateHandler mHandler;
	private Location mCurrentLocation;
	
	/**
	 * Overall distance
	 */
	private double distance = 0;
	private Date startDate;
	private boolean running = false;
    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
	
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 5 seconds
	
	public LocationService(Context ctx, UpdateHandler handler) {
		mContext = ctx;
		mHandler = handler;
		mLocationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void onLocationChanged(Location location) {
		if (isBetterLocation(location, mCurrentLocation))
				mCurrentLocation = location;

		mLocations.add(location);
		if (mLocations.size() > 2) {
			Location last = mLocations.get(mLocations.size() - 2);
			distance += last.distanceTo(location);
		}
		if (mHandler != null)
			mHandler.onChange();
	}
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	float[] inR = new float[16];
	float[] I = new float[16];
	float[] gravity = new float[3];
	float[] geomag = new float[3];
	float[] orientVals = new float[3];
	double azimuth = 0;
	double pitch = 0;
	double roll = 0;
	public void onSensorChanged(SensorEvent sensorEvent) {
	    // If the sensor data is unreliable return
	    if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
	        return;

	    // Gets the value of the sensor that has been changed
	    switch (sensorEvent.sensor.getType()) {  
	        case Sensor.TYPE_ACCELEROMETER:
	            gravity = sensorEvent.values.clone();
	            break;
	        case Sensor.TYPE_MAGNETIC_FIELD:
	            geomag = sensorEvent.values.clone();
	            break;
	    }

	    // If gravity and geomag have values then find rotation matrix
	    if (gravity != null && geomag != null) {

	        // checks that the rotation matrix is found
	        boolean success = SensorManager.getRotationMatrix(inR, I,
	                                                          gravity, geomag);
	        if (success) {
	            SensorManager.getOrientation(inR, orientVals);
	            azimuth = Math.toDegrees(orientVals[0]);
	            pitch = Math.toDegrees(orientVals[1]);
	            roll = Math.toDegrees(orientVals[2]);
	            
	            mHandler.onChange();
	        }
	    }
	}
	
	public void start() {
        // getting GPS status
		isGPSEnabled = mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled) {
			running = true;
			startDate = new Date();
			mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		} else {
			showSettingsAlert();
		}
			
		isNetworkEnabled = mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (isNetworkEnabled) {
			mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		
		this.onLocationChanged(mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
		
		SensorManager sm = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
		// Register this class as a listener for the accelerometer sensor
		sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		                    SensorManager.SENSOR_DELAY_GAME);
		// ...and the orientation sensor
		sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
		                    SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void stop() {
		running = false;
		mLocationMgr.removeUpdates(this);
	}
	
	public float getSpeed() {
		if (!mLocations.isEmpty()) {
			Location loc = mLocations.get(mLocations.size()-1);
			if(loc.hasSpeed())
				return loc.getSpeed();
			else if (mLocations.size() > 2) {
				Location last1 = mLocations.get(mLocations.size() - 1);
				Location last2 = mLocations.get(mLocations.size() - 2);
				return last1.distanceTo(last2)/MIN_TIME_BW_UPDATES;
			}
		}
		return 8;
	}
	
	public double getAzimuth() {
		return azimuth;
	}
	
	public ArrayList<Location> getLocations() {
		return mLocations;
	}
	
	public Location getCurrentLocation() {
		return mCurrentLocation;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getDistance() {
		return distance;
	}
	
	public long getDuration() {
		return new Date().getTime() - startDate.getTime();
	}
	
	public boolean getRunning() {
		return running;
	}
	
	public double getAverageSpeed() {
		return getDistance()/getDuration();
	}
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > MIN_TIME_BW_UPDATES;
	    boolean isSignificantlyOlder = timeDelta < -MIN_TIME_BW_UPDATES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
 
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
    
    interface UpdateHandler {
    	public void onChange();
    }
}

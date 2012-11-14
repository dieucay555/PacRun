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
	private LocationHandler mHandler;
	
	/**
	 * Overall distance
	 */
	private double distance = 0;
	private Date startDate;
	private boolean running = false;
    // flag for GPS status
    boolean isGPSEnabled = false;
	
	 // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // 5 seconds
	
	public LocationService(Context ctx, LocationHandler handler) {
		mContext = ctx;
		mHandler = handler;
		mLocationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		
	}
	
	public void onLocationChanged(Location location) {
		mLocations.add(location);
		if (mLocations.size() > 2) {
			Location last = mLocations.get(mLocations.size() - 2);
			distance += last.distanceTo(location);
		}
		if (mHandler != null)
			mHandler.onLocationChanged(location);
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
	        }
	    }
	}
	
	public void start() {
        // getting GPS status
		isGPSEnabled = mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled) {
			running = true;
			startDate = new Date();
			mLocationMgr.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			this.onLocationChanged(mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
			
			SensorManager sm = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			// Register this class as a listener for the accelerometer sensor
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
			                    SensorManager.SENSOR_DELAY_NORMAL);
			// ...and the orientation sensor
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
			                    SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			showSettingsAlert();
		}
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
		return -1;
	}
	
	public double getAzimuth() {
		return azimuth;
	}
	
	public ArrayList<Location> getLocations() {
		return mLocations;
	}
	
	public Location getCurrentLocation() {
		if (!mLocations.isEmpty())
			return mLocations.get(mLocations.size()-1);
		return null;
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
    
    interface LocationHandler {
    	public void onLocationChanged(Location location);
    }
}

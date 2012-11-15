package de.frizzware.pacrun;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class GameActivity extends MapActivity implements LocationService.UpdateHandler{
	LocationService mLocationService;
	MapView mMap;
	WayOverlay mWayOverlay = new WayOverlay();
	LocationOverlay mPacmanOverlay;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        
        // Displaying Zooming controls
        mMap = (MapView) findViewById(R.id.mapview);
        mMap.setBuiltInZoomControls(false);
        mMap.setStreetView(true); // Street View
        mMap.getController().setZoom(19);
        
        Bitmap pacman = BitmapFactory.decodeResource( getResources(), R.drawable.pacman);
        mPacmanOverlay = new LocationOverlay(this, mMap, pacman);
        mMap.getOverlays().add(mPacmanOverlay);
        mMap.getOverlays().add(mWayOverlay);
        
        mLocationService = new LocationService(this, this);
        mLocationService.start();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected void onResume() {
        mPacmanOverlay.enableCompass();
        mPacmanOverlay.enableMyLocation();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mPacmanOverlay.disableCompass();
		mPacmanOverlay.disableMyLocation();
	}

	public void onChange() {
		Location l = mLocationService.getCurrentLocation();
		if (l != null) {
	        GeoPoint point = new GeoPoint((int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6));
	        MapController controller = mMap.getController();
	        controller.animateTo(point);
	        //mMap.postInvalidate();
	        mPacmanOverlay.setOrientation((float)mLocationService.getAzimuth());
		}
	}
	
	class WayOverlay extends Overlay{

	    public WayOverlay(){
	    }   

	    public void draw(Canvas canvas, MapView mapv, boolean shadow){
	        super.draw(canvas, mapv, shadow);
	        if (shadow)
	        	return;

	        Paint   mPaint = new Paint();
	        mPaint.setDither(true);
	        mPaint.setColor(Color.BLUE);
	        mPaint.setStyle(Paint.Style.STROKE);
	        mPaint.setStrokeJoin(Paint.Join.ROUND);
	        mPaint.setStrokeCap(Paint.Cap.ROUND);
	        mPaint.setStrokeWidth(5);
	        
	        Path path = new Path();
	        Projection projection = mapv.getProjection();
	        ArrayList<Location> locs = mLocationService.getLocations();
	        if (locs.size() < 2)
	        	return;
	        	        
	        Location l = locs.get(0);
	        Point p = new Point();
	        GeoPoint gPoint = new GeoPoint((int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6));
	        projection.toPixels(gPoint, p);
	        path.moveTo(p.x, p.y);
	        for(int i = 1; i < locs.size(); i++) {
	        	if (l.distanceTo(locs.get(i)) < 10)
	        		continue;
	        	
	        	l = locs.get(i);
	        	gPoint = new GeoPoint((int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6));
	        	projection.toPixels(gPoint, p);
	        	path.lineTo(p.x,p.y);
	        }

	        canvas.drawPath(path, mPaint);
	    }
	}
}

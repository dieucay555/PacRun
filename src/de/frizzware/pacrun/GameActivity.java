package de.frizzware.pacrun;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

import de.frizzware.pacrun.MonsterManager.Monster;

public class GameActivity extends MapActivity implements LocationService.UpdateHandler{
	LocationService mLocationService;
	MapView mMap;
	WayOverlay mWayOverlay = new WayOverlay();
	UserLocationOverlay mPacmanOverlay;
	MonsterManager mMManager;
	Timer timer = new Timer();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        mLocationService = new LocationService(this, this);
        
        // Displaying Zooming controls
        mMap = (MapView) findViewById(R.id.mapview);
        mMap.setBuiltInZoomControls(false);
        mMap.setStreetView(true); // Street View
        mMap.getController().setZoom(19);
      
        List<Overlay> mapOverlays = mMap.getOverlays();
        
        Bitmap pacman = BitmapFactory.decodeResource( getResources(), R.drawable.pacman);
        mPacmanOverlay = new UserLocationOverlay(this, mMap, pacman);
        mapOverlays.add(mPacmanOverlay);
        
        Drawable d = getResources().getDrawable(R.drawable.clyde);
		Monster m = new Monster(d, new GeoPoint(5077825, 6060222));
		
		mMManager = new MonsterManager(this);
        mapOverlays.addAll(mMManager.getMonsters());
        
        timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mMManager.moveMonsters(mPacmanOverlay.getMyLocation());
				mMap.postInvalidate();
			}
		}, 1000, 1000);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}
	
	@Override
	protected void onResume() {
		mLocationService.start();
        mPacmanOverlay.enableCompass();
        mPacmanOverlay.enableMyLocation();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mLocationService.stop();
		mPacmanOverlay.disableCompass();
		mPacmanOverlay.disableMyLocation();
	}

	public void onChange() {
		Location l = mLocationService.getCurrentLocation();
		if (l != null) {
			
			
	        GeoPoint point = new GeoPoint((int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6));
	        MapController controller = mMap.getController();
	        controller.animateTo(point);
	        
	        //mPacmanOverlay.setOrientation((float)mLocationService.getAzimuth());
	        //mMap.postInvalidate();
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

package de.frizzware.pacrun;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.util.FloatMath;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MonsterManager {
	private MediaPlayer mPlayerDies;
	private ArrayList<Monster> monsters = new ArrayList<Monster>();
	private Context mContext;
	
	public MonsterManager(Context ctx) {
		mContext = ctx;
		generateMonsters();
		
		mPlayerDies = MediaPlayer.create(mContext, R.raw.pacman_dies);
	}
	
	private void generateMonsters() {		
		Drawable d = mContext.getResources().getDrawable(R.drawable.clyde);
		Monster m = new Monster(d, new GeoPoint((int)(50.77825*1E6), (int)(6.060222*1E6)));
		monsters.add(m);
		
		d = mContext.getResources().getDrawable(R.drawable.inky);
		m = new Monster(d, new GeoPoint((int)(50.77825*1E6), (int)(6.060222*1E6)));
		monsters.add(m);
		
		d = mContext.getResources().getDrawable(R.drawable.pinky);
		m = new Monster(d, new GeoPoint((int)(50.77825*1E6), (int)(6.060222*1E6)));
		monsters.add(m);
	}
	
	public List<Monster> getMonsters() {
		return monsters;
	}
	
	private void groupAround(Location current, Monster m, int i) {
		int latitude = (int)(current.getLatitude()*1E6);
		int longitude = (int) (current.getLongitude()*1E6);

		int lat = latitude + (int)Math.sin(Math.PI/2*i)*1000;
		int log = longitude + (int)Math.cos(Math.PI/2*i)*1000;
		m.setGeoPoint(new GeoPoint(lat, log));
	}
	
	public void moveMonsters(Location current) {
		int i = 0;
		for(Monster m : monsters) {
			if(m.distanceTo(current) > 50.) {
				groupAround(current, m, i);
				mPlayerDies.seekTo(0);
				//mPlayerDies.start();
			} else {
				
			}
			i++;
		}
	}
	
	public static class Monster extends Overlay {
		Drawable mDrawable;
		GeoPoint mGeoPoint;
		
		public Monster(Drawable drawable, GeoPoint geoPoint) {
			mDrawable = drawable;
			mGeoPoint = geoPoint;
		}
		
		@Override
		public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			
			Projection pr = mapView.getProjection();
			Point point = pr.toPixels(mGeoPoint, null);
			mDrawable.setBounds(new Rect(point.x-20, point.y-35, point.x+20, point.y+35));
			mDrawable.draw(canvas);
		}
		
		public void setGeoPoint(GeoPoint p) {
			mGeoPoint = p;
		}
		
		public GeoPoint getGeoPoint() {
			return mGeoPoint;
		}
		
		public float distanceTo(Location l) {
			float results[] = new float[3];
			Location.distanceBetween(mGeoPoint.getLatitudeE6()/1E6, mGeoPoint.getLongitudeE6()/1E6,
					l.getLatitude(), l.getLongitude(), results);
			return results[0];
		}
	}
}

package de.frizzware.pacrun;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.PowerManager.WakeLock;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MonsterManager {
	private MediaPlayer mPlayerDies;
	private MediaPlayer mEatingGhost;
	private MediaPlayer mWakawaka;
	private ArrayList<Monster> monsters = new ArrayList<Monster>();
	private Context mContext;
	
	public MonsterManager(Context ctx) {
		mContext = ctx;
		generateMonsters();
		
		mPlayerDies = MediaPlayer.create(mContext, R.raw.pacman_dies);
		mEatingGhost = MediaPlayer.create(mContext, R.raw.eating_ghost);
		mWakawaka = MediaPlayer.create(mContext, R.raw.wakawaka);
	}
	
	public void destroy() {
		mPlayerDies.release();
		mEatingGhost.release();
		mWakawaka.release();
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
		int lat = (int) (current.getLatitude()*1E6 + 500 + Math.sin(Math.PI/4*i)*1000);
		int log = (int) (current.getLongitude()*1E6 + 500 + Math.cos(Math.PI/4*i)*1000);
		m.setGeoPoint(new GeoPoint(lat, log));
	}
	
	private void stepTowards(Location current, Monster m, double speed) {
		GeoPoint p = m.getGeoPoint();
		
		int lat = (int) (p.getLatitudeE6() + 40*Math.signum(current.getLatitude()*1E6 - p.getLatitudeE6()));
		int log = (int) (p.getLongitudeE6() + 40*Math.signum(current.getLongitude()*1E6 - p.getLongitudeE6()));
		m.setGeoPoint(new GeoPoint(lat, log));
	}
	
	
	public void init(Location current) {
		for (int i = 0; i < monsters.size(); i++) {
			Monster m = monsters.get(i);
			groupAround(current, m, i);
		}
	}
	
	public boolean moveMonsters(Location current, double speed) {
		int i = 0;
		for(Monster m : monsters) {
			if(m.distanceTo(current) >= 200.) {
				mEatingGhost.start();
				groupAround(current, m, i);
			} else {
				stepTowards(current, m, speed);
				if(m.distanceTo(current) <= 10) {
					mPlayerDies.start();
					return false;
				}
			}
			i++;
		}
		mWakawaka.start();
		return true;
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
		
		public float distanceTo(Location point) {
			float results[] = new float[3];
			Location.distanceBetween(mGeoPoint.getLatitudeE6()/1E6, mGeoPoint.getLongitudeE6()/1E6,
					point.getLatitude(), point.getLongitude(), results);
			return results[0];
		}
	}
}

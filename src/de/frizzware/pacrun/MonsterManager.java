package de.frizzware.pacrun;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MonsterManager {
	private MediaPlayer mPlayerDies;
	private MediaPlayer mEatingGhost;
	private ArrayList<Monster> monsters = new ArrayList<Monster>();
	private Context mContext;
	
	public MonsterManager(Context ctx) {
		mContext = ctx;
		generateMonsters();
		
		mPlayerDies = MediaPlayer.create(mContext, R.raw.pacman_dies);
		mEatingGhost = MediaPlayer.create(mContext, R.raw.eating_ghost);
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
	
	private void groupAround(GeoPoint current, Monster m, int i) {
		int lat = current.getLatitudeE6() + (int)Math.sin(Math.PI/2*i)*1000;
		int log = current.getLongitudeE6() + (int)Math.cos(Math.PI/2*i)*1000;
		m.setGeoPoint(new GeoPoint(lat, log));
	}
	
	private void stepTowards(GeoPoint current, Monster m, double speed) {
		int lat = (int) (current.getLatitudeE6() - (current.getLatitudeE6() - m.getGeoPoint().getLatitudeE6())/1000);
		int log = (int) (current.getLongitudeE6() - (current.getLongitudeE6() - m.getGeoPoint().getLongitudeE6())/1000);
		m.setGeoPoint(new GeoPoint(lat, log));
	}
	
	
	public void init(GeoPoint current) {
		for (int i = 0; i < monsters.size(); i++) {
			Monster m = monsters.get(i);
			groupAround(current, m, i);
		}
	}
	
	public boolean moveMonsters(GeoPoint current, double speed) {
		int i = 0;
		for(Monster m : monsters) {
			if(m.distanceTo(current) >= 150.) {
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
		
		public float distanceTo(GeoPoint point) {
			float results[] = new float[3];
			Location.distanceBetween(mGeoPoint.getLatitudeE6()/1E6, mGeoPoint.getLongitudeE6()/1E6,
					point.getLatitudeE6()/1E6, point.getLongitudeE6()/1E6, results);
			return results[0];
		}
	}
}

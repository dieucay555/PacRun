package de.frizzware.pacrun;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class GameManager {
	private MediaPlayer mPlayerDies;
	private MediaPlayer mEatingGhost;
	private MediaPlayer mWakawaka;
	private ArrayList<GameObj> monsters = new ArrayList<GameObj>();
	private GameActivity mContext;
	private int lifes = 3;
	private ArrayList<GameObj> stuff = new ArrayList<GameManager.GameObj>();
	
	public GameManager(GameActivity ctx) {
		mContext = ctx;
		
		mPlayerDies = MediaPlayer.create(mContext, R.raw.pacman_dies);
		mEatingGhost = MediaPlayer.create(mContext, R.raw.eating_ghost);
		mWakawaka = MediaPlayer.create(mContext, R.raw.wakawaka);
	}
	
	public void init(Location current) {
		mContext.mMap.getOverlays().addAll(getMonsters());
		for (int i = 0; i < monsters.size(); i++) {
			GameObj m = monsters.get(i);
			groupAround(current, m, i);
		}
		//makeStuff(current);
		//mContext.mMap.getOverlays().addAll(stuff);
	}
	
	public void destroy() {
		mPlayerDies.release();
		mEatingGhost.release();
		mWakawaka.release();
	}
	
	private void makeStuff(Location around) {
		Random rand = new Random();// good enough
		int left = (int) (around.getLatitude()*1E6 - 25000);
		int top = (int) (around.getLongitude()*1E6 - 25000);
		
		Drawable coin = mContext.getResources().getDrawable(R.drawable.dot);
		
		for (int i = 0; i < 50; i++) {
			for (int x = 0; x < 50; x++) {
				GeoPoint p = new GeoPoint(left + i*100, top + x*100);
				GameObj coinObj = new GameObj(coin, p, GameTypes.COIN);
				stuff.add(coinObj);
			}
		}
	}

	public List<GameObj> getMonsters() {
		if (monsters.isEmpty()) {
			Drawable d = mContext.getResources().getDrawable(R.drawable.clyde);
			GameObj m = new GameObj(d, new GeoPoint((int)(50.77825*1E6), (int)(6.060222*1E6)), GameTypes.MONSTER);
			monsters.add(m);
			
			d = mContext.getResources().getDrawable(R.drawable.inky);
			m = new GameObj(d, new GeoPoint((int)(50.77825*1E6), (int)(6.060222*1E6)), GameTypes.MONSTER);
			monsters.add(m);
			
			d = mContext.getResources().getDrawable(R.drawable.pinky);
			m = new GameObj(d, new GeoPoint((int)(50.77825*1E6), (int)(6.060222*1E6)), GameTypes.MONSTER);
			monsters.add(m);
		}
		return monsters;
	}
	
	private void groupAround(Location current, GameObj m, int i) {
		int lat = (int) (current.getLatitude()*1E6 + 500 + Math.sin(Math.PI/4*i)*1000);
		int log = (int) (current.getLongitude()*1E6 + 500 + Math.cos(Math.PI/4*i)*1000);
		m.setGeoPoint(new GeoPoint(lat, log));
	}
	
	private void stepTowards(Location current, GameObj m, double speed) {
		GeoPoint p = m.getGeoPoint();
		
		int lat = (int) (p.getLatitudeE6() + 35*Math.signum(current.getLatitude()*1E6 - p.getLatitudeE6()));
		int log = (int) (p.getLongitudeE6() + 35*Math.signum(current.getLongitude()*1E6 - p.getLongitudeE6()));
		m.setGeoPoint(new GeoPoint(lat, log));
	}
	
	public boolean moveMonsters(Location current, double speed) {
		int i = 0;
		for(GameObj m : monsters) {
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
	
	interface GameTypes {
		final static int MONSTER = 1;
		final static int LIFE = 2;
		final static int COIN = 3;
	}
	
	public static class GameObj extends Overlay {
		int mType;
		Drawable mDrawable;
		GeoPoint mGeoPoint;
		
		public GameObj(Drawable drawable, GeoPoint geoPoint, int type) {
			mDrawable = drawable;
			mGeoPoint = geoPoint;
			mType = type;
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
		
		public int getType() {
			return mType;
		}
	}
}

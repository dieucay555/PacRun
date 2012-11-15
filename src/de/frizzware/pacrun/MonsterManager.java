package de.frizzware.pacrun;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MonsterManager extends ItemizedOverlay<OverlayItem> {
	private final LocationService mLocationService;
	private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
	private Random rand = new Random();
	
	public MonsterManager(Drawable drawable, LocationService locationSevice) {
		super(drawable);		
		mLocationService = locationSevice;
	}
	
	public void generateMonsters() {
		for (int i = 1; i <= 3; i++) {
			OverlayItem overlayitem = new OverlayItem(randomPoint(), "Monster " + i, "A Monster");
			addOverlay(overlayitem);
		}
		populate();
	}
	
	public GeoPoint randomPoint() {
		Location c = mLocationService.getCurrentLocation();
		if (c != null) {
			int lat = (int) ((int)(c.getLatitude() + 0.001*(0.5 - rand.nextDouble()))*1E6);
			int log = (int) ((int)(c.getLongitude() + 0.001*(0.5 - rand.nextDouble()))*1E6);
			return new GeoPoint(lat, log);
		} else 
			return new GeoPoint(0, 0);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mapOverlays.get(i);
	}

	@Override
	public int size() {
		return mapOverlays.size();
	}
	
    public void addOverlay(OverlayItem overlay) {
        mapOverlays.add(overlay);
        //this.populate();
    }

    private static class MonsterItem extends OverlayItem {
    	GeoPoint internPoint;

		public MonsterItem(GeoPoint point, java.lang.String title, java.lang.String snippet) {
			super(point, title, snippet);
			internPoint = point;
		}
    	
		@Override
		public GeoPoint getPoint() {
			return internPoint;//super.getPoint();
		}
		
		public void setInternPoint(GeoPoint p) {
			internPoint = p;
		}
    }
}

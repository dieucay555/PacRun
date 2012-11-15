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
		
		for (int i = 1; i <= 3; i++) {
			OverlayItem overlayitem = new OverlayItem(randomPoint(), "Monster " + i, "A Monster");
			addOverlay(overlayitem);
		}
		populate();
	}
	
	public GeoPoint randomPoint() {
		Location c = mLocationService.getCurrentLocation();
		int lat = (int) ((int)(c.getLatitude() + rand.nextDouble())*1E6);
		int log = (int) ((int)(c.getLongitude() + rand.nextDouble())*1E6);
		return new GeoPoint(lat, log);
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

}

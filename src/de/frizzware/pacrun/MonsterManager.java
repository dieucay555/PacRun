package de.frizzware.pacrun;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MonsterManager extends ItemizedOverlay<OverlayItem> {
	private final LocationService mLocationService;
	private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
	
	public MonsterManager(Drawable drawable, LocationService locationSevice) {
		super(drawable);		
		mLocationService = locationSevice;
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return mapOverlays.size();
	}
	
    public void addOverlay(OverlayItem overlay) {
        mapOverlays.add(overlay);
         this.populate();
    }

}

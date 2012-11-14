package de.frizzware.pacrun;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

public class GameActivity extends MapActivity implements LocationService.LocationHandler{
	LocationService mLocationService;
	MapView mMap;
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        
        // Displaying Zooming controls
        mMap = (MapView) findViewById(R.id.mapview);
        mMap.setBuiltInZoomControls(false);
        mMap.setStreetView(true); // Street View
        
        mLocationService = new LocationService(this, this);
        mLocationService.start();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onLocationChanged(Location l) {
        GeoPoint point = new GeoPoint((int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6));
        MapController controller = mMap.getController();
        controller.animateTo(point);
        controller.setZoom(19);
        
        List<Overlay> mapOverlays = mMap.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.pacman);
        AddItemizedOverlay itemizedOverlay =
        new AddItemizedOverlay(drawable, this);
         
        OverlayItem overlayitem = new OverlayItem(geoPoint, "Hello", "Sample Overlay item");
         
        itemizedOverlay.addOverlay(overlayitem);
        mapOverlays.add(itemizedOverlay);
	}
}

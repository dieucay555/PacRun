package de.frizzware.pacrun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.location.Location;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class UserLocationOverlay extends MyLocationOverlay {
    private float   mOrientation;
    private Bitmap  mBitmap;

    public UserLocationOverlay(Context context, MapView mapView, Bitmap bitmap) {
        super(context, mapView);
        mBitmap = bitmap;
    }

    @Override 
    protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
        // translate the GeoPoint to screen pixels
        Point screenPts = mapView.getProjection().toPixels(myLocation, null);

        // create a rotated copy of the marker
        Matrix matrix = new Matrix();
        matrix.postRotate(mOrientation);
        Bitmap rotatedBmp = Bitmap.createBitmap(
        		mBitmap, 
            0, 0, 
            mBitmap.getWidth(), 
            mBitmap.getHeight(), 
            matrix, 
            true
        );
        // add the rotated marker to the canvas
        canvas.drawBitmap(
            rotatedBmp, 
            screenPts.x - (rotatedBmp.getWidth()  / 2), 
            screenPts.y - (rotatedBmp.getHeight() / 2), 
            null
        );
    }

    public void setOrientation(float newOrientation) {
        mOrientation = newOrientation;
   }
}

package se.tidsmaskinen.map;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.android.R;
import se.tidsmaskinen.intro.InfoScreen;
//import se.tidsmaskinen.ksamsok.ListItem;
//import se.tidsmaskinen.ksamsok.SearchServie;
import se.tidsmaskinen.europeana.ListItem;
import se.tidsmaskinen.europeana.SearchServie;
import se.tidsmaskinen.utils.ImageUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;



public class MapScreen extends MapActivity  {
    
	private static final int POSITION_TIMEOUT_MS = 30000;

	private static final int MAP_MARKER_SIZE = 120;
	
	private ProgressDialog mProgressDialog;
	private Thread mProgressThread;
	private LocationManager mLocationManager;
	private final LocationListener mGpsLocationListener = new customLocationListener();
	private final LocationListener mNetworkLocationListener = new customLocationListener();
	private MapView mMapView;
	public static String mCoordinates;
	private Location mCurrentLocation;
	private Timer timeoutTimer;
	
	private int mPage = 1;
	
	private boolean isFirstLocationSearch = true;
	
	
	/** Called when the options menu is first created. */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return true;
	}
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);
	    
	    mMapView = (MapView) findViewById(R.id.mapview);	    
	    mMapView.setBuiltInZoomControls(true);
	    mMapView.setSatellite(false);
	    	    
	    //Center on Strömsund, Jämtland, Sweden
        MapController mc = mMapView.getController();
        mc.setCenter(new GeoPoint((int)(63.85*1e6), (int)(15.583333*1e6))); 
        mc.setZoom(5);
        
	    mMapView.getOverlays().add(new Overlay() {});
	    mMapView.getOverlays().add(new Overlay() {});

		
		ImageView next = (ImageView) findViewById(R.id.next);
		ImageView previous = (ImageView) findViewById(R.id.previous);
		next.setOnClickListener(onNextOrPrevClickListener);
		previous.setOnClickListener(onNextOrPrevClickListener);
		
		Button b = (Button) findViewById(R.id.updatePosition_btn);
		b.setOnClickListener(positionBtnClickListener);
		
		Button b2 = (Button) findViewById(R.id.search_btn);
		b2.setOnClickListener(searchBtnClickListener);
		
	    getPosition();
    }
    
    private OnClickListener positionBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
        	getPosition();
        }
    };
    
    private OnClickListener searchBtnClickListener = new OnClickListener() {
        public void onClick(View v) {
        	doSearch();
        }
    };
    
	private OnClickListener onNextOrPrevClickListener = new OnClickListener() {
		
		public void onClick(View v) {	
			if( v == findViewById(R.id.next)){
				mPage ++;
			}
			else if( v == findViewById(R.id.previous)){
				mPage --;
			}
			mProgressDialog = ProgressDialog.show(MapScreen.this, "Vänta...", "Hämtar sida " + mPage + " av " + SearchServie.getInstance().getTotalPages() + "...", true , true, onSearchCancel);
			mProgressDialog.show();
			mProgressThread = new PageThread(handler);
			mProgressThread.start();
		}	
	};
	
	
    private void getPosition() {
    	mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 	    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGpsLocationListener);
 	    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mNetworkLocationListener);
 	    
 		mProgressDialog = ProgressDialog.show(MapScreen.this, "Vänta...", "Hämtar position...", true , true, onLocationCancel);
 		mProgressDialog.show();
 	    
 		// get last known location if it takes too long..
 		timeoutTimer = new Timer();
 		timeoutTimer.schedule(new GPSTimeoutTask(), POSITION_TIMEOUT_MS);
	}

	private OnCancelListener onLocationCancel = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			stopLocationListening();
		}
	};
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Intent intent = new Intent();
		switch (item.getItemId()) {
		case R.id.center:
			zoomToPosition();
			return true;
			
		case R.id.maptype:
			final CharSequence[] mapeTypes = {"Normal", "Satellit"};

			AlertDialog.Builder mapTypeBuilder = new AlertDialog.Builder(this);
			mapTypeBuilder.setTitle("Välj typ av kartlager");
			mapTypeBuilder.setItems(mapeTypes, new DialogInterface.OnClickListener() 
			{
			    public void onClick(DialogInterface dialog, int item) 
			    {			    	
			    	if (item == 1){ mMapView.setSatellite(true); }
			    	else { mMapView.setSatellite(false); }
			    }
			});
			AlertDialog mapTypeAlert = mapTypeBuilder.create();
			mapTypeAlert.show();
			return true;

		case R.id.quit:
			this.finish();
			return true;
		
		case R.id.about:
			Intent myIntent = new Intent(MapScreen.this, InfoScreen.class);
            startActivity(myIntent);
			return true;
		}

		return false;
	}
	
	private void zoomToPosition() {
		if (mCurrentLocation != null){
    		GeoPoint geo = new GeoPoint((int)(mCurrentLocation.getLatitude()*1e6),(int)(mCurrentLocation.getLongitude()*1e6));
 	       	MapController mc = mMapView.getController();
 	       	mc.setCenter(geo);
 	       	int zoomlevel = mMapView.getMaxZoomLevel();
 	       	mc.setZoom(zoomlevel - 5);
	       	}
		else {
			Toast.makeText(getApplicationContext(), R.string.no_gps, Toast.LENGTH_LONG).show();
		}
		
	}

	private void doSearch() {

		mPage = 1;
		GeoPoint leftPoint = mMapView.getProjection().fromPixels(0, mMapView.getHeight()); 
		GeoPoint rightPoint = mMapView.getProjection().fromPixels(mMapView.getWidth(), 0); 
		    	
		//mCoordinates = "%22"+(leftPoint.getLongitudeE6()/1e6)+"+"+(leftPoint.getLatitudeE6()/1e6)+"+"+(rightPoint.getLongitudeE6()/1e6)+"+"+(rightPoint.getLatitudeE6()/1e6)+"%22";
		mCoordinates = "enrichment_place_latitude%3A["+(leftPoint.getLatitudeE6()/1e6)+"+TO+"+(rightPoint.getLatitudeE6()/1e6)
		  +"]+AND+enrichment_place_longitude%3A["+(leftPoint.getLongitudeE6()/1e6)+"+TO+"+(rightPoint.getLongitudeE6()/1e6)+"]";

		mProgressDialog = ProgressDialog.show(MapScreen.this, "Vänta...", "Söker...", true , true, onSearchCancel);
		mProgressDialog.show();
	
		
		mProgressThread = new ProgressThread(handler);
		mProgressThread.start();
	}
	
	private OnCancelListener onSearchCancel = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			SearchServie.getInstance().cancelSearch();
		}
	};
	
	
	/** Updates the location marker. */
	private void updateWithNewLocation(Location location) 
	{
		
		if (location != null) 
		{
			mCurrentLocation = location;
			double lati = location.getLatitude();
			double longi = location.getLongitude();

			Drawable drawable;
			if (!mCurrentLocation.getProvider().equals("gps"))
			{
				drawable = this.getResources().getDrawable(R.drawable.red_ic_maps_indicator_current_position);
			}
			else
			{
				drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
			}
		    LocationOverlay locationOverlay = new LocationOverlay(drawable);
		    
		    GeoPoint point = new GeoPoint((int)(lati*1e6),(int)(longi*1e6));
		    OverlayItem overlayitem = new OverlayItem(point, "", "");
		    locationOverlay.addOverlay(overlayitem);
		    
		    updateLayer(0, locationOverlay);
		}
	}
	
	@Override
	protected void onStop() 
	{
		stopLocationListening();
		super.onStop();
	}
	


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private void stopLocationListening() {
		mLocationManager.removeUpdates(mGpsLocationListener);
		mLocationManager.removeUpdates(mNetworkLocationListener);
		if(timeoutTimer != null)
			timeoutTimer.cancel();
	}
	
	/** 
	 * Updates a specific map layer with a new overlay.
	 * @param layer, the layer to be updated
	 * @param overLay, the new overlay
	 */
	private void updateLayer(int layer, Overlay overLay){   
		List<Overlay> mapOverlays = mMapView.getOverlays();	    
	    mapOverlays.set(layer, overLay);	   
	    mMapView.invalidate();
	}
	
	/**
	 * Makes an map overlay from the search results and then sends 
	 * them to update the map.  
	 */
	public void loadData(){   
		List<ListItem> items = SearchServie.getInstance().getItems();  
	    Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
	    MapOverlay itemizedoverlay = new MapOverlay(drawable, this);
	    if(items != null){  
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).getCoordinates() != null){
					OverlayItem overlayitem = new OverlayItem(items.get(i).getCoordinates(), null, null);
					Bitmap image = items.get(i).getThumbnail();
					if(image != null){
						Bitmap resized = ImageUtils.modify(image, MAP_MARKER_SIZE, MAP_MARKER_SIZE, 0);
						BitmapDrawable marker = new BitmapDrawable(resized);
						overlayitem.setMarker(marker);
					}
					itemizedoverlay.addOverlay(overlayitem);
				}
			}
	    }
		
		if (SearchServie.getInstance().getTotalPages() > 1){
			Toast.makeText(getApplicationContext(), "Sida " + mPage + " av " + SearchServie.getInstance().getTotalPages(), Toast.LENGTH_SHORT).show();
			if(SearchServie.getInstance().getTotalPages() > mPage){
				findViewById(R.id.next).setVisibility(View.VISIBLE);
			}
			else {
				findViewById(R.id.next).setVisibility(View.INVISIBLE);
			}
			if(mPage != 1){
				findViewById(R.id.previous).setVisibility(View.VISIBLE);
			}
			else {
				findViewById(R.id.previous).setVisibility(View.INVISIBLE);
			}
		}
		else {
			findViewById(R.id.next).setVisibility(View.INVISIBLE);
			findViewById(R.id.previous).setVisibility(View.INVISIBLE);
		}
		
	    updateLayer(1, itemizedoverlay);
	}
	

	
	
	/**
	 * Search result handler
	 */
	final Handler handler = new Handler() {        
        public void handleMessage(Message msg) {        	
        	mProgressDialog.dismiss();
        	if (msg.what == 1) {
        		loadData();
			}
        	else if (msg.what == -1) {
        		//updateLayer(1, new Overlay(){});
        		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_LONG).show();
			}
        	else if (msg.what == -2){
        		Toast.makeText(getApplicationContext(), "Avbruten sökning", Toast.LENGTH_LONG).show();
        	}
        	else
        	{
        		//updateLayer(1, new Overlay(){});
        		Toast.makeText(getApplicationContext(), R.string.no_results, Toast.LENGTH_LONG).show();
        	}
        }
    };
    
    
    class GPSTimeoutTask extends TimerTask {
 	   public void run() {
 		  timeoutHandler.sendEmptyMessage(1);  
 	   }
 	}
    
    /**
     * Handles callback from when location finding takes too long
     */
	final Handler timeoutHandler = new Handler() {        
        public void handleMessage(Message msg)  {        	
    		mProgressDialog.dismiss();
    		Location l = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		if(l == null){
    			l = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    		}
  		   
    		if(l != null){
    			updateWithNewLocation(l);
    			zoomToPosition();
    		} 
    		
			doSearch();
			stopLocationListening();	
        }
    };
	

    /** A nested thread that handles the actual search. */
    private class ProgressThread extends Thread {
        Handler mHandler;
       
        ProgressThread(Handler h) {
            mHandler = h;
        }
       
        public void run() {
        	int gotData = SearchServie.getInstance().search("",false,false,"","",mCoordinates);
        	mHandler.sendEmptyMessage(gotData);       	
        }
    }
    
    /** A nested thread that handles the actual search. */
    private class PageThread extends Thread {
        Handler mHandler;
       
        PageThread(Handler h) {
            mHandler = h;
        }
       
        public void run() {
        	SearchServie.getInstance().additionalSearch(mPage);
        	mHandler.sendEmptyMessage(1);       	
        }
    }
        
    /** The location listener that handles all the changes in location */
    private class customLocationListener implements LocationListener  {
        @Override
        public void onLocationChanged(Location location) 
        {
        	if(timeoutTimer != null)
        		timeoutTimer.cancel();
        	
        	stopLocationListening();
        	mProgressDialog.dismiss();
        	updateWithNewLocation(location);
        	zoomToPosition();
        	
        	if(isFirstLocationSearch){
        		doSearch();
        		isFirstLocationSearch = false;
        	}
        	
        }
       
	    public void onProviderDisabled(String provider)
	    {
	    	updateWithNewLocation(null);
	    }

	    public void onProviderEnabled(String provider) {}

	    public void onStatusChanged(String provider, int status, Bundle extras) {}	
    }





}
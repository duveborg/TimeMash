package se.tidsmaskinen.map;

import java.util.ArrayList;
import java.util.List;

import se.tidsmaskinen.detail.DetailScreen;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MapOverlay extends ItemizedOverlay<OverlayItem>
{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	
	public MapOverlay(Drawable defaultMarker) 
	{
		super(boundCenterBottom(defaultMarker));
	}
	
	public MapOverlay(Drawable defaultMarker, Context context) 
	{
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}
	
	public void addOverlay(OverlayItem overlay) 
	{
	    mOverlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) 
	{
		Drawable marker = mOverlays.get(i).getMarker(0);
		if(marker != null)
			boundCenterBottom(marker);
		return mOverlays.get(i);
	}

	@Override
	public int size() 
	{
		return mOverlays.size();
	}
	
	/**
	 * Handles the tap event.
	 * When the marker is taped a detail screen is launched, with the
	 * markers index as an extra.
	 */	
	@Override
	protected boolean onTap(int index) 
	{  

		final List<OverlayItem> nearby = getNearbyMarkers(mOverlays.get(index));
		
		Log.e("TIDSMASKIN", "NÄRA: " + nearby.size());
		// alltid nära sig själv, så alltid 1 eller större
		if(nearby.size() > 1){
			//final CharSequence[] items = {"Red", "Green", "Blue"};

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					ImageView view = new ImageView(mContext);
					view.setImageDrawable(nearby.get(position).getMarker(0));
					return view;
				}
				
				@Override
				public long getItemId(int position) {
					// TODO Auto-generated method stub
					return 0;
				}
				
				@Override
				public Object getItem(int position) {
					return nearby.get(position);
				}
				
				@Override
				public int getCount() {
					// TODO Auto-generated method stub
					return nearby.size();
				}
			}, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	
			    	int realIndex = mOverlays.indexOf(nearby.get(item));
					Intent intent = new Intent();
					intent = new Intent(mContext, DetailScreen.class);
					intent.putExtra("Id", realIndex);
					//intent.putExtra("Map", 1);
					mContext.startActivity(intent);
			    }
			});
			
			//builder.setTitle("Pick a color");
			AlertDialog alert = builder.create();
			alert.show();
		}
		
		else {
			Intent intent = new Intent();
			intent = new Intent(mContext, DetailScreen.class);
			intent.putExtra("Id", index);
			//intent.putExtra("Map", 1);
			mContext.startActivity(intent);
		}
		
		
		
		return true;
	}
	
	
	private float minDistance = 70;
	
	private List<OverlayItem> getNearbyMarkers(OverlayItem item){
		List<OverlayItem> result = new ArrayList<OverlayItem>();
		for(OverlayItem i : mOverlays){
			if(distance(i, item) < minDistance){
				result.add(i);
			}
		}
		
		return result;
	}
	
	private float distance(OverlayItem i1, OverlayItem i2){
		Location location1 = new Location("");
		location1.setLatitude(i1.getPoint().getLatitudeE6());
		location1.setLongitude(i1.getPoint().getLongitudeE6());
		
		Location location2 = new Location("");
		location2.setLatitude(i2.getPoint().getLatitudeE6());
		location2.setLongitude(i2.getPoint().getLongitudeE6());

		return location1.distanceTo(location2);
	}

}

package se.tidsmaskinen.europeana;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import se.tidsmaskinen.sok.DetailScreen;

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
		Intent intent = new Intent();
		intent = new Intent(mContext, DetailScreen.class);
		intent.putExtra("Id", index);
		intent.putExtra("Map", 1);
		mContext.startActivity(intent);
		return true;
	}

}

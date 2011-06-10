package se.tidsmaskinen.map;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class LocationOverlay extends ItemizedOverlay<OverlayItem>
{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	public LocationOverlay(Drawable defaultMarker) 
	{
		super(boundCenter(defaultMarker));
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
	
	@Override
	protected boolean onTap(int index) 
	{  
		return true;
	}

}

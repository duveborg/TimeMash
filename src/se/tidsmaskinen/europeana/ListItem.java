package se.tidsmaskinen.europeana;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.maps.GeoPoint;

public class ListItem 
{
	private String mTitle = "-";
	private String mThumbnailURL;
	private Bitmap mThumbnail = null;
	private List<String> mImages = new ArrayList<String>();
	private String mDescription;
	private String mType;
	private String mIdLabel;
	private String mTimeLabel;
	private String mPlaceLabel;
	private String mOrganization;
	private String mLink;
	
	private Double mLongitude;
	private Double mLatitude;
	
	/** Gets and sets the items title. */
	public String getTitle() { return mTitle; }
	public void setTitle(String title){	this.mTitle = title.trim();}

	/** Gets and sets the items thumbnail. */
	public String getThumbnailURL(){ return mThumbnailURL; }
	public void setThumbnailURL(String thumbnailURL)
	{ 
		this.mThumbnailURL = thumbnailURL.trim();
		setThumbnail(thumbnailURL.trim());
	}
	
	/** Gets and sets the items thumbnail. */
	public Bitmap getThumbnail(){ return mThumbnail; }
	public void setThumbnail(Bitmap thumbnail){ this.mThumbnail = thumbnail; }
	private void setThumbnail(String thumbnailURL)
	{ 
        Bitmap bitmap = null;
        InputStream in = null;      
        URL imageUrl = null;
        try 
        {
    		try 
    		{
    			imageUrl = new URL(thumbnailURL.replaceAll(" ", "%20"));
    		} 
    		catch (MalformedURLException e) 
    		{
    			throw new RuntimeException(e);
    		}
        	
    		URLConnection connection = imageUrl.openConnection();
    		connection.setConnectTimeout(1500);
    		connection.setReadTimeout(1500);
            in = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } 
        catch (Exception e) 
        {           
        	//e.printStackTrace();
        }
		this.mThumbnail = bitmap; 
	}
	
	/** Gets and sets the items images. */
	public List<String> getImages(){ return mImages; }
	public void setImages(String image)
	{ 
		if (!(image.contains("dokument")&& image.contains("http://www.fmis.raa.se/fmis/")))
		this.mImages.add(image);
	}

	/** Gets and sets the items description. */
	public String getDescription(){	return mDescription;	}
	public void setDescription(String description)
	{	
		if (this.mDescription != null){this.mDescription += "\n\n" +description.trim();}
		else{this.mDescription = description.trim();}		
	}
	
	/** Gets and sets the items type. */
	public String getType() { return mType; }
	public void setType(String type){	this.mType = type.trim();}
	
	/** Gets and sets the items id label. */
	public String getIdLabel() { return mIdLabel; }
	public void setIdLabel(String idLabel){	this.mIdLabel = idLabel.trim();}
	
	/** Gets and sets the items time label. */
	public String getTimeLabel() { return mTimeLabel; }
	public void setTimeLabel(String timeLabel){	this.mTimeLabel = timeLabel.trim();}
	
	/** Gets and sets the items place label. */
	public String getPlaceLabel() { return mPlaceLabel; }
	public void setPlaceLabel(String placeLabel){	this.mPlaceLabel = placeLabel.trim();}
	
	/** Gets and sets the items organization. */
	public String getOrganization() { return mOrganization; }
	public void setOrganization(String organization){	this.mOrganization = organization.trim();}
	
	/** Gets and sets the source link. */
	public String getLink() { return mLink; }
	public void setLink(String link){	this.mLink = link.trim();}

	/** Gets and sets the items latitude and longitude. */
	public Double getLatitude() { return mLatitude; }	
	public Double getLongitude() { return mLongitude; }	
	
	public void setLatitude(String coordinates)
	{	
		mLatitude = Double.parseDouble(coordinates);
	}
	public void setLongitude(String coordinates)
	{	
		mLongitude = Double.parseDouble(coordinates);
	}
	
	/** Gets the items coordinates as a GeoPoint. */
	public GeoPoint getCoordinates() 
	{ 
		return new GeoPoint((int)(mLatitude*1e6), (int)(mLongitude*1e6)); 
	}	
	
}

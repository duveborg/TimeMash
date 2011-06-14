package se.tidsmaskinen.europeana;

import java.util.ArrayList;
import java.util.List;
import se.tidsmaskinen.utils.ImageUtils;
import android.graphics.Bitmap;
import com.google.android.maps.GeoPoint;

public class ListItem 
{
	private String mTitle = "-No headline-";
	private String mThumbnailURL;
	private Bitmap mThumbnail = null;
	private List<String> mImages = new ArrayList<String>();
	private String mDescription;
	private String mByLine;
	private String mType;
	private String mIdLabel;
	private String mTimeLabel = "Date was not specified.";
	private String mPlaceLabel = "This object has no specified location associated with it.";
	private String mOrganization = "No organization hase been specified for this object.";
	private String mLink;
	
	private Double mLongitude;
	private Double mLatitude;
	
	/** Gets and sets the items title. */
	public String getTitle() { return mTitle; }
	public void setTitle(String title)
	{	if (title.trim().length() > 0)
		this.mTitle = title.trim();
	}

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
	        Bitmap bitmap = ImageUtils.downloadImage(thumbnailURL);
			this.mThumbnail = bitmap;		
	}
	
	/** Gets and sets the items images. */
	public List<String> getImages(){ return mImages; }
	public void setImages(String image)
	{ 
		if (!(image.contains("dokument")&& image.contains("http://www.fmis.raa.se/fmis/")))
		this.mImages.add(image);
	}
	
	public String getByLine(){	return mByLine;	}
	public void setByLine(String byLine){	this.mByLine = byLine.trim();}

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
		if (mLatitude != null)
		{
			return new GeoPoint((int)(mLatitude*1e6), (int)(mLongitude*1e6)); 
		}
		else
		{
			return new GeoPoint((int)(0*1e6), (int)(0*1e6)); 
		}
	}	
	
}

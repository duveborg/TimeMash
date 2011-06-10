package se.tidsmaskinen.europeana;

import java.util.List;

public class SearchServie 
{
	static final SearchServie INSTANCE = new SearchServie();
	
	static final int SELECTION_SIZE = 20;
	static final int MAP_SIZE = 20;
	static final String BASE_URL = "http://api.europeana.eu/api/opensearch.rss?searchTerms=";	
	static final String PAGE = "&startPage=";
	static final String API_KEY = "&wskey=2Ey79FjnHsQ";
	
	private String mQuery = "";
	private int mTotalResults;
	private List<ListItem> mItems;
	
	private SearchServie() {}

	public static SearchServie getInstance() 
	{
		return INSTANCE;
	}
	/**
	 * Handles all primary searching in this application.
	 * Combines the supplied parameters to a finished query string, which then
	 * is used to do the actual search.
	 * @param query The text string supplied by the user
	 * @param withImages With or without images
	 * @param noLibris Books or no books
	 * @param type The chosen type
	 * @param milieu The chosen milieu
	 * @param coordinates A geo rectangle of the screen
	 * @return -1 for error 0 for no data, and 1 for when the search was a success.
	 */
	public int search(String query, boolean withImages, boolean libris, String type, String milieu, String coordinates)
	{		
		/*
		Boolean isFirst = true;
		
	    if(!"".equals(query))
		{	
	    	mQuery = "&query=text%3D";
	    	mQuery += "%22" +query.trim().replaceAll(" ", "%22+and+%22") +"%22";
	    	isFirst = false;
		}
	    else
	    {
	    	mQuery = "&query=";
	    }
			    
		if (milieu.equals("Byggnader"))
		{ 
			if (!isFirst){ mQuery+="+and+"; }else{ isFirst = false; } 
			mQuery += "%22bbrb%22"; 
		}
		if (milieu.equals("Fornlämningar"))
		{ 
			if (!isFirst){ mQuery+="+and+"; }else{ isFirst = false; }
			mQuery += "%22fmi%22"; 
		}
				
		if(withImages)
		{	
			if (!isFirst){ mQuery+="+and+"; }else{ isFirst = false; }
			mQuery += "thumbnailExists=j"; 
		}
		
		if (coordinates != null)
		{ 
			if (!isFirst){ mQuery+="+and+"; }else{ isFirst = false; }
			mQuery+= "boundingBox=/WGS84+" +coordinates;  
		}
		
		if(type.equals("Foto"))
		{ 
			if (!isFirst){ mQuery+="+and+"; }else{ isFirst = false; }
			mQuery += "itemType=%22foto%22"; 
		}
		if(type.equals("Föremål"))
		{	
			if (!isFirst){ mQuery+="+and+"; }else{ isFirst = false; }
			mQuery += "itemType=%22objekt/f%C3%B6rem%C3%A5l%22"; 
		}
		if(type.equals("Platser"))
		{	
			if (!isFirst){ mQuery+="+and+"; }else{ isFirst = false; }
			mQuery += "+itemType=%22milj%C3%B6%22"; 
		}
		
		if(!libris){	mQuery += "+not+serviceOrganization=LIBRIS"; }
		
		XMLPull xmlPull = new XMLPull(BASE_URL+SIZE+SELECTION_SIZE+API_KEY+mQuery);
		*/
		
		Boolean isFirst = true;
		
	    if(query.length() != 0)
		{	
	    	mQuery = query;
	    	isFirst = false;
		}
		
		if (coordinates != null)
		{ 
			if (!isFirst){ mQuery+="&"; }else{ isFirst = false; }
			mQuery+= coordinates;  
		}
		
		XMLPull xmlPull = new XMLPull(BASE_URL+mQuery+API_KEY);
		try 
		{
			mItems = xmlPull.parseRSS();
			if (mItems.isEmpty()){ return 0; }
		} 
		catch (Exception e) 
		{
			return -1;
		}
		return 1;
	}
	
	/**
	 * Handles additional searching. Uses the saved query but adds 
	 * other starting location
	 * @return 
	 */
	public boolean additionalSearch(int page)
	{
		/*
		int startFrom = ((page-1)*SELECTION_SIZE)+1;
		XMLPull xmlPull = new XMLPull(BASE_URL+SIZE+SELECTION_SIZE+API_KEY+FROM+startFrom+mQuery);
		*/
		XMLPull xmlPull = new XMLPull(BASE_URL+mQuery+API_KEY+PAGE+page);
		try 
		{
			mItems = xmlPull.parseRSS();
			return true;
		} 
		catch (Exception e) 
		{
			return false;
		}
	}
	
	public List<ListItem> getItems(){ return mItems; }			
	
	/** Returns the total number of pages */
	public int getTotalPages(){ return Math.round((float)mTotalResults/SELECTION_SIZE); }
		
	/** Returns the total number of results */
	public int getTotal(){ return mTotalResults; }
	
	/** Sets the total number of results */
	public void setTotal(int total){ mTotalResults = total; }

}

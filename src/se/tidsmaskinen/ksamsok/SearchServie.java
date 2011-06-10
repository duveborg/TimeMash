package se.tidsmaskinen.ksamsok;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;


public class SearchServie 
{
	static final SearchServie INSTANCE = new SearchServie();
	
	static final int PAGE_SIZE = 15;
	
	static final String BASE_URL = "http://kulturarvsdata.se/ksamsok/api";
	
	private Map<String, String> params = new HashMap<String, String>();

	public static volatile boolean searchCanceled = false;
	
	
	private int mTotalResults;
	private List<ListItem> mItems;
	
	private SearchServie() {}

	public static SearchServie getInstance() 
	{
		return INSTANCE;
	}

	public void cancelSearch(){
		searchCanceled = true;
	}
	
	public int search(String coordinates){
		searchCanceled = false;
		
		params.put("hitsPerPage", String.valueOf( PAGE_SIZE ));
		params.put("startRecord", "0");
		params.put("x-api", "test");
		params.put("method", "search");
		//params.put("recordSchema", "presentation");
		params.put("sort", "create_fromTime");
		params.put("sortConfig", "asc");
		
		Map<String, String> searchparams = new HashMap<String, String>();
		searchparams.put("boundingBox", "/WGS84+" + coordinates);
		searchparams.put("thumbnailExists", "j");
		searchparams.put("serviceName", "kmb"); 
		
		params.put("query", buildQuery(searchparams));
		String url = getUrl(params);
		Log.i("söker med url: ", url);
		XMLPull xmlPull = new XMLPull(url);
		try 
		{		
			mItems = xmlPull.parse();
			if (mItems.isEmpty()){ return 0; }
		} 
		catch (Exception e) 
		{
			return -1;
		}
		return 1;
	}
	
	
	private String buildQuery(Map<String, String> params){
		String url = "";
		boolean first = true;
		for(String key : params.keySet()){
			url += (first ? "" : "%20AND%20") + key + "%3D" + params.get(key);
			first = false;
		}
		
		//url += "%20AND%20create_fromTime<1990";
		return url;
	}
	
	private String getUrl(Map<String, String> params) {
		String url = BASE_URL;
		boolean first = true;
		
		for(String key : params.keySet()){
			
				url += (first ? "?" : "&") + key + "=" + params.get(key);
		
			first = false;
		}
		return url;
	}

	/**
	 * Handles additional searching. Uses the saved query but adds 
	 * other starting location
	 * @return 
	 */
	public boolean additionalSearch(int page) {
		searchCanceled = false;
		params.put("startRecord", String.valueOf( ((page-1)*PAGE_SIZE)+1) );
		XMLPull xmlPull = new XMLPull(getUrl(params));
		try 
		{
			mItems = xmlPull.parse();
			return true;
		} 
		catch (Exception e) 
		{
			return false;
		}
	}
	
	public List<ListItem> getItems(){ return mItems; }			
	

	public int getTotalPages(){ return Math.round((float)mTotalResults/PAGE_SIZE); }
		
	public int getTotal(){ return mTotalResults; }
	
	public void setTotal(int total){ mTotalResults = total; }

}

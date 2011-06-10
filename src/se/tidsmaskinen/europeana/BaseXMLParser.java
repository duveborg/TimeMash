package se.tidsmaskinen.europeana;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public abstract class BaseXMLParser implements XMLParser 
{
	// names of the XML tags
	static final String TOTAL = "totalHits";
	static final String RECORDS = "records";
	static final String RECORD = "record";
	static final String TITLE = "itemLabel";
	static final String THUMBNAIL = "thumbnail";
	static final String IMAGE = "lowresSource";
	static final String DESCRIPTION = "description";
	static final String BYLINE = "byline";
	static final String TYPE = "type";
	static final String ID = "idLabel";
	static final String DATE = "timeLabel";
	static final String PLACE = "placeLabel";
	static final String ORGANIZATION = "organization";
	static final String LINK = "url";
	static final String COORDINATES = "coordinates";
	
	//The URL to the specified service
	private final URL mServiceUrl;

	/**
	 * Sets up the service URL for later use
	 * @param serviceUrl
	 */
	protected BaseXMLParser(String serviceUrl)
	{
		try 
		{
			this.mServiceUrl = new URL(serviceUrl);
		} 
		catch (MalformedURLException e) 
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns connects to server and returns its response.
	 * @return InputStream, the response from the server
	 */
	protected InputStream getInputStream() 
	{
		InputStream in = null;
		try 
		{
			URLConnection connection = mServiceUrl.openConnection();
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			in = connection.getInputStream();
		} 
		catch (IOException e) 
		{
			//throw new RuntimeException(e);
		}
		return in;
	}
}

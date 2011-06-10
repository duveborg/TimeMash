package se.tidsmaskinen.ksamsok;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;


import android.util.Log;
import android.util.Xml;

public class XMLPull extends BaseXMLParser {

	public XMLPull(String serviceUrl)
	{
		super(serviceUrl);
	}

	/**
	 * Parse trough the XML returned from the server.
	 * Certain data, specified by tags, are collected and stored in 
	 * a new list item. All these items are then returned as a list.
	 */
	public List<ListItem> parse() 
	{
		List<ListItem> messages = null;
		XmlPullParser parser = Xml.newPullParser();
		try 
		{		
			InputStream inputStream = this.getInputStream();
			if (inputStream != null)
			{
			parser.setInput(inputStream, null);
			int eventType = parser.getEventType();
			ListItem currentMessage = null;
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				
				if(SearchServie.searchCanceled){
					Log.e("TIDSMASKIN", "Search was canceled");
					return new ArrayList<ListItem>();
				}
	
				String name = null;
				String prefix = null;
				String prefix2 = null;
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						messages = new ArrayList<ListItem>();
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
						prefix = parser.getNamespace("pres");
						prefix2 = parser.getNamespace("gml");
						//Log.e("TIDSMASKIN", "name: " + name);
						if (name.equalsIgnoreCase(RECORD))
						{
							currentMessage = new ListItem();
						} 
						else if (currentMessage != null)
						{
							if (name.equalsIgnoreCase(TITLE))
							{
								currentMessage.setTitle(parser.nextText());
							}
							else if (name.equalsIgnoreCase(THUMBNAIL))
							{
								currentMessage.setThumbnailURL(parser.nextText());
							}
							else if (name.equalsIgnoreCase(IMAGE))
							{
								currentMessage.setImages(parser.nextText());
							}
							else if (name.equalsIgnoreCase(DESCRIPTION)&&(prefix != null))
							{
								currentMessage.setDescription(parser.nextText());
							}
							else if (name.equalsIgnoreCase(BYLINE)&&(prefix != null))
							{
								currentMessage.setDescription("Fotograf: " +parser.nextText());
							}
							else if (name.equalsIgnoreCase(TYPE)&& prefix != null)
							{
								currentMessage.setType(parser.nextText());
							}
							else if (name.equalsIgnoreCase(ID))
							{
								currentMessage.setIdLabel(parser.nextText());
							}
							else if (name.equalsIgnoreCase(DATE))
							{
								currentMessage.setTimeLabel(parser.nextText());
							}
							else if (name.equalsIgnoreCase(PLACE))
							{
								currentMessage.setPlaceLabel(parser.nextText());
							}
							else if (name.equalsIgnoreCase(ORGANIZATION)&&(prefix != null))
							{
								currentMessage.setOrganization(parser.nextText());
							}
							else if (name.equalsIgnoreCase(LINK))
							{
								currentMessage.setLink(parser.nextText());
							}
							else if (name.equalsIgnoreCase(COORDINATES)&&(prefix != null)&&(prefix2 != null))
							{
								currentMessage.setCoordinates(parser.nextText());
							}
							else if (name.equalsIgnoreCase(RDF_LINK) && parser.getAttributeValue(0).equals("Presentation") )
							{				
								currentMessage.setXmlLink(parser.nextText());
							}
						}
						else if (name.equalsIgnoreCase(TOTAL))
						{
							SearchServie.getInstance().setTotal(Integer.parseInt(parser.nextText()));							
						}
						break;
						
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(RECORD) && currentMessage != null)
						{
							messages.add(currentMessage);
						} 
						else if (name.equalsIgnoreCase(RECORDS))
						{
							done = true;
						}
						break;
				}
				eventType = parser.next();
			}
			}
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		return messages;
	}
}

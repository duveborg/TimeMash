package se.tidsmaskinen.europeana;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import android.util.Xml;

public class XMLPull extends BaseXMLParser {

	public XMLPull(String serviceUrl)
	{
		super(serviceUrl);
	}
	
	/* 
     <item>
        <title> </title>
        <link>http://www.europeana.eu/portal/record/03901/30DF05CB0B6E06F72DC2A8379154DE848A5D6040.srw?wskey=2Ey79FjnHsQ</link>

        <description>Culture.fr/collections</description>
        <enclosure url="http://www.culture.gouv.fr/Wave/image/joconde/0047/hn00478_e0550_v.jpg" />
        <europeana:year> </europeana:year>
        <europeana:language>fr</europeana:language>
        <europeana:type>IMAGE</europeana:type>
        <europeana:provider>Culture.fr/collections</europeana:provider>

        <europeana:dataProvider> </europeana:dataProvider>
        <europeana:rights> </europeana:rights>
        <enrichment:place_latitude>65.6</enrichment:place_latitude>
        <enrichment:place_longitude>23.817</enrichment:place_longitude>
        <enrichment:place_term>http://sws.geonames.org/604423/</enrichment:place_term>
        <enrichment:place_label>malli</enrichment:place_label>

        <enrichment:place_label>mali</enrichment:place_label>
     </item>
	 * */
	
	public List<ListItem> parseRSS() 
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
				String name = null;
				//String prefix = null;
				String prefix2 = null;
				
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						messages = new ArrayList<ListItem>();
						break;
					case XmlPullParser.START_TAG:
						
						name = parser.getName();
						//prefix = parser.getNamespace("europeana");
						prefix2 = parser.getNamespace("enrichment");
						
						if (name.equalsIgnoreCase("item"))
						{
							currentMessage = new ListItem();
						} 
						else if (currentMessage != null)
						{
							if (name.equalsIgnoreCase("title"))
							{
								currentMessage.setTitle(parser.nextText());
							}
							else if (name.equalsIgnoreCase("description"))
							{
								currentMessage.setDescription(parser.nextText());
							}
							else if (name.equalsIgnoreCase("enclosure"))
							{
								currentMessage.setThumbnailURL(parser.getAttributeValue(0));
							}
							else if (name.equalsIgnoreCase("link"))
							{
								currentMessage.setLink(parser.nextText());
							}
							else if (name.equalsIgnoreCase("place_latitude")&&(prefix2 != null))
							{
								currentMessage.setLatitude(parser.nextText());
							}
							else if (name.equalsIgnoreCase("place_longitude")&&(prefix2 != null))
							{
								currentMessage.setLongitude(parser.nextText());
							}
						}
						else if (name.equalsIgnoreCase("totalResults"))
						{
							SearchServie.getInstance().setTotal(Integer.parseInt(parser.nextText()));							
						}
						break;
						
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase("item") && currentMessage != null)
						{
							messages.add(currentMessage);
						} 
						else if (name.equalsIgnoreCase("channel"))
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
	
	public ListItem parseItem() 
	{
		ListItem currentMessage = null;
		XmlPullParser parser = Xml.newPullParser();
		try 
		{		
			InputStream inputStream = this.getInputStream();
			if (inputStream != null)
			{
			parser.setInput(inputStream, null);
			int eventType = parser.getEventType();
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done)
			{
				String name = null;
				String prefix = null;
				String prefix2 = null;
				String prefix3 = null;
				String prefix4 = null;
				
				switch (eventType){
					case XmlPullParser.START_TAG:
						name = parser.getName();
						prefix = parser.getNamespace("europeana");
						prefix2 = parser.getNamespace("enrichment");
						prefix3 = parser.getNamespace("dc");
						prefix4 = parser.getNamespace("srw");
						if (name.equalsIgnoreCase("record")&&(prefix4 != null))
						{
							currentMessage = new ListItem();
						} 
						else if (currentMessage != null)
						{
							if (name.equalsIgnoreCase("title")&&(prefix3 != null))
							{
								currentMessage.setTitle(parser.nextText());
							}
							else if (name.equalsIgnoreCase("isShownBy")&&(prefix != null))
							{
								currentMessage.setThumbnailURL(parser.nextText());
							}
							else if (name.equalsIgnoreCase("object")&&(prefix != null))
							{
								currentMessage.setImages(parser.nextText());
							}
							else if (name.equalsIgnoreCase("description")&&(prefix3 != null))
							{
								currentMessage.setDescription(parser.nextText());
							}
							else if (name.equalsIgnoreCase("type")&& prefix3 != null)
							{
								currentMessage.setType(parser.nextText());
							}
							
							else if (name.equalsIgnoreCase("collectionName")&(prefix != null))
							{
								currentMessage.setIdLabel(parser.nextText());
							}
							else if (name.equalsIgnoreCase(DATE))
							{
								currentMessage.setTimeLabel(parser.nextText());
							}
							
							else if (name.equalsIgnoreCase("country"))
							{
								currentMessage.setPlaceLabel(parser.nextText());
							}
							else if (name.equalsIgnoreCase("provider")&&(prefix != null))
							{
								currentMessage.setOrganization(parser.nextText());
							}
							else if (name.equalsIgnoreCase("uri")&&(prefix != null))
							{
								currentMessage.setLink(parser.nextText());
							}
							
							else if (name.equalsIgnoreCase("place_latitude")&&(prefix2 != null))
							{
								currentMessage.setLatitude(parser.nextText());
							}
							else if (name.equalsIgnoreCase("place_longitude")&&(prefix2 != null))
							{
								currentMessage.setLongitude(parser.nextText());
							}
						}
						break;
						
					case XmlPullParser.END_TAG:
						name = parser.getName(); 
						if (name.equalsIgnoreCase("record")&&(prefix4 != null))
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
		return currentMessage;
	}

	@Override
	public List<ListItem> parse() {
		// TODO Auto-generated method stub
		return null;
	}
}

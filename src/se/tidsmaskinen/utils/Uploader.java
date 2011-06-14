package se.tidsmaskinen.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import se.tidsmaskinen.europeana.ListItem;

import android.util.Log;



public class Uploader {
	
	
	public static final String SERVER_BASE_URL = "http://updateculture.appspot.com";
	//public static final String SERVER_BASE_URL = "http://130.242.9.254:9000";
	
	public static HttpClient httpPostClient;
	
	/**
	 * Returns the id of the uploaded image, or -1 if fail
	 */
	public static Long upload(File image, 
			String user, 
			ListItem content,
			String imageLongitude, 
			String imageLatitude) throws ParseException, IOException{
	
		 Long result = -1L;
		
		 httpPostClient = new DefaultHttpClient();  
         String postURL = SERVER_BASE_URL + "/upload";
         
         HttpPost post = new HttpPost(postURL); 
	     FileBody bin = new FileBody(image);
	     MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
	     
	     
	     reqEntity.addPart("object.objectUri", new StringBody(content.getLink()));
	     
	     reqEntity.addPart("object.originalThumbnailUrl", new StringBody(content.getThumbnailURL()));
	    
	     if(content.getImages().size() > 0)
	    	 reqEntity.addPart("object.originalImageUrl", new StringBody(content.getImages().get(0)));
	     
	     if(content.getTimeLabel() != null)
	    	 reqEntity.addPart("object.originalDate", new StringBody(content.getTimeLabel()));
	     
	     if(content.getDescription() != null)
	    	 reqEntity.addPart("object.description", new StringBody(content.getDescription()));
	     
	     if(content.getByLine() != null)
	    	 reqEntity.addPart("object.imageByline", new StringBody(content.getByLine()));
	     
	     if(content.getTitle() != null)
	    	 reqEntity.addPart("object.label", new StringBody(content.getTitle()));
	     
	     reqEntity.addPart("object.longitude", new StringBody((content.getCoordinates().getLongitudeE6() / 1e6) + ""));
	     reqEntity.addPart("object.latitude", new StringBody((content.getCoordinates().getLatitudeE6() / 1e6) + ""));
	     
	     reqEntity.addPart("imageBytes", bin);
	     reqEntity.addPart("image.savedBy", new StringBody(user));
	     if(imageLatitude != null && imageLongitude != null){
	    	 reqEntity.addPart("image.latitude", new StringBody(imageLatitude));
	     	 reqEntity.addPart("image.longitude", new StringBody(imageLongitude));
	     }
	     
	     post.setEntity(reqEntity);  
	  
	     HttpResponse response = httpPostClient.execute(post);  
	     HttpEntity resEntity = response.getEntity();  
	     if (resEntity != null) {    
               String requestResponse = EntityUtils.toString(resEntity);
               result = Long.parseLong(requestResponse);
         }   
	
	     return result;
	}
	
	
	public static void abortUpload(){
		httpPostClient.getConnectionManager().shutdown();
	}
	
	
	
	public static List<Long> getImagesByUri(String uri){
		
		List<Long> ids = new ArrayList<Long>();
		try {
			HttpClient client = new DefaultHttpClient(); 
			String url = SERVER_BASE_URL + "/images?uri=" + URLEncoder.encode(uri, "UTF-8");
			
			HttpGet s = new HttpGet(url);
			
			HttpResponse response = client.execute(s);
			HttpEntity resEntity = response.getEntity();
			 if (resEntity != null) {    
	               String requestResponse = EntityUtils.toString(resEntity);
	               String[] strIds = requestResponse.split(",");
	               for(String id : strIds){
	            	   Log.e("TIDSMASKIN", "adding id: " + id);
	            	   
	            	   try {
	            		   Long longid = Long.parseLong(id);
	            		   ids.add(longid);
	            	   }
	            	   catch(Exception e){
	            		   //ignore
	            	   }
	               }
	         }  
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ids;
	}
	
	
	
	
	
	
	
	
	/*
	public static void uploadImage(Image image){
		
		
		

	    HttpParams p=new BasicHttpParams();
	    p.setParameter("name","asd");

	  //Instantiate an HttpClient
	  HttpClient client = new DefaultHttpClient(p);

	  //Instantiate a GET HTTP method
	    try {
		        HttpResponse response=client.execute(new HttpGet("http://www.itortv.com/android/sendName.php"));
		        InputStream is=response.getEntity().getContent();
		        //You can convert inputstream to a string with: http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
		} catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		} catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		}



		
		
		
		
		// Creates Byte Array from picture
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(imageToWrite, "png", baos);
		URL url = new URL("http://imgur.com/api/upload");

		//encodes picture with Base64 and inserts api key
		String data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(Base64.encode(baos.toByteArray()), "UTF-8");
		data += "&" + URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode("apinyckel", "UTF-8");

		// opens connection and sends data
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
	}
	*/
}

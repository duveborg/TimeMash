package se.tidsmaskinen.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class ImageUtils {
	
	public static final int CONNECTION_TIMEOUT_MS = 10000;
	
	
	private static final int MAX_CACHE_SIZE = 30;
	private static LinkedHashMap<String, Bitmap> imageCache = new LinkedHashMap<String, Bitmap>();
	
	public static Bitmap downloadImage(String url){
		Log.e("TIDSMASKIN", "Downloading image: " + url);

		Bitmap bitmap = imageCache.get(url);
		if(bitmap == null){

			InputStream in = null;      
			URL imageUrl = null;
			try 
			{

				try 
				{
					imageUrl = new URL(url.trim().replaceAll(" ", "%20"));
				} 
				catch (MalformedURLException e) 
				{
					throw new RuntimeException(e);
				}

				HttpGet httpRequest = null; 
				try { 
					httpRequest = new HttpGet(imageUrl.toURI()); 
				} catch (URISyntaxException e) { 
					e.printStackTrace(); 
				} 
				DefaultHttpClient httpclient = new DefaultHttpClient(); 
				HttpParams params = httpclient.getParams();
				HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT_MS);
				HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT_MS);


				HttpResponse response = (HttpResponse) httpclient.execute(httpRequest); 
				HttpEntity entity = response.getEntity(); 
				BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity (entity); 
				in = bufHttpEntity.getContent(); 
				bitmap = BitmapFactory.decodeStream(in); 
				
			      
		        imageCache.put(url, bitmap);
		        if(imageCache.size() > MAX_CACHE_SIZE){
		        	// remove first item
		        	for(String firstKey : imageCache.keySet()){
		        		imageCache.remove(firstKey);
		        		break;
		        	}
		        }
			}


			catch (Exception e) 
			{		
				Log.e("TIDSMASKINSFEL", "msg: " + e.getMessage());
			}
			finally {
				try {
					if(in != null)
						in.close();
				} catch (IOException e) {
				}
			}
		} else {
			Log.i("Tidsmaskin", "Got "+url+" from cache, chache size: " + imageCache.size());
		}
		return bitmap;                
	}
	
	
	public static Bitmap modify(Bitmap modifyme, int maxWidth, int maxHeight, int rotation){
		int width = modifyme.getWidth();
        int height = modifyme.getHeight();
       
        // calculate the scale - in this case = 0.4f
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        
        float smallestScale = Math.min(scaleWidth, scaleHeight);
       
        // createa matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(smallestScale, smallestScale);
        // rotate the Bitmap
        if(rotation != 0)
        	matrix.postRotate(rotation);
 
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(modifyme, 0, 0,
                          width, height, matrix, true);
        return resizedBitmap;
	}
	


	public static byte[] rotate(byte[] imageData, int rotation) {
		Bitmap modifyme = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		Matrix matrix = new Matrix();
    	matrix.postRotate(rotation);
    	 Bitmap rotatedBitmap = Bitmap.createBitmap(modifyme, 0, 0,
    			 modifyme.getWidth(), modifyme.getHeight(), matrix, true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
		byte[] b = baos.toByteArray();
		return b;
	}
}

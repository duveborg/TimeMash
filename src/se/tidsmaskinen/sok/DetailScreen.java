package se.tidsmaskinen.sok;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import se.tidsmaskinen.europeana.ListItem;
import se.tidsmaskinen.europeana.SearchServie;
import se.tidsmaskinen.europeana.XMLPull;

import com.google.android.maps.GeoPoint;
import se.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailScreen extends Activity 
{		
	private ProgressDialog mProgressDialog;
	private ProgressThread progressThread;
	private List<Integer> mItems = new ArrayList<Integer>();
	private int mPage = 0;
	private int mID = 0;
	private Bitmap mImage;
	private Bitmap mThumbnail;
	
	private ListItem theItem = new ListItem();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		if (getIntent().getIntExtra("Map", 0) == 1)
		{
			ImageView next = (ImageView) findViewById(R.id.next);
			ImageView previous = (ImageView) findViewById(R.id.previous);
			next.setOnClickListener(onClickListener);
			previous.setOnClickListener(onClickListener);
						
			SearchServie service = SearchServie.getInstance();
			List<ListItem> items = service.getItems();
			GeoPoint geo = items.get(getIntent().getIntExtra("Id", 0)).getCoordinates();
			
			for (int i = 0; i < items.size(); i++) 
			{
				if (items.get(i).getCoordinates() != null)
				{
					if(items.get(i).getCoordinates().getLatitudeE6() == geo.getLatitudeE6() && items.get(i).getCoordinates().getLongitudeE6() == geo.getLongitudeE6()) 
					{
						mItems.add(new Integer(i));
					}
				}
			}
			if(mItems.size() > 1){ next.setVisibility(0); } //Visible
			
			mID = mItems.get(mPage).intValue();
            mProgressDialog = new ProgressDialog(DetailScreen.this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();
            progressThread = new ProgressThread(handler);
			progressThread.start();
		}
		else
		{
			mID = getIntent().getIntExtra("Id", 0);
            mProgressDialog = new ProgressDialog(DetailScreen.this);
            mProgressDialog.setMessage("Laddar...");
            mProgressDialog.show();
            progressThread = new ProgressThread(handler);
			progressThread.start();
		}
	}
		
    /**
     * Downloads an image to the detail screen
     * @param Url, a Url string to and image
     * @return a bitmap from the given Url
     */
    private Bitmap DownloadImage(String URL)
    {        
        Bitmap bitmap = null;
        InputStream in = null;      
        URL imageUrl = null;
        try 
        {
    		try 
    		{
    			imageUrl = new URL(URL.replaceAll(" ", "%20"));
    		} 
    		catch (MalformedURLException e) 
    		{
    			throw new RuntimeException(e);
    		}    
            in = imageUrl.openConnection().getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } 
        catch (OutOfMemoryError e) 
        {            
        	try 
        	{
				in = imageUrl.openConnection().getInputStream();
				BitmapFactory.Options options =new BitmapFactory.Options();
				options.inSampleSize = 8;
	            bitmap = BitmapFactory.decodeStream(in, null, options);
	            in.close();
			} 
        	catch (OutOfMemoryError e1)
        	{				
				//e1.printStackTrace();
			} 
        	catch (IOException e1) 
        	{			
				//e.printStackTrace();
			}        	
        } 
        catch (Exception e) 
        {		
			//e.printStackTrace();
		}
        return bitmap;                
    }
    
	private OnClickListener onClickListener = new OnClickListener() 
	{
		public void onClick(View v) 
		{
			ImageView next = (ImageView) findViewById(R.id.next);
			ImageView previous = (ImageView) findViewById(R.id.previous);
			next.setVisibility(0); //Visible
			previous.setVisibility(0); //Visible
			
			if (v == findViewById(R.id.next))
			{
				if (mPage<mItems.size()-1)
				{
					mPage ++;
					mID = mItems.get(mPage).intValue();
    	            mProgressDialog = new ProgressDialog(DetailScreen.this);
    	            mProgressDialog.setMessage("Laddar...");
    	            mProgressDialog.show();
    	            progressThread = new ProgressThread(handler);
    				progressThread.start();
				}
			}
			else if (v == findViewById(R.id.previous))
			{
				if (mPage>0)
				{
					mPage --;
					mID = mItems.get(mPage).intValue();
    	            mProgressDialog = new ProgressDialog(DetailScreen.this);
    	            mProgressDialog.setMessage("Laddar...");
    	            mProgressDialog.show();
    	            progressThread = new ProgressThread(handler);
    				progressThread.start();			
				}
			}
			else
			{
				//Intent intent = new Intent(DetailScreen.this, ImageScreen.class);
				//intent.putExtra("Id", mID);
				//DetailScreen.this.startActivity(intent);
			}
			
			if (mPage >= mItems.size()-1){ next.setVisibility(4);} //Invisible			
			if (mPage == 0){ previous.setVisibility(4);} //Invisible
		}
	};
	
	private void loadItemInfo(int id)
	{
		/*
		SearchServie service = SearchServie.getInstance();
		List<ListItem> items = service.getItems();
		ListItem item = items.get(id);
		*/
		
		ListItem item = theItem;
		
        ImageView image = (ImageView) findViewById(R.id.image);
		TextView headline = (TextView) findViewById(R.id.headline);
		//TextView type = (TextView) findViewById(R.id.type);
		//TextView idLabel = (TextView) findViewById(R.id.id);
		TextView description = (TextView) findViewById(R.id.description);
		TextView date = (TextView) findViewById(R.id.date);
		TextView place = (TextView) findViewById(R.id.place);
		TextView organization = (TextView) findViewById(R.id.organization);
		TextView link = (TextView) findViewById(R.id.link);
				
	
        if (mImage != null)
        {
        	//image.setImageBitmap(mImage);
        	//findViewById(R.id.full_screen).setVisibility(0);
        	//image.setOnClickListener(onClickListener);
        }
        else
        {
        	if (mThumbnail != null)
            {
            	image.setImageBitmap(mThumbnail);
               	//findViewById(R.id.full_screen).setVisibility(4);
               	image.setOnClickListener(null);
            }
            else
            {
            	//findViewById(R.id.full_screen).setVisibility(4);
            	image.setOnClickListener(null);
            	image.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.no_image));
            }
        }      
        
		headline.setText(item.getTitle());
		//type.setText(item.getType());
		//idLabel.setText(item.getIdLabel());
		description.setText(item.getDescription());
		date.setText(item.getTimeLabel());
		place.setText(item.getPlaceLabel());
		organization.setText(item.getOrganization());
		link.setMovementMethod(LinkMovementMethod.getInstance());
		link.setText(Html.fromHtml("<a href=\"" +item.getLink() +"\">Länk till källa</a>"));
		findViewById(R.id.detail_view).startAnimation(AnimationUtils.loadAnimation(DetailScreen.this, R.anim.fade));
		findViewById(R.id.detail_view).setVisibility(0); //Makes the view visible the first time it is displayed
	}  
	
    final Handler handler = new Handler() 
    {        
        public void handleMessage(Message msg) 
        {
        	if (msg.what == 0)
        	{
        		loadItemInfo(mID);
			}
        	mProgressDialog.dismiss();
        }
    };
    
    /** A nested thread that downloads the next image to be display. */
    private class ProgressThread extends Thread 
    {
        Handler mHandler;
       
        ProgressThread(Handler h) 
        {
            mHandler = h;
        }
       
        public void run()
        {
        	SearchServie service = SearchServie.getInstance();
    		List<ListItem> items = service.getItems();
    		ListItem item = items.get(mID);
    		    		
    		XMLPull xmlPull = new XMLPull(item.getLink());
    		try 
    		{
    			theItem = xmlPull.parseItem();
    		} 
    		catch (Exception e) { }
    		
    		mImage = null;
    		mThumbnail = null;
    		
    		if (!theItem.getImages().isEmpty())
            {
    			mImage = DownloadImage(theItem.getImages().get(0));
            }
            else
            {
            	mThumbnail = DownloadImage(theItem.getThumbnailURL());
            }

        	mHandler.sendEmptyMessage(0);
        }

    }
}
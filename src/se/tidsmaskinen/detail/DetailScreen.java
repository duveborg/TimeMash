package se.tidsmaskinen.detail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.android.R;
import se.tidsmaskinen.camera.CameraScreen;
import se.tidsmaskinen.ksamsok.ListItem;
import se.tidsmaskinen.ksamsok.SearchServie;
import se.tidsmaskinen.utils.ImageUtils;
import se.tidsmaskinen.utils.Uploader;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailScreen extends Activity 
{		
	private ProgressDialog mProgressDialog;
	private ImageLoaderThread imageDownloadThread;
	private UploadThread uploadThread;
	private UpdateGalleryScrolLThread galleryScrollThread;
	
	private int mID = 0;
	private Bitmap mImage;
	private Bitmap mThumbnail;
	
	private List<Long> mGalleryImageIds = new ArrayList<Long>();
	
	private boolean galleryLoaded = false;
	private boolean mainImageLoaded = false;
	
	private String uploadedImageCoordinate;

	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detail_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.alias:
			promtForAlias();
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		mID = getIntent().getIntExtra("Id", 0);
        mProgressDialog =  ProgressDialog.show(DetailScreen.this, "Vänta...", "Hämtar bilder...", true , true, onProgressCancel);
        imageDownloadThread = new ImageLoaderThread(imageLoaderHandler);
		imageDownloadThread.start();
		galleryScrollThread = new UpdateGalleryScrolLThread(imageScrollHandler);
		galleryScrollThread.start();
	}
	
	private OnCancelListener onProgressCancel = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			finish();
		}
	};
	
	@Override
	public void onDestroy( ){
		super.onDestroy();
		Log.i("TIDSMASKIN", "DetailScreen.onDestroy");
	    ImageAdapter.cache.clear();
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.i("TIDSMASKIN", "onActivityResult()");
    	
    	if(resultCode == RESULT_OK){
    		View view = findViewById(R.id.upload_view);
    		view.setVisibility(View.VISIBLE);

    		uploadedImageCoordinate = data.getExtras().getString("coordinate");
    		
    		ImageView image = (ImageView) findViewById(R.id.newimage);
        	FileInputStream in = null;
        	try {
    			in = openFileInput(CameraScreen.TEMP_IMAGE_FILE_NAME);
    	    	Bitmap imgBitmap = BitmapFactory.decodeStream(in);
    	    	image.setImageBitmap(imgBitmap);
    	    	
    	    	ScrollView mainScrollView = (ScrollView) findViewById(R.id.scroll_view);
    	    	mainScrollView.fullScroll(ScrollView.FOCUS_UP);
    	    	
    	    	Toast.makeText(DetailScreen.this, "Tryck på \"Ladda upp bild\" om du vill spara bilen på resitiden.appspot.com", Toast.LENGTH_LONG).show();
    	    	
    	    	
    	    	final SharedPreferences settings = getSharedPreferences(PRES_FILE, 0);
    	        String alias = settings.getString(ALIAS_KEY, NOT_CHOSEN);
    	        if(alias.equals(NOT_CHOSEN)){
    	        	promtForAlias();
    	        }
    	    	
    		} catch (Exception e) {
    			Log.e("TIDSMASKIN", e.getMessage());
    		} finally {
    			if(in != null){
    				try { 
    					in.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}	
    		}
    	}
    }
	
	

	
	private static final String PRES_FILE = "prefsFile";
	private static final String NOT_CHOSEN = "_notchosen";
	private static final String DEFAULT_USER = "Anonym";
	private static final String ALIAS_KEY = "alias";
	
	private void promtForAlias(){
		final SharedPreferences settings = getSharedPreferences(PRES_FILE, 0);
		String alias = settings.getString(ALIAS_KEY, NOT_CHOSEN);


		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Välj alias");
		alert.setMessage("Ditt alias sparas tillsammans med bilder du laddar upp");

		final EditText input = new EditText(this);
		alert.setView(input);
		input.setText(alias.equals(NOT_CHOSEN) ? DEFAULT_USER : alias);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if(value != null && !"".equals(value) ){
					Editor e = settings.edit();
					e.putString(ALIAS_KEY, value);
					e.commit();
				}
			}
		});
		alert.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		alert.show();

	}
	
	private String getSavedAlias(){
		final SharedPreferences settings = getSharedPreferences(PRES_FILE, 0);
        String alias = settings.getString(ALIAS_KEY, NOT_CHOSEN);
        if(alias.equals(NOT_CHOSEN)){
        	alias = DEFAULT_USER;
        }
        return alias;
	}
	
	
	private void uploadImage(){			
    	mProgressDialog = ProgressDialog.show(DetailScreen.this, "Vänta...", "Laddar upp bild...", true , true, onUploadCancel);
        mProgressDialog.show();
        
        uploadThread = new UploadThread(imageUploaderHandler);
        uploadThread.start();
	}
	
	private OnCancelListener onUploadCancel = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			Uploader.abortUpload();
		}
	};
	
	private void loadItemInfo(){
		SearchServie service = SearchServie.getInstance();
		List<ListItem> items = service.getItems();
		final ListItem item = items.get(mID);
		
        ImageView image = (ImageView) findViewById(R.id.image);
		TextView headline = (TextView) findViewById(R.id.headline);
		TextView date = (TextView) findViewById(R.id.date);
		TextView description = (TextView) findViewById(R.id.description);
		TextView link = (TextView) findViewById(R.id.link);
		TextView place = (TextView) findViewById(R.id.place);
		TextView organization = (TextView) findViewById(R.id.organization);
		
	    final Button button = (Button) findViewById(R.id.goToCameraBtn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent();
        		intent = new Intent(DetailScreen.this, CameraScreen.class);
        		intent.putExtra("Id", mID);
        		DetailScreen.this.startActivityForResult(intent, 1);
            }
        });
        
        final Button uploadbutton = (Button) findViewById(R.id.uploadBtn);
        uploadbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	uploadImage();
            }
        });
		
     
		
        if (mImage != null) {
        	image.setImageBitmap(mImage);
        }
        else if (mThumbnail != null) {
        	image.setImageBitmap(mThumbnail);
        }
        else {
        	image.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.no_image));
        }
       
        date.setText((item.getTimeLabel() == null ? "Okänt datum" : item.getTimeLabel())  + ":");
		headline.setText(item.getTitle());
		description.setText(item.getDescription());
		organization.setText(item.getOrganization());
		place.setText(item.getPlaceLabel());
		link.setMovementMethod(LinkMovementMethod.getInstance());
		link.setText(Html.fromHtml("<a href=\"" +item.getLink() +"\">Länk till källa</a>"));
		 //Makes the view visible the first time it is displayed
	}  
	
	
	
	final Handler imageUploaderHandler = new Handler() {        
        public void handleMessage(Message msg) {     	
        	if (msg.what == 1) {
        		View view = findViewById(R.id.upload_view);
        		view.setVisibility(View.GONE);
        		
        		galleryScrollThread = new UpdateGalleryScrolLThread(imageScrollHandler);
        		galleryScrollThread.start();

        		mProgressDialog.dismiss();
        		Toast.makeText(DetailScreen.this, "Bild uppladdad!", Toast.LENGTH_LONG).show();
			} else {
				mProgressDialog.dismiss();
				Toast.makeText(DetailScreen.this, "Misslyckades med bilduppladdning", Toast.LENGTH_LONG).show();
			} 	
        }
    };
    
    
    
    private class UploadThread extends Thread {
        Handler mHandler;
       
        UploadThread(Handler h) {
            mHandler = h;
        }
       
        public void run(){
        	SearchServie service = SearchServie.getInstance();
    		List<ListItem> items = service.getItems();
    	    ListItem item = items.get(mID);
    	    int result = -1;
    	    Long newImageId = 0L;
        	try { 	    	
    	    	File image = getFileStreamPath(CameraScreen.TEMP_IMAGE_FILE_NAME);
    	    	newImageId = Uploader.upload(image, getSavedAlias(), item.getXmlLink(), uploadedImageCoordinate);
    	    	result = 1;
        	//	ImageAdapter.addImageToCache(newImageId, image);
    		} catch (Exception e) {
    			e.printStackTrace();
    			// returning -1
    		}

    		Message m = new Message();
    		m.what = result;
    		m.obj = newImageId;
        	mHandler.sendMessage(m);
        }

    }
    

	final Handler imageScrollHandler = new Handler()  {        
        public void handleMessage(Message msg)  {
        	Gallery g = (Gallery) findViewById(R.id.gallery);
    	 	
    	    if(mGalleryImageIds.size() > 0){
    	    	View v = findViewById(R.id.gallery_wrapper_view);
    	    	v.setVisibility(View.VISIBLE);
    	    	View v2 = findViewById(R.id.no_images_text);
    	    	v2.setVisibility(View.GONE);
    	    	if(mGalleryImageIds.size() > 1){
    	    		TextView today = (TextView) findViewById(R.id.today_txt);
    	    		today.setText("Tidigare uppladdade bilder: ("+mGalleryImageIds.size()+" bilder, svep åt sidan för att visa alla) ");
    	    	}
    	    } else {
    	    	View v = findViewById(R.id.no_images_text);
    	    	v.setVisibility(View.VISIBLE);
    	    }
    
    	    ImageAdapter imageScroll = new ImageAdapter(DetailScreen.this, mGalleryImageIds);
    	    g.setAdapter(imageScroll);
    	    
    	    galleryLoaded = true;
    	    if(mainImageLoaded){
    	    	mProgressDialog.dismiss();
    	    	findViewById(R.id.detail_view).setVisibility(View.VISIBLE);
    	    }
        		
        }
    };
    

    private class UpdateGalleryScrolLThread extends Thread {
    	Handler mHandler;

    	UpdateGalleryScrolLThread(Handler h) {
    		mHandler = h;
    	}

    	public void run() {
    		SearchServie service = SearchServie.getInstance();
    		List<ListItem> items = service.getItems();
    		final ListItem item = items.get(mID);
    		mGalleryImageIds = Uploader.getImagesByUri(item.getXmlLink());
    		Log.e("TIDSMASKIN", item.getXmlLink() + " has " + mGalleryImageIds.size() + " images");
    		mHandler.sendEmptyMessage(1);
    	}
    }
    
    final Handler imageLoaderHandler = new Handler() {        
        public void handleMessage(Message msg) {
        	if (msg.what == 0){
        		loadItemInfo();
			}
        	mainImageLoaded = true;
        	if(galleryLoaded){
        		mProgressDialog.dismiss();
        		findViewById(R.id.detail_view).setVisibility(View.VISIBLE);
        	}
        		
        }
    };
    
    
    /** A nested thread that downloads the next image to be display. */
    private class ImageLoaderThread extends Thread {
        Handler mHandler;
       
        ImageLoaderThread(Handler h) {
            mHandler = h;
        }
       
        public void run(){
        	SearchServie service = SearchServie.getInstance();
    		List<ListItem> items = service.getItems();
    		if(items == null){
    			finish();
    		}
    		ListItem item = items.get(mID);
    		mImage = null;
    		mThumbnail = null;
    		if (!item.getImages().isEmpty()){
    			mImage = ImageUtils.downloadImage(item.getImages().get(0));
            }
            else{
            	mThumbnail = ImageUtils.downloadImage(item.getThumbnailURL());
            }

        	mHandler.sendEmptyMessage(0);
        }
    }
}
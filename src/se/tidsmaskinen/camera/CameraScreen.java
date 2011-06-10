package se.tidsmaskinen.camera;



import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import se.android.R;
import se.tidsmaskinen.ksamsok.ListItem;
import se.tidsmaskinen.ksamsok.SearchServie;
import se.tidsmaskinen.utils.ImageUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CameraScreen extends Activity {

	public static final String TEMP_IMAGE_FILE_NAME = "tempCamImg";
	private CameraPreview mPreview;
	private boolean imgTaken = false;
	private SeekBar mSeekBar;
	private Bitmap mOverlayImage;
	private DrawOnTop mDrawOnTop;
	
	private ImageLoaderThread downloadImageThread;
	private boolean overlayImageDownloaded = false;
	
	private int mRotation;
	private OrientationEventListener mOrientationEventListener;

	private LocationManager mLocationManager;
	private final LocationListener mGpsLocationListener = new CustomLocationListener();
	private final LocationListener mNetworkLocationListener = new CustomLocationListener();
	private Location mCurrentLocation;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
   
        mOrientationEventListener = (new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
	        @Override
	        public void onOrientationChanged(int aangle) {
	        	mRotation = aangle;
	           
	        }
        });
        
        
    	mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    	startLocationListening();
        
      
        mPreview = new CameraPreview(this);
        mDrawOnTop = new DrawOnTop(this);
        
        setContentView(mPreview);
        
        mSeekBar = new SeekBar(this);
        mSeekBar.setMax(254);
        mSeekBar.setProgress(100);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {	
				if(overlayImageDownloaded)
					mDrawOnTop.invalidate();
				
			}
		});
        
  
        
        Button b = new Button(this);
        b.setOnClickListener(mCameraClickListener);
        b.setBackgroundDrawable(getResources().getDrawable((R.drawable.camera)));     
        b.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        LinearLayout bottomWrapper = new LinearLayout(this);
        bottomWrapper.setOrientation(LinearLayout.VERTICAL);
        bottomWrapper.setGravity(Gravity.BOTTOM);
        bottomWrapper.setHorizontalGravity(Gravity.CENTER);
        bottomWrapper.addView(b);
        
        addContentView(mDrawOnTop, 			new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        bottomWrapper.addView(mSeekBar, 		new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        addContentView(bottomWrapper, 	new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
    }
    
    
    @Override
    public void onResume() {
    	super.onResume();
    	startLocationListening();
      if (mOrientationEventListener.canDetectOrientation()){
    	  mOrientationEventListener.enable();
      } else {
        //handle the fact that you can't detect the orientation
      }
    }
    
	@Override
	protected void onStop() 
	{
		stopLocationListening();
		super.onStop();
	}
    
	private void stopLocationListening() {
		mLocationManager.removeUpdates(mGpsLocationListener);
		mLocationManager.removeUpdates(mNetworkLocationListener);
	}
	
	private void startLocationListening(){
 	    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGpsLocationListener);
 	    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mNetworkLocationListener);
	}
    
    
    private OnClickListener mCameraClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!imgTaken){
				imgTaken = true;
        		mPreview.mCamera.takePicture(null, mPictureCallback, mPictureCallback);
			}
		}
	};

    @Override
    protected void onPause() {
      super.onPause();
      mOrientationEventListener.disable();
      stopLocationListening();
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {
			if (imageData != null) {
				Log.i("TIDSMASKIN", "imageData: " + imageData.length);
				
				int rotation = getClosestRotation(mRotation);
				byte[] rotatedImageData = ImageUtils.rotate(imageData, rotation + 90);
				FileOutputStream fos = null;
				try {
					fos = openFileOutput(TEMP_IMAGE_FILE_NAME, Context.MODE_PRIVATE);
					fos.write(rotatedImageData);
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if(fos != null){
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}	
				
				Intent dataHolder = new Intent();
				if(mCurrentLocation != null){
					dataHolder.putExtra("coordinate", mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
				}
				setResult(RESULT_OK, dataHolder);
				
				finish();
			}
		}
	};
	
	
	/** The location listener that handles all the changes in location */
    private class CustomLocationListener implements LocationListener  {
        @Override
        public void onLocationChanged(Location location) 
        {
        	Log.i("TIDSMASKINEN","got new location: " + location);
        	if(isBetterLocation(location, mCurrentLocation)){
        		Log.i("TIDSMASKINEN", location + " is better then " + mCurrentLocation);
        		mCurrentLocation = location;
        	}
        	
        }
        
        private static final int TWO_MINUTES = 1000 * 60 * 2;

        /** Determines whether one Location reading is better than the current Location fix
          * @param location  The new Location that you want to evaluate
          * @param currentBestLocation  The current Location fix, to which you want to compare the new one
          */
        protected boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
            // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
            return false;
        }

        /** Checks whether two providers are the same */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
              return provider2 == null;
            }
            return provider1.equals(provider2);
        }
       

		public void onProviderDisabled(String provider){}

	    public void onProviderEnabled(String provider) {}

	    public void onStatusChanged(String provider, int status, Bundle extras) {}	
    }
	
	
	private int getClosestRotation(int rotation) {
		// 0 90 180 270 360
		Log.i("TIDSMASKIN", "angle input: " + rotation);
		if(rotation < 45){
			rotation = 0;
		} else if( rotation < 135){
			rotation = 90;
		} else if (rotation < 225){
			rotation = 180;
		} else if ( rotation < 315){
			rotation = 270;
		} else {
			rotation = 0;
		}
		Log.i("TIDSMASKIN", "angle output: " + rotation);
		return rotation;
	}
	
	/** A nested thread that downloads the next image to be display. */
    private class ImageLoaderThread extends Thread {
        Handler mHandler;
       
        ImageLoaderThread(Handler h) {
            mHandler = h;
        }
       
        public void run(){
			SearchServie service = SearchServie.getInstance();
			List<ListItem> items = service.getItems();
			ListItem item = items.get(getIntent().getIntExtra("Id", 0));

			Bitmap image = null;
			if(item.getImages().size() > 0){
				image = ImageUtils.downloadImage(item.getImages().get(0));
			} else {
				image = item.getThumbnail();
			}

			if(image != null){
				Display display = getWindowManager().getDefaultDisplay(); 
				int screenWidth = display.getWidth();
				int screenHeight = display.getHeight();
				boolean widerThenHigher = image.getHeight() < image.getWidth();
	
				Log.i("Tidsmaskin", "screenWidth: " + screenWidth);
				Log.i("Tidsmaskin", "screenHeight: " + screenHeight);
				Log.i("Tidsmaskin", "image.getHeight(): " + image.getHeight());
				Log.i("Tidsmaskin", "image.getWidth(): " + image.getWidth());
	
				if(widerThenHigher){
					mOverlayImage = ImageUtils.modify(image, screenHeight, screenWidth , 90);
				} else {
					mOverlayImage = ImageUtils.modify(image, screenWidth, screenHeight, 0);
				}
				
				overlayImageDownloaded = true;
				
	
				Log.i("Tidsmaskin", "modifiedImage.getHeight(): " + mOverlayImage.getHeight());
				Log.i("Tidsmaskin", "modifiedImage.getWidth(): " + mOverlayImage.getWidth());
	
	        	mHandler.sendEmptyMessage(0);
			}
        }
    }
	
    
    class DrawOnTop extends View {

    	public DrawOnTop(Context context) {
            super(context);
                
        }

    	@Override
    	protected void onDraw(Canvas canvas) {
    		
    		if(mOverlayImage == null && !overlayImageDownloaded && downloadImageThread == null){
    			downloadImageThread = new ImageLoaderThread(new Handler(){
    				 public void handleMessage(Message msg) {
    					 invalidate();
    				 }
    			});
    			downloadImageThread.start();
    		} else if(mOverlayImage != null){
    			Paint paint = new Paint();
        		paint.setAlpha(mSeekBar.getProgress());
    			canvas.drawBitmap(mOverlayImage, 0, 0, paint);
    		}

    		super.onDraw(canvas);
    	}

    }
}
	

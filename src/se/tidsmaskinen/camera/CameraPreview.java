package se.tidsmaskinen.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Display display;
	Camera mCamera;

	private boolean isPreviewRunning = false;
	
	public CameraPreview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	

	public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
		Log.e("TIDSMASKIN", "surfaceCreated()");
		
		if(mCamera != null)return;
		
        mCamera = Camera.open();
        
        //mCamera.setDisplayOrientation(90);
        
        try {
        	Camera.Parameters parameters = mCamera.getParameters();
 
        	
        	for(Size size : parameters.getSupportedPictureSizes()){
        		Log.e("TIDSMASKIN", "size: " + size.height + " - " + size.width);
        	}
        	// max 800 x 480
        	if(!parameters.getSupportedPictureSizes().isEmpty()){
        		Size size = null;
        		for(Size s : parameters.getSupportedPictureSizes()){
        			Log.e("TIDSMASKIN", "size: " + s.height + " - " + s.width);
        			if(s.width <= 800 || s.height <= 800){
        				size = s;
        				Log.e("TIDSMASKIN", "valde size: " + s.height + " - " + s.width);
        				break;
        			}
        		}
        		if(size == null){
        			size = parameters.getSupportedPictureSizes().get(0); 
        		}
        		//int index = parameters.getSupportedPictureSizes().size() - 2;
        		//Size size = parameters.getSupportedPictureSizes().get(index > 0 ? index : 0);
            	parameters.setPictureSize(size.width, size.height);
        	}
        	
	        mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Log.e("TIDSMASKIN", "surfaceChanged: " + format + ", " + width + ", " + height);
		if (isPreviewRunning) {
			mCamera.stopPreview();
		}
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			//  buggar ur utan + 3
			//Size size = parameters.getSupportedPreviewSizes().get(0);
			int w = parameters.getPreviewSize().width;
			int h = parameters.getPreviewSize().height;
			parameters.setPreviewSize(w, h);//width, height );

			mCamera.setParameters(parameters);
			mCamera.startPreview();
		} catch(Exception e) {
			Log.d("TIDSMASKINEN", "Cannot start preview", e);    
		}
		isPreviewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		if(isPreviewRunning && mCamera != null) {
			if(mCamera!=null) {
				mCamera.stopPreview();
				mCamera.release();  
				mCamera = null;
			}
			isPreviewRunning = false;
		}
	}
}

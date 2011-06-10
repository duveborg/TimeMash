package se.tidsmaskinen.detail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.tidsmaskinen.utils.ImageUtils;
import se.tidsmaskinen.utils.Uploader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    //private int mGalleryItemBackground;
    private Context mContext;

    private List<Long> imageIds = new ArrayList<Long>(); 
    public static Map<Long, Bitmap> cache = new HashMap<Long, Bitmap>();
    
    public ImageAdapter(Context c, List<Long> imageIds) {
        this.mContext = c;
        this.imageIds = imageIds;
       // TypedArray a = obtainStyledAttributes(R.styleable.HelloGallery);
        //mGalleryItemBackground = a.getResourceId(
          //      R.styleable.HelloGallery_android_galleryItemBackground, 0);
        //a.recycle();
        

    }

    public int getCount() {
        return imageIds.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);
        Long imageId = imageIds.get(position);
        Bitmap image = null;
        if((image = cache.get(imageId)) == null){
        	String url = Uploader.SERVER_BASE_URL + "/image/" + imageId + "/thumbnail";
        	image = ImageUtils.downloadImage(url);
        	cache.put(imageId, image);
        }
        
        i.setImageBitmap(image);
        //i.setImageResource(mImageIds[position]);
        //i.setLayoutParams(new Gallery.LayoutParams(150, 100));
        i.setScaleType(ImageView.ScaleType.FIT_XY); // FIT_XY FIT_CENTER
      //  i.setAdjustViewBounds(true);
        //i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }

	public static void addImageToCache(Long newImageId, File imageFile) {
		FileInputStream in = null;
    	try {
			in = new FileInputStream(imageFile);
	    	Bitmap imgBitmap = BitmapFactory.decodeStream(in);
	    	cache.put(newImageId, imgBitmap);
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
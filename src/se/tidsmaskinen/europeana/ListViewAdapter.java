package se.tidsmaskinen.europeana;

import java.util.List;
import se.tidsmaskinen.detail.DetailScreen;
import se.android.R;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends ArrayAdapter<ListItem> {

    public ListViewAdapter(Activity activity, List<ListItem> items) 
    {
        super(activity, 0, items);
    }
    
    /**
     * Returns the actual list view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        Activity activity = (Activity) getContext();
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate the views from XML
        View rowView = inflater.inflate(R.layout.list_item, null);
        rowView.setId(position);
        ListItem item = getItem(position);
        
        ImageView thumbnail = (ImageView) rowView.findViewById(R.id.thumbnail);

        if (item.getThumbnail() != null)
        {
        	thumbnail.setImageBitmap(item.getThumbnail());
        }
        /**
        else
        {
        	if (item.getThumbnailURL() != null)
        	{
        		Bitmap thumb = DownloadImage(item.getThumbnailURL());
        		if (thumb != null)
        		{
        			item.setThumbnail(thumb);
        			thumbnail.setImageBitmap(thumb); 
        		}
        	}
        }
        */

        TextView headline = (TextView) rowView.findViewById(R.id.headline);
        headline.setText(item.getTitle());
        TextView type = (TextView) rowView.findViewById(R.id.description);
        type.setText(item.getDescription());
        
        rowView.setOnClickListener
        (
        		new OnClickListener() 
        		{
					@Override
					public void onClick(View v) 
					{
						Intent intent = new Intent();
						intent = new Intent(v.getContext(), DetailScreen.class);
						intent.putExtra("Id", v.getId());
						v.getContext().startActivity(intent);
					}
				}
        );
        
        return rowView;
    } 
}
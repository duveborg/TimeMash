package se.tidsmaskinen.sok;

import java.util.List;

import se.tidsmaskinen.europeana.ListItem;
import se.tidsmaskinen.europeana.ListViewAdapter;
import se.tidsmaskinen.europeana.SearchServie;

import se.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ResultScreen extends Activity 
{	
	private SearchServie service = SearchServie.getInstance();
	private ProgressDialog mProgressDialog;
	private ProgressThread mProgressThread;
	private int mPage = 1;
	private boolean mButtonLock = false;		//Locks the button to prevent multiple activations
	
	/**
	 * Handles the on click event for the next/previous buttons. 
	 * Loads a previous set of data or makes an additional search with the 
	 * same parameters as before but another selection set.
	 */
	private OnClickListener onClickListener = new OnClickListener() 
	{
		public void onClick(View v) 
		{
			if (mButtonLock != true)
			{
			if (v.getId() == findViewById(R.id.arrowPre).getId()) 
			{
				//Previous
				mButtonLock = true;
				if (mPage > 1)
				{
					mPage --;
		            mProgressDialog = new ProgressDialog(ResultScreen.this);
		            mProgressDialog.setMessage("Laddar...");
		            mProgressDialog.show();
		            mProgressThread = new ProgressThread(handler);
					mProgressThread.start();
				}
				else
				{
					mButtonLock = false;
				}
			}
			if (v.getId() == findViewById(R.id.arrowNext).getId()) 
			{
				//Next
				mButtonLock = true;
				if (mPage < service.getTotalPages())
				{
					mPage ++;
					mProgressDialog = new ProgressDialog(ResultScreen.this);
					mProgressDialog.setMessage("Laddar...");
					mProgressDialog.show();
					mProgressThread = new ProgressThread(handler);
					mProgressThread.start();
				}
				else
				{
					mButtonLock = false;
				}
			}
			}
		}
	};
	
	/**
	 * Makes an list view from the search results to be displayed to the user. 
	 */
	private void loadData()
	{
		List<ListItem> items = service.getItems();
		ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(new ListViewAdapter(this, items));
		
		TextView selection = (TextView) findViewById(R.id.selection);
		selection.setText("Sida " +mPage +" av " +service.getTotalPages());
		TextView total = (TextView) findViewById(R.id.total);
		total.setText("Totalt " +SearchServie.getInstance().getTotal()+"st objekt");
		mButtonLock = false;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		//loadData();
				
		findViewById(R.id.arrowPre).setOnClickListener(onClickListener);
		findViewById(R.id.arrowNext).setOnClickListener(onClickListener);
	}
	
    /** 
     * Define the Handler that receives messages from the nested thread and 
     * update the GUI accordingly. In this case handling different 
     * search results and displaying messages to the user if the search 
     * did not give any results.
     */
    final Handler handler = new Handler() 
    {        
        public void handleMessage(Message msg) 
        {
        	if (msg.what == 0)
        	{
        		loadData();
			}
        	else
        	{
        		Toast.makeText(getApplicationContext(), "Ingen kontakt med servern.\nKontrollera ditt nät och var god försök igen.", 3000).show();
        		mButtonLock = false;
        	}
        	mProgressDialog.dismiss();
        }
    };
	
    /** A nested thread that handles the actual additional search. */
    private class ProgressThread extends Thread {
        Handler mHandler;
       
        ProgressThread(Handler h) 
        {
            mHandler = h;
        }
       
        public void run()
        {
        	Boolean gotData = SearchServie.getInstance().additionalSearch(mPage);
        	if (gotData){ mHandler.sendEmptyMessage(0); }
        	else{ mHandler.sendEmptyMessage(1); }        	
        }

    }
}
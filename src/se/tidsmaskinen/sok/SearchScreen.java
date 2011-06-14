package se.tidsmaskinen.sok;

import java.util.ArrayList;
import java.util.List;

import se.tidsmaskinen.europeana.ListItem;
import se.tidsmaskinen.europeana.ListViewAdapter;
import se.tidsmaskinen.europeana.SearchServie;
import se.tidsmaskinen.map.MapScreen;

import se.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchScreen extends Activity 
{	
	private SearchServie service = SearchServie.getInstance();
	private ProgressDialog mProgressDialog;
	private ProgressThread mProgressThread;
	private SearchThread mSearchThread;
	private int mPage = 1;
	private String mQuery;
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

			if( v == findViewById(R.id.submitButton))
			{
				//Check to see if there is a search string
				TextView searchText = (TextView) findViewById(R.id.search_text);
				
				if(!"".equals(searchText.getText().toString()))
				{
					mQuery = searchText.getText().toString();
		            mProgressDialog = ProgressDialog.show(SearchScreen.this, "Please wait...", "Loading...", true , true, onSearchCancel);
		            
					InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
		    		
		    		findViewById(R.id.arrowNext).setVisibility(8);
		    		findViewById(R.id.arrowPre).setVisibility(8);
		            mPage = 1;
		            
		            mSearchThread = new SearchThread(handler);
					mProgressDialog.show();
					mSearchThread.start();
				}
			}
				
			if (v.getId() == findViewById(R.id.arrowPre).getId()) 
			{
				//Previous
				mButtonLock = true;
				if (mPage > 1)
				{
					mPage --;
					mProgressDialog = ProgressDialog.show(SearchScreen.this, "Please wait...", "Loading...", true , true, onSearchCancel);
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
					mProgressDialog = ProgressDialog.show(SearchScreen.this, "Please wait...", "Loading...", true , true, onSearchCancel);
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
	
	private OnCancelListener onSearchCancel = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			SearchServie.getInstance().cancelSearch();
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
		
		Toast.makeText(getApplicationContext(), "Page " + mPage + " of " + SearchServie.getInstance().getTotalPages(), Toast.LENGTH_SHORT).show();
		if(SearchServie.getInstance().getTotalPages() > mPage)
		{
			findViewById(R.id.arrowNext).setVisibility(0);
		}
		else
		{
			findViewById(R.id.arrowNext).setVisibility(8);
		}
		if(mPage != 1)
		{
			findViewById(R.id.arrowPre).setVisibility(0);
		}
		else
		{
			findViewById(R.id.arrowPre).setVisibility(8);
		}
		mButtonLock = false;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		ImageButton submitButton = (ImageButton) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(onClickListener);
		
		ImageButton backButton = (ImageButton) findViewById(R.id.back_to_map);
		backButton.setOnClickListener(toMapListener);
		//loadData();
				
		findViewById(R.id.arrowPre).setOnClickListener(onClickListener);
		findViewById(R.id.arrowNext).setOnClickListener(onClickListener);
	}
	
    private OnClickListener toMapListener = new OnClickListener() {
        public void onClick(View v) {
        	SearchScreen.this.finish();
        }
    };
	
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
        	if (msg.what == 1)
        	{
        		loadData();
			}
        	else if (msg.what == -1)
        	{
        		Toast.makeText(getApplicationContext(), "No contact with server. \nPlease check your network and please try again.", 3000).show();
        		mButtonLock = false;
        	}
        	mProgressDialog.dismiss();
        }
    };
    
    /** A nested thread that handles the actual search. */
    private class SearchThread extends Thread {
        Handler mHandler;
       
        SearchThread(Handler h) 
        {
            mHandler = h;
        }
       
        public void run()
        {
        	int gotData = SearchServie.getInstance().search(mQuery, false, false, "", "", null);
        	mHandler.sendEmptyMessage(gotData);
        }

    }
	
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
        	if (gotData){ mHandler.sendEmptyMessage(1); }
        	else{ mHandler.sendEmptyMessage(-1); }        	
        }

    }
}
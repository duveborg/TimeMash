package se.tidsmaskinen.intro;

import se.android.R;
import android.app.Activity;
import android.os.Bundle;

public class InfoScreen extends Activity {	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
	}
}
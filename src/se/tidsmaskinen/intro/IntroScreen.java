package se.tidsmaskinen.intro;

import se.android.R;
import se.tidsmaskinen.map.MapScreen;
import se.tidsmaskinen.sok.SearchScreen;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class IntroScreen extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN );
		setContentView(R.layout.intro);
	}
	
	private boolean touched = false;
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP && !touched){
			touched = true;
			Intent myIntent = new Intent(IntroScreen.this, MapScreen.class);
	        startActivity(myIntent);
	        finish();
		}
		return super.onTouchEvent(event);
    }
	
}

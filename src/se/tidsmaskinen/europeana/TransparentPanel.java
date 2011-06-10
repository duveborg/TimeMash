package se.tidsmaskinen.europeana;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TransparentPanel extends LinearLayout 
{
	private Paint mInnerPaint, mBorderPaint;

	public TransparentPanel(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init();
	}

	public TransparentPanel(Context context) 
	{
		super(context);
		init();
	}

	private void init() {
		mInnerPaint = new Paint();
		mInnerPaint.setARGB(225, 175, 175, 175); // light gray
		mInnerPaint.setAntiAlias(true);

		mBorderPaint = new Paint();
		mBorderPaint.setARGB(255, 255, 255, 255); // black
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setStyle(Style.STROKE);
		mBorderPaint.setStrokeWidth(2);
	}

	public void setInnerPaint(Paint innerPaint) {
		this.mInnerPaint = innerPaint;
	}

	public void setBorderPaint(Paint borderPaint) {
		this.mBorderPaint = borderPaint;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {

		RectF drawRect = new RectF();
		drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

		canvas.drawRoundRect(drawRect, 5, 5, mInnerPaint);
		canvas.drawRoundRect(drawRect, 5, 5, mBorderPaint);

		super.dispatchDraw(canvas);
	}
}
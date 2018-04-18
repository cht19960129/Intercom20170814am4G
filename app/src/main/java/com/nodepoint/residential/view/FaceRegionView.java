package com.nodepoint.residential.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by kangung on 2017.11.07.
 */

public class FaceRegionView extends View {

	public Context mContext;
	public int nMode = 0;
	int nGap = 60;
	int cameraWidth = 0;
	int cameraHeight = 0;


	public FaceRegionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		if (isInEditMode()) {
			return;
		}
	}

	public void setCameraDimension(int width, int height)
	{
		this.cameraHeight = height;
		this.cameraWidth = width;
		invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int cx = canvas.getWidth();
		int cy = canvas.getHeight();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3.0f);

		int rx, ry, rx1, ry1;
		int CX = (cx>cy)?cy:cx;
		rx = (cx-CX*3/4)/2;rx1 = rx+CX*3/4;
		ry = (cy-CX*3/4)/2;ry1 = ry+CX*3/4;

		canvas.drawRect(rx, ry, rx1, ry1, paint);
	}
}

package com.unitec.fingerpaintapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

	/** drawing and canvas paint*/
	private Paint drawingPaint, canvasPaint;
	/** initial color*/
	private int paintColor = 0xFF660000;
	/** define a drawing canvas */
	private Canvas drawingCanvas;
	/** canvas bitmap */
	private Bitmap canvasBitmap;
	/** 1 triangle, 2 square, 3 circle */
	private int shapes = 1;
	/** initial erase */
	private boolean erase = false;
	/** setup the maximum touch points */
	private static final int MAX_TOUCHPOINTS = 5;

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
	}

	/**
	 * 1. Get drawing area setup for interaction.
	 * 
	 * 2. Set default properties to drawingPaint
	 */
	private void setupDrawing() {
		// get drawing area setup for interaction
		drawingPaint = new Paint();

		drawingPaint.setColor(paintColor);

		resetDrawPaint();

		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}

	/**
	 * Reset draw paint.
	 */
	private void resetDrawPaint() {
		drawingPaint.setAntiAlias(true);
		drawingPaint.setStrokeWidth(2);
	}
	
	/**
	 * Set new color to the palette.
	 * @param newColor
	 */
	public void setColor(String newColor) {
		// set color
		invalidate();
		setErase(false);
		paintColor = Color.parseColor(newColor);
		drawingPaint.setColor(paintColor);
	}

	/**
	 * Set new shape.
	 * @param newShape
	 */
	public void setShape(String newShape) {
		invalidate();
		setErase(false);
		if (newShape != null && newShape.equals("triangle")) {
			this.shapes = 1;
		} else if (newShape != null && newShape.equals("square")) {
			this.shapes = 2;
		} else if (newShape != null && newShape.equals("circle")) {
			this.shapes = 3;
		}
	}

	/**
	 * Set erase.
	 * @param isErase
	 */
	public void setErase(boolean isErase) {
		// set erase true or false
		erase = isErase;
		if (erase) {
			drawingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		} else {
			resetDrawPaint();
			drawingPaint.setXfermode(null);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// view given size
		super.onSizeChanged(w, h, oldw, oldh);

		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawingCanvas = new Canvas(canvasBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw view
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	}

	/**
	 * Set a new paint size.
	 * @param newSize
	 */
	public void setPaintSize(float newSize) {
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize,
				getResources().getDisplayMetrics());
		drawingPaint.setStrokeWidth(pixelAmount);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// detect user touch
		// get the x,y coordinates of the MotionEvent.ACTION_MOVE event
		float touchX = event.getX();
		float touchY = event.getY();
		
		/** Obtain the pointer count */
		int pointerCount = event.getPointerCount();
        if (pointerCount > MAX_TOUCHPOINTS) {
                pointerCount = MAX_TOUCHPOINTS;
        }
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if(event.getPressure() > 0) {
					drawingPaint.setStrokeWidth(drawingPaint.getStrokeWidth() + 
							event.getPressure(event.getPointerCount() - 1));
				}
				for (int i = 0; i < pointerCount; i++) {
					touchX = event.getX(i);
					touchY = event.getY(i);
					
					drawingShapes(touchX, touchY);
				}
				
				break;
			case MotionEvent.ACTION_MOVE:
				if(event.getPressure() > 0) {
					drawingPaint.setStrokeWidth(drawingPaint.getStrokeWidth() + 
							event.getPressure(event.getPointerCount() - 1));
				}
				for (int i = 0; i < pointerCount; i++) {
					touchX = event.getX(i);
					touchY = event.getY(i);
					
					drawingShapes(touchX, touchY);
				}
				break;
			default:
				return false;
		}

		invalidate();// force a screen re-draw
		return true;// actually consume the event
	}
	
	/** 
	 * Drawing shapes based on user touch points.
	 * @param touchX x value
	 * @param touchY y value
	 */
	private void drawingShapes(float touchX, float touchY) {
		switch (this.shapes) {
		case 1:
			Path path = new Path();
			path.moveTo(touchX, touchY + 30);
			path.lineTo(touchX + 60, touchY);
			path.lineTo(touchX, touchY - 30);
			path.close();
			drawingCanvas.drawPath(path, drawingPaint);
			break;
		case 2:
			drawingCanvas.drawRect(touchX, touchY, touchX + 40, touchY + 40, drawingPaint);
			break;
		case 3:
			drawingCanvas.drawCircle(touchX, touchY, 15, drawingPaint);
			break;
	}
	}

	public void resetDrawing() {
		drawingCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}
}

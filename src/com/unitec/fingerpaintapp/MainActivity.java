package com.unitec.fingerpaintapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.example.fingerpaintapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

	private DrawingView drawingView;
	private ImageButton currentPalette;
	private ImageView currentShape;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		drawingView = (DrawingView) findViewById(R.id.drawing);

		Button btnSave = (Button) this.findViewById(R.id.save_btn);
		btnSave.setOnClickListener(this);

		Button btnReset = (Button) this.findViewById(R.id.reset_btn);
		btnReset.setOnClickListener(this);

		Button btnExit = (Button) this.findViewById(R.id.exit_btn);
		btnExit.setOnClickListener(this);

		Button btnEmail = (Button) this.findViewById(R.id.email_btn);
		btnEmail.setOnClickListener(this);

		GridLayout paletteLayout = (GridLayout) findViewById(R.id.palette_colors);
		currentPalette = (ImageButton) paletteLayout.getChildAt(0);// Set the
																	// default
																	// palette
																	// to the
																	// first
																	// colors
		currentPalette.setImageDrawable(getResources().getDrawable(R.drawable.palette_pressed));

		LinearLayout shapeLayout = (LinearLayout) findViewById(R.id.shapesControl);
		currentShape = (ImageView) shapeLayout.getChildAt(1);// Set the default
																// shape to the
																// first shapes
		currentShape.performClick();
	}

	/**
	 * Set chosen color which is user clicked one of the palette.
	 * 
	 * @param view
	 */
	public void paletteClicked(View view) {
		// use chosen color
		if (view != currentPalette) {
			// update color
			ImageButton imgView = (ImageButton) view;
			String color = view.getTag().toString();
			drawingView.setColor(color);

			imgView.setImageDrawable(getResources().getDrawable(R.drawable.palette_pressed));
			currentPalette.setImageDrawable(getResources().getDrawable(R.drawable.palette));
			currentPalette = (ImageButton) view;
		}
	}

	/**
	 * Set chosen shape which is user clicked one of three shapes.
	 * 
	 * @param view
	 */
	public void shapeClicked(View view) {
		view.setActivated(!view.isActivated());
		if (view != currentShape) {
			if (currentShape.isActivated()) {
				currentShape.performClick();
			}

			ImageView imgView = (ImageView) view;
			String shape = view.getTag().toString();
			drawingView.setShape(shape);

			currentShape = (ImageView) view;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Handle action bar item clicks here. The action bar will automatically
	 * handle clicks on the Home/Up button, so long as you specify a parent
	 * activity in AndroidManifest.xml.
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menuGallery) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivity(intent);
		} else if (id == R.id.menuExit) {
			AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
			exitDialog.setTitle("Exit Drawing");
			exitDialog.setMessage("Exit drawing, this will lose the current drawing");
			exitDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
					System.exit(0);

					dialog.dismiss();
				}
			});
			exitDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			exitDialog.show();
		} else if (id == R.id.menuErase) {
			currentShape.performClick();

			drawingView.setErase(true);
			drawingView.setPaintSize(20);
		} else if (id == R.id.menuMMS) {
			save();

			File file = new File(Environment.getExternalStorageDirectory() + File.separator + "screenshot.png");

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			intent.putExtra("subject", "Send screenshot MMS");
			intent.putExtra("address", "0212079887");
			intent.putExtra("sms_body", "This is my drawing picture.");
			intent.putExtra(Intent.EXTRA_TEXT, "This is EXTRA_TEXT");
			intent.setType("image/*");
			intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Save picture to Gallery when end users click the Save button.
	 */
	public void save() {

		drawingView.setDrawingCacheEnabled(true);
		drawingView.buildDrawingCache();
		Bitmap bitmap = drawingView.getDrawingCache();
		if (bitmap != null) {
			try {
				String img = Environment.getExternalStorageDirectory() + File.separator + "screenshot.png";
				FileOutputStream out = new FileOutputStream(img);
				File file = new File(img);
				Log.i("Img Save Path: ", Environment.getExternalStorageDirectory() + File.separator + "screenshot.png");
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				drawingView.destroyDrawingCache();
			} catch (Exception e) {
				Log.i("Save Error: ", e.getMessage());
			}
		}
		

	}

	/**
	 * Send email to fengzht@vip.qq.com when end users click Email button.
	 */
	private void sendEmail() {
		save();

		Intent email = new Intent(android.content.Intent.ACTION_SEND);

		File file = new File(Environment.getExternalStorageDirectory() + File.separator + "screenshot.png");
		email.setType("application/octet-stream");

		String[] emailReciver = new String[] { "fengzht@vip.qq.com" };

		String emailTitle = "Android Assignment1 Screenshot";
		String emailContent = "Screenshot on the window.";

		email.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
		email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailTitle);
		email.putExtra(android.content.Intent.EXTRA_TEXT, emailContent);
		email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

		startActivity(Intent.createChooser(email, "Please setup your email account first!"));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.save_btn) {
			// When end users click save button, then show the confirm
			// information.
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setTitle("Save drawing");
			saveDialog.setMessage("Save drawing to device Gallery?");
			saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// save drawing
					drawingView.setDrawingCacheEnabled(true);
					String imgSaved = MediaStore.Images.Media.insertImage(getContentResolver(),
							drawingView.getDrawingCache(), UUID.randomUUID().toString() + ".png", "drawing");
					if (imgSaved != null) {
						Toast savedToast = Toast.makeText(getApplicationContext(), "Drawing saved to Gallery!",
								Toast.LENGTH_SHORT);
						savedToast.show();
					} else {
						Toast unsavedToast = Toast.makeText(getApplicationContext(), "Oops! Image could not be saved.",
								Toast.LENGTH_SHORT);
						unsavedToast.show();
					}

					drawingView.destroyDrawingCache();
				}
			});
			saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			saveDialog.show();
		} else if (v.getId() == R.id.email_btn) {
			// send email button
			sendEmail();

		} else if (v.getId() == R.id.reset_btn) {
			// When end users click reset button, then show the confirm
			// information.
			AlertDialog.Builder resetDialog = new AlertDialog.Builder(this);
			resetDialog.setTitle("Reset drawing");
			resetDialog.setMessage("Reset drawing, this will lose the current drawing");
			resetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					drawingView.resetDrawing();
					dialog.dismiss();
				}
			});
			resetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			resetDialog.show();
		} else if (v.getId() == R.id.exit_btn) {
			// When end users click exit button, then show the confirm
			// information.
			AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
			exitDialog.setTitle("Exit Drawing");
			exitDialog.setMessage("Exit drawing, this will lose the current drawing");
			exitDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
					System.exit(0);

					dialog.dismiss();
				}
			});
			exitDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			exitDialog.show();
		}
	}
}

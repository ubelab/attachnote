package com.agomir.attachnote;

import java.io.File;

import com.agomir.attachnote.utils.FileUtils;
import com.agomir.attachnote.view.FingerPaintDrawableView;


import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	FingerPaintDrawableView fingerPaintView;
	
	@Override
	protected void onDestroy() {
		if(fingerPaintView != null) {
			fingerPaintView.destroy();//recycle bitmaps
		}
		super.onDestroy();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fingerPaintView = new FingerPaintDrawableView(getApplicationContext());
        RelativeLayout.LayoutParams fingerRelativeParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fingerRelativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		
        RelativeLayout root = (RelativeLayout)findViewById(R.id.root_layout);
        root.addView(fingerPaintView,fingerRelativeParams);
        
        //Se in input abbiamo un file mandato con SEND
    	if(getIntent().getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
	    	Bundle b = getIntent().getExtras();
	    	String filePath = b.get(Intent.EXTRA_STREAM).toString();
	    	Log.d("FilePath = ",filePath);
	    	if(filePath != null) {
	    		
	    		if(filePath.contains("content://")) {
	    			final Uri selectedImage = Uri.parse(filePath);
	    			filePath = FileUtils.getRealPathFromURI(selectedImage,this);
	    		}else {
	    			filePath = Uri.decode(filePath).replace("file://","");
	    		}
	    		
	    		if(!new File(filePath).exists()) {
					Toast.makeText(getApplicationContext(), "Impossible to find image.", Toast.LENGTH_SHORT).show();
					return;
				}
	    		
	    		//Leggo solo le dimensioni dell'immagine
	    	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    	    bmOptions.inJustDecodeBounds = false;
	    	    Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
	    	    fingerPaintView.setBackgroundImage(bitmap);
			}
    	}
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}

package com.agomir.attachnote;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.agomir.attachnote.helper.VoiceRecognitionHelper;
import com.agomir.attachnote.listeners.ShakeListener;
import com.agomir.attachnote.utils.FileUtils;
import com.agomir.attachnote.utils.HashingUtils;
import com.agomir.attachnote.view.FingerPaintDrawableView;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 738392362;
	
	private String filePath;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeListener shakeListener;
	
	FingerPaintDrawableView fingerPaintView;
	
	@Override
	protected void onDestroy() {
		if(fingerPaintView != null) {
			fingerPaintView.destroy();//recycle bitmaps
		}
		if(mSensorManager!= null && shakeListener != null) {
			mSensorManager.unregisterListener(shakeListener);
		}
		super.onDestroy();
	}
	
	protected void onResume() {
		super.onResume();
		if(mSensorManager!= null && shakeListener != null) {
			mSensorManager.registerListener(shakeListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	protected void onPause() {
		super.onPause();
		if(mSensorManager!= null && shakeListener != null) {
			mSensorManager.unregisterListener(shakeListener);
		}
	}
	
	 /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
    
    //Salva la nota su file system, associandola al file in questione
    private void saveNote() {
    	Bitmap bitmap = fingerPaintView.getBitmap();
		try {
			//1) CREO UNA CHIAVE UNIVOCA PER QUESTA NOTA
			//Uso una convenzione prestabilita: [MD5(filepath completo)]_[timestamp].png
			String pathMD5 = HashingUtils.md5(filePath);
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String nomeFile = pathMD5+"_"+timeStamp;
			//2) SALVO L'IMMAGINE
			File file = FileUtils.createImageFileForSave(nomeFile);
			boolean saved = FileUtils.saveBitmapPNG(file.getAbsolutePath(), bitmap);
			Log.d("### salvato in",""+file.getAbsolutePath());
			
			if(saved) {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "Note saved to disk", Toast.LENGTH_SHORT).show();
						}
					});
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    //Condivide la nota con applicazioni esterne
    private void shareNote() {
    	//Salvo la bitmap nel canvas sul file system e passo il path all'activity successiva
		Bitmap bitmap = fingerPaintView.getBitmap();
		try {
			File file = FileUtils.createTmpImageFile(true);
			boolean saved = FileUtils.saveBitmapPNG(file.getAbsolutePath(), bitmap);
			if(saved) {
				Uri screenshotUri = Uri.fromFile(file);
				Log.d("SCREENSHOT URI","SCREENSHOT URI="+screenshotUri);
	        	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	        	emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
	        	emailIntent.setType("image/png");
	        	startActivity(Intent.createChooser(emailIntent, getApplicationContext().getString(R.string.menu_share)));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            
            for(String s: matches) {
            	VoiceRecognitionHelper.analyzeVoice(s, fingerPaintView);
            	break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fingerPaintView = new FingerPaintDrawableView(getApplicationContext());
        RelativeLayout.LayoutParams fingerRelativeParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fingerRelativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		
        RelativeLayout root = (RelativeLayout)findViewById(R.id.root_layout);
        root.addView(fingerPaintView,0,fingerRelativeParams);
     
        //Se in input abbiamo un file mandato con SEND
    	if(getIntent().getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
	    	Bundle b = getIntent().getExtras();
	    	filePath = b.get(Intent.EXTRA_STREAM).toString();
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
	    		
	    	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    	    bmOptions.inJustDecodeBounds = false;
	    	    Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
	    	    fingerPaintView.setBackgroundImage(bitmap);
			}
    	}
    	
    	//Registro il listener
    	shakeListener = new ShakeListener() {
    	      @Override
    	      public void onShake()
    	      {
    	        	Log.d("#### SHAKE SHAKE SHAKE","#### SHAKE SHAKE SHAKE");
    	        	fingerPaintView.clearDashboard();
    	      }
	    };
	    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    mSensorManager.registerListener(shakeListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == R.id.menu_voice_recognition) {
    		startVoiceRecognitionActivity();
    	}else if(item.getItemId() == R.id.menu_share) {
    		shareNote();
    	}else if(item.getItemId() == R.id.menu_save) {
    		saveNote();
    	}
    	return true;
    }
}

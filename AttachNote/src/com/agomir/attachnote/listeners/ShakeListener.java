package com.agomir.attachnote.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.hardware.SensorManager;

/**
 * @author mub
 * ShakeListener è un listener che implementa il SensorEventListener del framework Android
 * e serve a catturare l'evento di squotimento del device. Se uno squotimento viene rilevato viene
 * invocato il suo metodo onShake() che essendo astratto obbliga chi istanzia questa classe a darne 
 * una implementazione. Nel nostro caso lo useremo per cancellare la lavagna.
 */
public abstract class ShakeListener implements SensorEventListener {
  private static final int FORCE_THRESHOLD = 350;
  private static final int TIME_THRESHOLD = 100;
  private static final int SHAKE_TIMEOUT = 500;
  private static final int SHAKE_DURATION = 1000;
  private static final int SHAKE_COUNT = 3;

  private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
  private long mLastTime;

  private int mShakeCount = 0;
  private long mLastShake;
  private long mLastForce;

  public abstract void onShake();

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onSensorChanged(SensorEvent event) {
	    if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
	    
		long now = System.currentTimeMillis();
	    
		if ((now - mLastForce) > SHAKE_TIMEOUT) {
	      mShakeCount = 0;
	    }
	    
		if ((now - mLastTime) > TIME_THRESHOLD) {
	      long diff = now - mLastTime;
	      float speed = Math.abs(event.values[SensorManager.DATA_X] +
	                    event.values[SensorManager.DATA_Y] +
	                    event.values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000;
	      
		  if (speed > FORCE_THRESHOLD) {
	        if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
	          mLastShake = now;
	          mShakeCount = 0;
	          onShake();
	        }
	        mLastForce = now;
	      }
	      mLastTime = now;
		  mLastX = event.values[SensorManager.DATA_X];
		  mLastY = event.values[SensorManager.DATA_Y];
		  mLastZ = event.values[SensorManager.DATA_Z];
	    }
  }
}
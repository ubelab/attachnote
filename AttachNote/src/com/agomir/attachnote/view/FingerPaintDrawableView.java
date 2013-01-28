package com.agomir.attachnote.view;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


/**
 * @author mub
 * View personalizzata per il disegno a mano libera
 */
public class FingerPaintDrawableView extends View implements OnTouchListener{

	public static int FULL_ALPHA = 255;
	
	private Context context;
	
	private Paint       mPaint;
	private Bitmap background;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint   mContourPaint;
    
    private int mBitmpaPaintAlpha = 255;
    
    private int VIEW_HEIGHT,VIEW_WIDTH = 0;
	private int CX,CY =0;
	
	float DENSITY = 1.0f;
	
	private boolean isFingerDown = false;
	
	private Rect destRect;
	
	public void destroy() {
		if(background != null && !background.isRecycled()) {
			background.recycle();
			background = null;
		}
		if(mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}
	
    public FingerPaintDrawableView(Context context) {
		super(context);
		this.context = context;
		this.DENSITY = context.getResources().getDisplayMetrics().density;
        mPath = new Path();
        //mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(false);
        mBitmapPaint.setFilterBitmap(false);
        
        mContourPaint = new Paint();
        mContourPaint.setAntiAlias(false);
        mContourPaint.setFilterBitmap(true);
        
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);
        //Questo mi serve per la gomma ad esempio, che se pitturi la trasparenza, cancella quello che c'è sotto.
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        
        setOnTouchListener(this);
	}


	public int getmBitmpaPaintAlpha() {
		return mBitmpaPaintAlpha;
	}

	public void setmBitmpaPaintAlpha(int mBitmpaPaintAlpha) {
		this.mBitmpaPaintAlpha = mBitmpaPaintAlpha;
		this.invalidate();
	}

	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    	VIEW_WIDTH = getWidth();
    	VIEW_HEIGHT = getHeight();
    	CY = VIEW_HEIGHT/2;
    	CX = VIEW_WIDTH/2;
    	
    	//touchNormalization = (float)SCREEN_HEIGHT/(float)MAX_BITMAP_SIZE;
    	destRect = new Rect(CX-(VIEW_WIDTH/2),0,CX+(VIEW_WIDTH/2),VIEW_HEIGHT);
    
    	initializeBitmaps();
    }
	
    public void initializeBitmaps() {
		mBitmap = Bitmap.createBitmap(VIEW_WIDTH, VIEW_HEIGHT, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
    }
    
    public void clearDashboard() {
    	mCanvas.drawColor(Color.WHITE);
    	invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	if(destRect != null && background != null && !background.isRecycled()) {
	    	canvas.drawBitmap(background, null, destRect, mBitmapPaint);
    	}
    	if(destRect != null && mBitmap != null && !mBitmap.isRecycled()) {
	    	canvas.drawBitmap(mBitmap, null, destRect, mBitmapPaint);
    	}
    }
    
    public void setBackgroundImage(Bitmap bitmap){
    	this.background = bitmap;
    }
    
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 2;
    int paintSize = 1;
    private void touch_start(float x, float y) {
    	paintSize = 5;
    	//mPaint.setStrokeWidth(paintSize);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        isFingerDown = true;
    }
    float dx,dy;
    private void touch_move(float x, float y) {
        
    	dx = Math.abs(x - mX);
        dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            mCanvas.drawPath(mPath, mPaint);
        }
    }
    
    private void touch_up() {
    	mPath.lineTo(mX+1, mY+1);//il +1 serve a fare disegnare i punti (toccata e fuga)
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
        //Posizione del dito alzato
        isFingerDown = false;
    }

    float x,y;
    int PAINT_SIZE;
    int left,top,right,bottom;
	public boolean onTouch(View v, MotionEvent event) {
		 x = event.getX();///touchNormalization;
	     y = event.getY();///touchNormalization;
	     PAINT_SIZE = 5;
	     
	     if(x > mX) {
	    	 right = (int)x;
	    	 left = (int)mX; 
	     }else {
	    	 right = (int)mX;
	    	 left = (int)x; 
	     }
	     
	     if(y > mY) {
	    	 top = (int)mY;
	    	 bottom = (int)y; 
	     }else {
	    	 top = (int)y;
	    	 bottom = (int)mY; 
	     }
	     
	     switch (event.getAction()) {
	         case MotionEvent.ACTION_DOWN:
	             touch_start(x, y);
	             //Invalido solo l'intorno di dove ho disegnato, per velocizzare il refresh anche su schermi grandi
	             invalidate((int)(left-PAINT_SIZE),(int)(top-PAINT_SIZE),(int)(right+PAINT_SIZE),(int)(bottom+PAINT_SIZE));
	             break;
	         case MotionEvent.ACTION_MOVE:
	        	 touch_move(x, y);
	        	 invalidate((int)(left-PAINT_SIZE),(int)(top-PAINT_SIZE),(int)(right+PAINT_SIZE),(int)(bottom+PAINT_SIZE));
	             break;
	         case MotionEvent.ACTION_UP://N.B. se sono con 2 dita, questo non viene lanciato al primo dito rilasciato,ma al secondo
	             touch_up();
	             invalidate((int)(left-PAINT_SIZE),(int)(top-PAINT_SIZE),(int)(right+PAINT_SIZE),(int)(bottom+PAINT_SIZE));
	             break;
	     }
	     return true;
	}
  
	public void setColor(int color) {
		mPaint.setColor(color);
	}
	
	public void setBrushSize(int size) {
		mPaint.setStrokeWidth(size);
	}
	
	public int getBrushSize() {
		System.out.println("### "+mPaint.getStrokeWidth());
		return (int)mPaint.getStrokeWidth();
	}

}

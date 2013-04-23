package com.agomir.attachnote;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.agomir.attachnote.utils.FileUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DocEntryActivity extends Activity {
	private static final String TAG = "DocEntryActivity";
	float DENSITY = 1.0f;
	ArtworkAdapter myAdapter;
	private String filePath;
	View footerView;

	@Override
	protected void onResume() {
		ArrayList<File> notes = FileUtils.getDocNotes(filePath);
        myAdapter = new ArtworkAdapter(this, R.layout.list_item, notes);
        final ListView lv = (ListView)findViewById(R.id.artwork_list);
        lv.setAdapter(myAdapter);
        lv.setTextFilterEnabled(true);
        if(notes != null && notes.size() > 0 && !("NO_FILE".equalsIgnoreCase(filePath))) {
        	((TextView)(footerView.findViewById(R.id.footertext))).setText("File: "+filePath);
        }
        else if(notes != null && notes.size() > 0 && "NO_FILE".equalsIgnoreCase(filePath)) {
        	((TextView)(footerView.findViewById(R.id.footertext))).setText("Free notes");
        }
        else if((notes == null || notes.size() == 0) && "NO_FILE".equalsIgnoreCase(filePath)) {
        	((TextView)(footerView.findViewById(R.id.footertext))).setText("There are no free notes");
        }
        else{
        	((TextView)(footerView.findViewById(R.id.footertext))).setText("There are no note for file: "+filePath);
        }
		super.onResume();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savedwork);
 
        this.DENSITY = getApplicationContext().getResources().getDisplayMetrics().density;
  
        final ListView lv = (ListView)findViewById(R.id.artwork_list);
        //Metto un footer alla lista dove mettere eventuali messaggi
        footerView = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_footer, null, false);
		lv.addFooterView(footerView);

        //Se in input abbiamo un file mandato con SEND prendo il suo path
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
					Toast.makeText(getApplicationContext(), "Impossible to find file.", Toast.LENGTH_SHORT).show();
				}
			}
    	}else {
    		filePath = "NO_FILE";//NOTE LIBERE
    	}
    }
    
    private class ArtworkAdapter extends ArrayAdapter<File> {

        private ArrayList<File> items = new ArrayList<File>();

        public ArtworkAdapter(Context context, int textViewResourceId, ArrayList<File> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        public File getItem(int pos) {
        	if(pos >=0 && pos <items.size()) {
        		return items.get(pos);
        	}else return null;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	final ListView lv = (ListView)findViewById(R.id.artwork_list);
        	View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }
            final File o = items.get(position);
            if (o != null) {
            	final ImageView noteImage = (ImageView)v.findViewById(R.id.noteimage);
            	noteImage.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent myIntent = new Intent(DocEntryActivity.this, CanvasActivity.class);
						myIntent.putExtra("filePath", filePath);
						myIntent.putExtra("noteImagePath", o.getAbsolutePath());
						DocEntryActivity.this.startActivity(myIntent);
					}
				});

            	final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(final Message message) {
						try {
							noteImage.setImageBitmap((Bitmap) ((Map)(message.obj)).get("image"));
						}catch (Exception e) {
							e.printStackTrace();
						}
                    }
                };

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                    	BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    	bmOptions.inJustDecodeBounds = false;
                 	    bmOptions.inSampleSize = 4;
                 	    bmOptions.inPurgeable = false;
                    	
                    	Bitmap bitmap = BitmapFactory.decodeFile(o.getAbsolutePath(),bmOptions);
                    	
                    	//Notifico la view
                    	Map<String,Bitmap> map = new HashMap<String,Bitmap>();
                    	map.put("image",bitmap);
                        Message message = handler.obtainMessage(1, map);
                        handler.sendMessage(message);
                    }
                };
                thread.start();
            }
            return v;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.savedwork, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.menu_addnote:
	        	Intent myIntent = new Intent(DocEntryActivity.this, CanvasActivity.class);
				myIntent.putExtra("filePath", filePath);
				DocEntryActivity.this.startActivity(myIntent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
    }
}

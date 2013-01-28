package com.agomir.attachnote.helper;

import android.graphics.Color;

import com.agomir.attachnote.view.FingerPaintDrawableView;

/**
 * @author mub
 * Classe di utilita' per analizzare le parole e eseguire i comandi sulla
 * lavagna
 */
public class VoiceRecognitionHelper {

	public static void analyzeVoice(String message, FingerPaintDrawableView dashboard) {
		
		System.out.println("##### message "+message);
		
		//GESTIONE COLORI
    	if(message.toLowerCase().contains("verde")) {
    		dashboard.setColor(Color.GREEN);
    	}else if(message.toLowerCase().contains("rosso")) {
    		dashboard.setColor(Color.RED);
    	}else if(message.toLowerCase().contains("blu")) {
    		dashboard.setColor(Color.BLUE);
    	}
    	//GESTIONE DIMENSIONE PENNELLO
    	if(message.toLowerCase().contains("increment") && message.toLowerCase().contains("dimension")) {
    		dashboard.setBrushSize(dashboard.getBrushSize() + 5);
    	}
    	if(message.toLowerCase().contains("decrement") && message.toLowerCase().contains("dimension")) {
    		int newdim = dashboard.getBrushSize() - 5;
    		if(newdim <=1) newdim = 1;
    		dashboard.setBrushSize(newdim);
    	}
	}
	
}

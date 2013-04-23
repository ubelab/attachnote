package com.agomir.attachnote.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ChooseColorDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose color")
               .setItems(new String[]{"Red","Green","Blue"}, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		               // The 'which' argument contains the index position
		               // of the selected item
		               }
		        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
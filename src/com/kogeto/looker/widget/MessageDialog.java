package com.kogeto.looker.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kogeto.looker.R;

public class MessageDialog extends AlertDialog {
    Context context;
    String title;
    String message;


    public MessageDialog(Context context, String title, String message){
        super(context);
        this.context = context;
        this.title = title;
        this.message = message;
    }
    
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_dialog);
        
        TextView title_text = (TextView)findViewById(R.id.title_text);
        TextView message_text = (TextView)findViewById(R.id.message_text);
        
        title_text.setText(title);
        message_text.setText(message);
    }
    
    
    
    public void setPositiveListener(String button_text, View.OnClickListener listener){
    	TextView positive_text = (TextView)findViewById(R.id.positive_text);
    	positive_text.setText(button_text);
    	positive_text.setOnClickListener(listener);
    }

    
    
    public void setPositiveListener(View.OnClickListener listener){
    	TextView positive_text = (TextView)findViewById(R.id.positive_text);
    	positive_text.setOnClickListener(listener);
    }

    
    
    public void setNegativeListener(String button_text, View.OnClickListener listener){
    	TextView negative_text = (TextView)findViewById(R.id.negative_text);
    	negative_text.setText(button_text);
    	negative_text.setOnClickListener(listener);
    }

    
    public void setNegativeListener(View.OnClickListener listener){
    	TextView negative_text = (TextView)findViewById(R.id.negative_text);
    	negative_text.setOnClickListener(listener);
    }
}

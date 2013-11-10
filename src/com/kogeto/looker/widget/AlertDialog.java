package com.kogeto.looker.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.kogeto.looker.R;

public class AlertDialog extends android.app.AlertDialog {
    Context context;
    String title;
    String message;


    public AlertDialog(Context context, String title, String message){
        super(context);
        this.context = context;
        this.title = title;
        this.message = message;
    }
    
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        setContentView(R.layout.alert_dialog);
        
        TextView title_text = (TextView)findViewById(R.id.title_text);
        TextView message_text = (TextView)findViewById(R.id.message_text);
        
        title_text.setText(title);
        message_text.setText(message);
    }
    
    
    
    public void setOKListener(View.OnClickListener listener){
    	TextView positive_text = (TextView)findViewById(R.id.ok_text);
    	positive_text.setOnClickListener(listener);
    }
}

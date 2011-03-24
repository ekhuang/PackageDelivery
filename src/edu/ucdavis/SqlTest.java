package edu.ucdavis;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SqlTest extends Activity {
	// Called when the activity is first created.
	
	   TextView txt;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    // Create a crude view - this should really be set via the layout resources  
	    // but since its an example saves declaring them in the XML.  
	    LinearLayout rootLayout = new LinearLayout(getApplicationContext());  
	    txt = new TextView(getApplicationContext());  
	    rootLayout.addView(txt);  
	    setContentView(rootLayout);  
	    
	    SqlMyServer sqlMyServer = new SqlMyServer();
	    ArrayList<String> logFields = new ArrayList<String>();
	    logFields.add("Username");
	    logFields.add("Password");

	    // Set the text and call the connect function.  
	    txt.setText("Connecting..."); 
	  //call the method to run the data retrieval
	    txt.setText(sqlMyServer.SqlQuery("INSERT INTO USERS VALUES (\"Bob\",\"wert\")")); 
	}
}


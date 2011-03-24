package edu.ucdavis;

import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class Update extends Activity {
	
	private Button update;
	private Button friendButton;
	private Button packageButton;
	
	private String id;
	private double lat = -10;
	private double lon = -10;
	
	LocationManager mlocManager;
	LocationListener mlocListener;
	
	SqlMyServer sqlMyServer;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getStringExtra("LOGIN_ID");
        mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        sqlMyServer = new SqlMyServer();
        sqlMyServer.SqlQuery("update users set Geo_X = " + lat + ", Geo_Y = " + lon + " where alias = \"" + id + "\"");
        setContentView(R.layout.main);
        update = (Button)findViewById(R.id.update);
        update.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onUpdateButtonClicked();
        	}
        });
        friendButton = (Button)findViewById(R.id.friend_requests);
        friendButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onFriendRequestButtonClicked();
        	}
        });
        packageButton = (Button)findViewById(R.id.package_requests);
        packageButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onPackageRequestButtonClicked();
        	}
        });
        updateNotifications();
        checkTransactions();
    }
    
    private void updateNotifications() {
    	String result = sqlMyServer.JsonSqlQuery("SELECT message from notifications where alias = \"" + id + "\"");
        try{
			JSONArray jsonResultArray = new JSONArray(result);
			int len = jsonResultArray.length();
			for(int i=0; i<len; i++){
				final String message = jsonResultArray.getJSONObject(i).get("message").toString();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(message)
		 	       .setCancelable(true)
			 	   .setNegativeButton("OK", new DialogInterface.OnClickListener() {
				       public void onClick(DialogInterface dialog, int id) {
				    	   sqlMyServer.SqlQuery("DELETE from notifications where message = \"" + message + "\"");
				           dialog.cancel();
				       }
			 	   });
				AlertDialog alert = builder.create();
	 		 	alert.show();
			}
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
    }
    
    private void checkTransactions() {
    	String result = sqlMyServer.JsonSqlQuery(
				"SELECT * FROM PACKAGES WHERE " +
				"(holder = \"" + id + "\")");
    	try{
			JSONArray jsonResultArray = new JSONArray(result);
			int len = jsonResultArray.length();
			for(int i=0; i<len; i++){
				String timestamp = jsonResultArray.getJSONObject(i).getString("expiry_time");
				//int pid = jsonResultArray.getJSONObject(i).getInt("pid");
				Timestamp stamp = Timestamp.valueOf(timestamp);
				Timestamp current = new Timestamp(System.currentTimeMillis());
				if (current.compareTo(stamp) > 0) {
					sqlMyServer.SqlQuery("DELETE from users where alias = \"" + id + "\"");
					sqlMyServer.SqlQuery("DELETE from packages where sender = \"" + id + "\" or intermediary = \"" + id + "\" or receiver = \"" + id + "\"");
					sqlMyServer.SqlQuery("DELETE from friends where alias1 = \"" + id + "\" or alias2 = \"" + id + "\"");
					sqlMyServer.SqlQuery("DELETE from requests where sender = \"" + id + "\" or receiver = \"" + id + "\"");
					sqlMyServer.SqlQuery("DELETE from prequests where sender = \"" + id + "\" or receiver = \"" + id + "\"");
					sqlMyServer.SqlQuery("DELETE from notifications where alias = \"" + id + "\"");
					sqlMyServer.SqlQuery("INSERT INTO banlist VALUES (\"" + id + "\")");
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("You have failed to deliver a package on time and are now on the global blacklist.")
			 	       .setCancelable(true)
				 	   .setNegativeButton("OK", new DialogInterface.OnClickListener() {
					       public void onClick(DialogInterface dialog, int id) {
					    	   returnToLogin();
					           dialog.cancel();
					       }
				 	   });
					AlertDialog alert = builder.create();
		 		 	alert.show();
		 		 	break;
				}
			}
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
    }
    
    private void onUpdateButtonClicked() {
    	sqlMyServer.SqlQuery("update users set Geo_X = " + lat + ", Geo_Y = " + lon + " where alias = \"" + id + "\"");
    	updateNotifications();
    	checkTransactions();
    }
    
    private void returnToLogin() {
       Intent i = new Intent(this, Login.class);
 	   startActivity(i);
    }
    
    private void onFriendRequestButtonClicked() {
    	Intent i = new Intent(this, FriendRequest.class);
    	i.putExtra("LOGIN_ID", id);
    	startActivity(i);
    }
    
    private void onPackageRequestButtonClicked() {
    	Intent i = new Intent(this, PackageRequest.class);
    	i.putExtra("LOGIN_ID", id);
    	startActivity(i);
    }

	public class MyLocationListener implements LocationListener
	{
		public void onLocationChanged(Location loc)
		{
			lat = loc.getLatitude();
			lon = loc.getLongitude();
		}
		
		public void onProviderDisabled(String provider)
		{
			Toast.makeText( getApplicationContext(),
			"Gps Disabled",
			Toast.LENGTH_SHORT ).show();
		}
		
		public void onProviderEnabled(String provider)
		{
			Toast.makeText( getApplicationContext(),
			"Gps Enabled",
			Toast.LENGTH_SHORT).show();
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		
		}
	
	}/* End of Class MyLocationListener */

}
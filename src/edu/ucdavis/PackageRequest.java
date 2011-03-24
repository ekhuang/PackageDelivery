package edu.ucdavis;

import java.util.ArrayList;

import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class PackageRequest extends Activity {
	
	private EditText recipientName;
	private Button sendRequest;
	private ListView transactionsList;
	private ListView incomingTransactionsList;	
	
	private String id;
	
	private ArrayList<String> list;
	private ArrayList<Integer> request_pids;
	private ArrayList<String> packageList;
	private ArrayList<Integer> pids;
	
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> packageAdapter;
	
	SqlMyServer sqlMyServer;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.packages);
		
		sqlMyServer = new SqlMyServer();
		
		id = getIntent().getStringExtra("LOGIN_ID");
		
		recipientName = (EditText)findViewById(R.id.recipientField);
		sendRequest = (Button)findViewById(R.id.sendPackageRequest);
		transactionsList = (ListView)findViewById(R.id.transactionsList);
		incomingTransactionsList = (ListView)findViewById(R.id.incomingPackageRequests);
		
		sendRequest.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onSendRequestButtonClicked();
        	}
        });
		
		list = new ArrayList<String>(0);
		packageList = new ArrayList<String>(0);
		pids = new ArrayList<Integer>(0);
		request_pids = new ArrayList<Integer>(0);
		
		String result = sqlMyServer.JsonSqlQuery("SELECT * FROM PREQUESTS WHERE receiver = \"" + id + "\"");
		try{
			JSONArray jsonResultArray = new JSONArray(result);
			int len = jsonResultArray.length();
			for(int i=0; i<len; i++){
				String sender = jsonResultArray.getJSONObject(i).get("sender").toString();
				int type = jsonResultArray.getJSONObject(i).getInt("type"), pid = -1;
				if (type == 1)
					pid = jsonResultArray.getJSONObject(i).getInt("pid");
				System.out.println(sender);
				list.add(sender);
				request_pids.add(pid);
			}
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		result = sqlMyServer.JsonSqlQuery(
				"SELECT * FROM PACKAGES WHERE " +
				"(sender = \"" + id + "\" or receiver = \"" + id + "\" or intermediary = \"" + id + "\")");
		try{
			JSONArray jsonResultArray = new JSONArray(result);
			int len = jsonResultArray.length();
			for(int i=0; i<len; i++){
				int pid = jsonResultArray.getJSONObject(i).getInt("pid");
				String timestamp = jsonResultArray.getJSONObject(i).getString("expiry_time");
				packageList.add(pid + ": expires " + timestamp);
				pids.add(pid);
			}
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , list);
		packageAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , packageList);
		incomingTransactionsList.setAdapter(adapter);
		transactionsList.setAdapter(packageAdapter);
		
		incomingTransactionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		        onListItemClick(v,pos,id);
		    }
		});
		
		transactionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		        onPackagesListItemClick(v,pos,id);
		    }
		});
	}
	
	private String getIntermediary(String sender) {
		double my_x = 0, my_y = 0, s_x = 0, s_y = 0, min = -1;
		
		String result = sqlMyServer.SqlQuery("SELECT Geo_X, Geo_Y FROM users WHERE alias = \"" + id + "\"");
		try{
			JSONObject jsonResult = new JSONObject(result);
			my_x = jsonResult.getDouble("Geo_X");
			my_y = jsonResult.getDouble("Geo_Y");
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}		
		
		result = sqlMyServer.SqlQuery("SELECT Geo_X, Geo_Y FROM users WHERE alias = \"" + sender + "\"");
		try{
			JSONObject jsonResult = new JSONObject(result);
			s_x = jsonResult.getDouble("Geo_X");
			s_y = jsonResult.getDouble("Geo_Y");
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}	
		
		String intermediary = "", alias1 = "", alias2 = "";
		result = sqlMyServer.SqlQuery(
				"SELECT * FROM FRIENDS WHERE " +
				"(alias1 = \"" + sender + "\" and alias2 = \"" + id + "\") or " +
				"(alias1 = \"" + id + "\" and alias2 = \"" + sender + "\")");
		if (!result.equals("")) {
			try{
				JSONObject jsonResult = new JSONObject(result);
				alias1 = jsonResult.get("alias1").toString();
				alias2 = jsonResult.get("alias2").toString();
				System.out.println("alias1 = " + alias1);
				System.out.println("alias2 = " + alias2);
				if (id.equals(alias1) || id.equals(alias2))
					return sender;
			}catch(JSONException e){
				Log.e("JSON_EXCEPTION",e.toString());
			}
		}
		else {
			result = sqlMyServer.JsonSqlQuery(
					"SELECT * FROM FRIENDS WHERE " +
					"(alias1 = \"" + id + "\" or alias2 = \"" + id + "\")");
			try{
				JSONArray jsonResultArray = new JSONArray(result);
				int len = jsonResultArray.length();
				String friend = "";
				for(int i=0; i<len; i++){
					alias1 = jsonResultArray.getJSONObject(i).get("alias1").toString();
					alias2 = jsonResultArray.getJSONObject(i).get("alias2").toString();
					if (id.equals(alias2))
						friend = alias1;
					else
						friend = alias2;
					result = sqlMyServer.SqlQuery(
							"SELECT * FROM FRIENDS WHERE " +
							"(alias1 = \"" + sender + "\" and alias2 = \"" + friend + "\") or " +
							"(alias1 = \"" + friend + "\" and alias2 = \"" + sender + "\")");
					JSONObject jsonResult = new JSONObject(result);
					alias1 = jsonResult.get("alias1").toString();
					alias2 = jsonResult.get("alias2").toString();
					if (friend.equals(alias1) || friend.equals(alias2)) {
						result = sqlMyServer.SqlQuery("SELECT Geo_X, Geo_Y FROM users WHERE alias = \"" + friend + "\"");
						jsonResult = new JSONObject(result);
						double i_x = jsonResult.getDouble("Geo_X"), i_y = jsonResult.getDouble("Geo_Y");
						double dist = Math.sqrt((i_x - my_x)*(i_x - my_x) + (i_y - my_y)*(i_x - my_y)) + Math.sqrt((i_x - s_x)*(i_x - s_x) + (i_y - s_y)*(i_x - s_y));
						if (min == -1 || min > dist) {
							min = dist;
							intermediary = friend;
						}
					}
				}
				
			}catch(JSONException e){
				Log.e("JSON_EXCEPTION",e.toString());
			}
		}
		return intermediary;
	}
	
	private void onListItemClick(View v, final int pos, long lid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (request_pids.get(pos) == -1) {
		builder.setMessage("Accept " + list.get(pos) + "'s package request?")
	       .setCancelable(true)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	    	   sqlMyServer.SqlQuery("DELETE FROM PREQUESTS WHERE sender = \"" + list.get(pos) + "\" and receiver = \"" + id + "\" and type = 0");
	    	   String intermediary = getIntermediary(list.get(pos));
	    	   if (intermediary.equals("")) {
	    		   System.err.println("Shortest path not found.");
	    		   return;
	    	   }
	    	   Timestamp expiry = new Timestamp(System.currentTimeMillis() + 7 * 86400000);
	    	   sqlMyServer.SqlQuery("INSERT INTO PACKAGES VALUES (\"" + list.get(pos) + "\", \"" + id + "\", \"" + intermediary + "\", \"" + list.get(pos) + "\", \"" + expiry + "\", NULL)");
	    	   sqlMyServer.SqlQuery("INSERT INTO NOTIFICATIONS VALUES (\"" + list.get(pos) + "\",\"" + id + " has accepted your package request.\")");
	           dialog.cancel();
	           packageList = new ArrayList<String>(0);
	           String result = sqlMyServer.JsonSqlQuery(
	   				"SELECT * FROM PACKAGES WHERE " +
	   				"(sender = \"" + id + "\" or receiver = \"" + id + "\" or intermediary = \"" + id + "\")");
		   		try{
		   			JSONArray jsonResultArray = new JSONArray(result);
		   			int len = jsonResultArray.length();
		   			for(int i=0; i<len; i++){
		   				int pid = jsonResultArray.getJSONObject(i).getInt("pid");
		   				String timestamp = jsonResultArray.getJSONObject(i).getString("expiry_time");
		   				packageList.add(pid + ": expires " + timestamp);
		   			}
		   		}catch(JSONException e){
		   			Log.e("JSON_EXCEPTION",e.toString());
		   		}
	           list.remove(pos);
	    	    ((BaseAdapter) adapter).notifyDataSetChanged();
	    	    ((BaseAdapter) packageAdapter).notifyDataSetChanged();
	       }
	       })
	 	   .setNegativeButton("No", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	    	   sqlMyServer.SqlQuery("DELETE FROM PREQUESTS WHERE sender = \"" + list.get(pos) + "\" and receiver = \"" + id + "\"");
	    	   sqlMyServer.SqlQuery("INSERT INTO NOTIFICATIONS VALUES (\"" + list.get(pos) + "\",\"" + id + " has rejected your package request.\")");
	           dialog.cancel();
	           list.remove(pos);
	    	    ((BaseAdapter) adapter).notifyDataSetChanged();
	       }
	       });
		}
		else {
			builder.setMessage("Accept " + list.get(pos) + "'s package transfer request?")
		       .setCancelable(true)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int did) {
		    	   String receiver = "";
		    	   String result = sqlMyServer.SqlQuery("SELECT * FROM PACKAGES WHERE pid = " + request_pids.get(pos));
			   		try{
			   			JSONObject jsonResult = new JSONObject(result);
			   			receiver = jsonResult.getString("receiver");
			   		}catch(JSONException e){
			   			Log.e("JSON_EXCEPTION",e.toString());
			   		}
			   		sqlMyServer.SqlQuery("DELETE FROM PREQUESTS WHERE sender = \"" + list.get(pos) + "\" and receiver = \"" + id + "\" and type = 1 and pid = " + request_pids.get(pos));
		   		   if (receiver.equals(id)) {
		   			   sqlMyServer.SqlQuery("DELETE FROM PACKAGES WHERE pid = " + request_pids.get(pos));
			    	   sqlMyServer.SqlQuery("INSERT INTO NOTIFICATIONS VALUES (\"" + list.get(pos) + "\",\"" + id + " has accepted your transfer confirmation.\nThe transaction is complete.\")");
		   		   }
		   		   else {
			    	   sqlMyServer.SqlQuery("UPDATE PACKAGES SET holder = \"" + id + "\" WHERE pid = " + request_pids.get(pos));
			    	   sqlMyServer.SqlQuery("INSERT INTO NOTIFICATIONS VALUES (\"" + list.get(pos) + "\",\"" + id + " has accepted your transfer confirmation.\")");
			   	   }
		           dialog.cancel();
		           packageList = new ArrayList<String>(0);
		           result = sqlMyServer.JsonSqlQuery(
		   				"SELECT * FROM PACKAGES WHERE " +
		   				"(sender = \"" + id + "\" or receiver = \"" + id + "\" or intermediary = \"" + id + "\")");
			   		try{
			   			JSONArray jsonResultArray = new JSONArray(result);
			   			int len = jsonResultArray.length();
			   			for(int i=0; i<len; i++){
			   				int pid = jsonResultArray.getJSONObject(i).getInt("pid");
			   				String timestamp = jsonResultArray.getJSONObject(i).getString("expiry_time");
			   				packageList.add(pid + ": expires " + timestamp);
			   			}
			   		}catch(JSONException e){
			   			Log.e("JSON_EXCEPTION",e.toString());
			   		}
		           list.remove(pos);
		    	    ((BaseAdapter) adapter).notifyDataSetChanged();
		    	    ((BaseAdapter) packageAdapter).notifyDataSetChanged();
		       }
		       })
		 	   .setNegativeButton("No", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int did) {
		    	   sqlMyServer.SqlQuery("DELETE FROM PREQUESTS WHERE sender = \"" + list.get(pos) + "\" and receiver = \"" + id + "\" and type = 1 and pid = " + request_pids.get(pos));
		    	   sqlMyServer.SqlQuery("INSERT INTO NOTIFICATIONS VALUES (\"" + list.get(pos) + "\",\"" + id + " has rejected your package request.\")");
		           dialog.cancel();
		           list.remove(pos);
		    	    ((BaseAdapter) adapter).notifyDataSetChanged();
		       }
		       });
		}
		AlertDialog alert = builder.create();
	 	alert.show();
	}
	
	private void onPackagesListItemClick(View v, final int pos, long lid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String result = sqlMyServer.SqlQuery("SELECT * FROM PACKAGES WHERE pid = " + pids.get(pos));
		String sender = "", receiver = "", intermediary = "", holder = "", expiry = "";
		try{
			JSONObject jsonResult = new JSONObject(result);
			sender = jsonResult.getString("sender");
			receiver = jsonResult.getString("receiver");
			intermediary = jsonResult.getString("intermediary");
			holder = jsonResult.getString("holder");
			expiry = jsonResult.getString("expiry_time");
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		final String next = (holder.equals(intermediary)) ? receiver : intermediary;
		String message = "sender: " + sender + "\nreceiver: " + receiver + "\nintermediary: " + intermediary + "\nholder: " + holder + "\nexpires: " + expiry;
		if (holder.equals(id)) {
			message = message + "\nRequest transfer confirmation from " + next + "?";
			builder.setMessage(message)
	       .setCancelable(true)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	    	   sqlMyServer.SqlQuery("INSERT into prequests values (\"" + id + "\", \"" + next + "\", 1, " + pids.get(pos) + ")");
	    	   dialog.cancel();
	       }
	       })
	 	   .setNegativeButton("No", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	           dialog.cancel();
	       }
	       });
		}
		else {
			builder.setMessage(message)
		       .setCancelable(true)
		 	   .setNegativeButton("OK", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int did) {
		           dialog.cancel();
		       }
		       });
		}
		AlertDialog alert = builder.create();
	 	alert.show();
	}
	
	private void onSendRequestButtonClicked() {
		String name = recipientName.getText().toString(), alias = "";
		
		String result = sqlMyServer.SqlQuery("SELECT * FROM USERS WHERE alias = \"" + name + "\"");
		System.out.println("result = " + result);
		try{
			JSONObject jsonResult = new JSONObject(result);
			System.out.println("alias = " + jsonResult.get("alias"));
			alias = jsonResult.get("alias").toString();
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (name.length() > 0 && name.equals(alias) && !name.equals(id)) {
			sqlMyServer.SqlQuery("INSERT into prequests(sender, receiver, type) values (\"" + id + "\", \"" + name + "\", 0)");
			builder.setMessage("Request Sent")
	 	       .setCancelable(true)
		 	   .setNegativeButton("OK", new DialogInterface.OnClickListener() {
				       public void onClick(DialogInterface dialog, int id) {
				              dialog.cancel();
				       }
			    });
			
			 	AlertDialog alert = builder.create();
			 	alert.show();
		}
		else if (name.length() == 0 || !name.equals(alias)) {
			builder.setMessage("Error: requested user not found")
	 	       .setCancelable(true)
		 	   .setNegativeButton("OK", new DialogInterface.OnClickListener() {
				       public void onClick(DialogInterface dialog, int id) {
				              dialog.cancel();
				       }
			    });
			
			 	AlertDialog alert = builder.create();
			 	alert.show();
		}
		else {
			builder.setMessage("Error: you cannot request a transaction with yourself")
	 	       .setCancelable(true)
		 	   .setNegativeButton("OK", new DialogInterface.OnClickListener() {
				       public void onClick(DialogInterface dialog, int id) {
				              dialog.cancel();
				       }
			    });
			
			 	AlertDialog alert = builder.create();
			 	alert.show();
		}
	}
}

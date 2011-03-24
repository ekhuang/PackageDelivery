package edu.ucdavis;

import java.util.ArrayList;

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
import android.widget.ListAdapter;
import android.widget.ListView;



public class FriendRequest extends Activity {
	
	private Button sendRequest;
	private EditText nameField;
	private ListView requests;
	private ListAdapter adapter;
	private ListView friends;
	private ListAdapter friendadapter;
	
	private String id;
	
	private ArrayList<String> list;
	private ArrayList<String> friendlist;
	
	SqlMyServer sqlMyServer;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.friends);
		
		requests = (ListView)findViewById(R.id.incomingRequests);
		friends = (ListView)findViewById(R.id.friendsList);
		
		sqlMyServer = new SqlMyServer();
		
		id = getIntent().getStringExtra("LOGIN_ID");
		
		sendRequest = (Button)findViewById(R.id.sendRequest);
		sendRequest.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onFriendRequestButtonClicked();
        	}
        });
		
		list = new ArrayList<String>(0);
		friendlist = new ArrayList<String>(0);
		
		String result = sqlMyServer.JsonSqlQuery("SELECT * FROM REQUESTS WHERE receiver = \"" + id + "\"");
		try{
			JSONArray jsonResultArray = new JSONArray(result);
			int len = jsonResultArray.length();
			for(int i=0; i<len; i++){
				String sender = jsonResultArray.getJSONObject(i).get("sender").toString();
				System.out.println(sender);
				list.add(sender);
			}
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		result = sqlMyServer.JsonSqlQuery(
				"SELECT * FROM FRIENDS WHERE " +
				"(alias1 = \"" + id + "\" or alias2 = \"" + id + "\")");
		try{
			JSONArray jsonResultArray = new JSONArray(result);
			int len = jsonResultArray.length();
			for(int i=0; i<len; i++){
				String alias1 = jsonResultArray.getJSONObject(i).get("alias1").toString();
				String alias2 = jsonResultArray.getJSONObject(i).get("alias2").toString();
				if (id.equals(alias2))
					friendlist.add(alias1);
				else
					friendlist.add(alias2);
			}
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , list);
		friendadapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , friendlist);
		requests.setAdapter(adapter);
		friends.setAdapter(friendadapter);
		
		requests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		        onListItemClick(v,pos,id);
		    }
		});
		
		friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		        onFriendsListItemClick(v,pos,id);
		    }
		});
		
		System.out.println("incoming requests = " + result);
	}
	
	private void onListItemClick(View v, final int pos, long lid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Add " + list.get(pos) + " to friends?")
	       .setCancelable(true)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	    	   sqlMyServer.SqlQuery("DELETE FROM REQUESTS WHERE sender = \"" + list.get(pos) + "\" and receiver = \"" + id + "\"");
	    	   sqlMyServer.SqlQuery("INSERT INTO FRIENDS VALUES (\"" + id + "\",\"" + list.get(pos) + "\")");
	    	   sqlMyServer.SqlQuery("INSERT INTO NOTIFICATIONS VALUES (\"" + list.get(pos) + "\",\"" + id + " has accepted your friend request.\")");
	           dialog.cancel();
	           friendlist.add(list.get(pos));
	           list.remove(pos);
	    	    ((BaseAdapter) adapter).notifyDataSetChanged();
	    	    ((BaseAdapter) friendadapter).notifyDataSetChanged();
	       }
	       })
	 	   .setNegativeButton("No", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	    	   sqlMyServer.SqlQuery("DELETE FROM REQUESTS WHERE sender = \"" + list.get(pos) + "\" and receiver = \"" + id + "\"");
	    	   sqlMyServer.SqlQuery("INSERT INTO NOTIFICATIONS VALUES (\"" + list.get(pos) + "\",\"" + id + " has rejected your friend request.\")");
	           dialog.cancel();
	           list.remove(pos);
	    	    ((BaseAdapter) adapter).notifyDataSetChanged();
	       }
	       });
		AlertDialog alert = builder.create();
	 	alert.show();
	}
	
	private void onFriendsListItemClick(View v, final int pos, long lid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String result = sqlMyServer.SqlQuery("SELECT * FROM USERS WHERE alias = \"" + friendlist.get(pos) + "\"");
		String phone = "";
		try{
			JSONObject jsonResult = new JSONObject(result);
			phone = jsonResult.getString("phone");
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		builder.setMessage(friendlist.get(pos) + "\n" + phone + "\nDelete this friend?")
	       .setCancelable(true)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	    	   sqlMyServer.SqlQuery(
	   				"DELETE FROM FRIENDS WHERE " +
	   				"(alias1 = \"" + friendlist.get(pos) + "\" and alias2 = \"" + id + "\") or " +
	   				"(alias1 = \"" + id + "\" and alias2 = \"" + friendlist.get(pos) + "\")");
	           dialog.cancel();
	           friendlist.remove(pos);
	    	   ((BaseAdapter) friendadapter).notifyDataSetChanged();
	       }
	       })
	 	   .setNegativeButton("No", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int did) {
	           dialog.cancel();
	       }
	       });
		AlertDialog alert = builder.create();
	 	alert.show();
	}
	
	private void onFriendRequestButtonClicked() {
		nameField = (EditText)findViewById(R.id.nameField);
		String name = nameField.getText().toString();
		String alias = "", alias1 = "", alias2 = "", sender = "", receiver = "";
		System.out.println("name = " + name);
		
		String result = sqlMyServer.SqlQuery("SELECT * FROM USERS WHERE alias = \"" + name + "\"");
		System.out.println("result = " + result);
		try{
			JSONObject jsonResult = new JSONObject(result);
			System.out.println("alias = " + jsonResult.get("alias"));
			alias = jsonResult.get("alias").toString();
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		String result1 = sqlMyServer.SqlQuery(
				"SELECT * FROM FRIENDS WHERE " +
				"(alias1 = \"" + name + "\" and alias2 = \"" + id + "\") or " +
				"(alias1 = \"" + id + "\" and alias2 = \"" + name + "\")");
		System.out.println("result1 = " + result1);
		try{
			JSONObject jsonResult = new JSONObject(result1);
			alias1 = jsonResult.get("alias1").toString();
			alias2 = jsonResult.get("alias2").toString();
			System.out.println("alias1 = " + alias1);
			System.out.println("alias2 = " + alias2);
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		String result2 = sqlMyServer.SqlQuery(
				"SELECT * FROM REQUESTS WHERE " +
				"(sender = \"" + name + "\" and receiver = \"" + id + "\") or " +
				"(sender = \"" + id + "\" and receiver = \"" + name + "\")");
		System.out.println("result1 = " + result1);
		try{
			JSONObject jsonResult = new JSONObject(result2);
			sender = jsonResult.get("sender").toString();
			receiver = jsonResult.get("receiver").toString();
			System.out.println("sender = " + sender);
			System.out.println("receiver = " + receiver);
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (name.length() > 0 && name.equals(alias) && !name.equals(alias1) && !name.equals(alias2) && !name.equals(sender) && !name.equals(receiver) && !name.equals(id)) {
			sqlMyServer.SqlQuery("INSERT INTO REQUESTS VALUES (\"" + id + "\",\"" + name + "\")");
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
		else {
			  String message = "Error: requested user not found";
			  if (name.equals(id))
				  message = "Error: you cannot friend yourself";
			  else if (name.length() == 0)
				  message = "Error: nothing in field";
			  else if (name.equals(alias1) || name.equals(alias2))
				  message = "Error: user already in your friends list";
			  else if (name.equals(sender))
				  message = "Error: you already have a request from this user";
			  else if (name.equals(receiver))
				  message = "Error: pending request for this user already exists";
			  builder.setMessage(message)
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

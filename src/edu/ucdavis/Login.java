package edu.ucdavis;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity {
	
	private EditText username;
	private EditText password;
	
	private Button login;
	private Button register;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		username = (EditText)findViewById(R.id.usernameField);
		password = (EditText)findViewById(R.id.passwordField);
		login = (Button)findViewById(R.id.login);
		login.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onLoginButtonClicked();
        	}
        });
		register = (Button)findViewById(R.id.register);
		register.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onRegisterButtonClicked();
        	}
        });
	}
	
	private void onLoginButtonClicked() {
		SqlMyServer sqlMyServer = new SqlMyServer();
	    String name = username.getText().toString();
	    String pass = password.getText().toString();
	    String result = sqlMyServer.SqlQuery("SELECT * FROM USERS WHERE alias = \"" + name + "\"");
	    String passw = "";
	    try{
			JSONObject jsonResult = new JSONObject(result);
			passw = jsonResult.get("password").toString();
			System.out.println("pasword = " + passw);
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
	    System.out.println("result = " + result + "\n" + "{\"alias\":\"" + name + "\",\"Password\":\"" + pass + "\"}");
	    if (pass.equals(passw))
	    	goToMainMenu(name);
	    else {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("error: invalid username and/or password")
	    	       .setCancelable(true)
	    		.setNegativeButton("ok", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                 dialog.cancel();
	            }
	        });

	    	AlertDialog alert = builder.create();
	    	alert.show();
	    }
	}
	
	private void onRegisterButtonClicked() {
		SqlMyServer sqlMyServer = new SqlMyServer();
	    String name = username.getText().toString();
	    String pass = password.getText().toString();
	    String result = sqlMyServer.SqlQuery("SELECT * FROM users u, banlist b WHERE u.alias = \"" + name + "\" or b.alias = \"" + name + "\"");
	    String alias = "";
	    try{
			JSONObject jsonResult = new JSONObject(result);
			System.out.println("alias = " + jsonResult.get("alias"));
			alias = jsonResult.get("alias").toString();
		}catch(JSONException e){
			Log.e("JSON_EXCEPTION",e.toString());
		}
	    System.out.println("result = " + result);
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    if (!name.equals(alias) && name.length() > 3 && pass.length() > 5) {
	    	sqlMyServer.SqlQuery("INSERT INTO USERS(alias, password) VALUES (\"" + name + "\",\"" + pass + "\")");
	    	builder.setMessage("Registration Successful")
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
	    	String message = "";
	    	if (name.length() < 4)
	    		message = "Error: username too short";
	    	else if (pass.length() < 6)
	    		message = "Error: password too short";
	    	else
	    		message = "Error: this username is taken";
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
	
	private void goToMainMenu(String name) {
		Intent i = new Intent(this, Update.class);
		i.putExtra("LOGIN_ID", name);
    	startActivity(i);
	}
}

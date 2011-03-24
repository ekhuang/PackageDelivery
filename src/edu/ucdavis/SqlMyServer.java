package edu.ucdavis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;

public class SqlMyServer {
	private String _host;  //Host Address where sql server is located
	
	
	
	// Initializes _host
	public SqlMyServer(){
		_host = "http://192.168.200.38/ECS160.php";
	}
	
	
	
	// Tests connection to host.  Returns 1 if failed to connect, else 0 for successful connection
	public int TestConnection(){
		try{
			HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(_host);
            httpclient.execute(httppost);
            
		}catch(Exception e){  //Connection failed, return 1
			return 1;
		}
		
		return 0;    //Connection Success, return 0
	}
	
	
	
	// Performs a Selection Query.  logFields is a arraylist<string> of names of fields such as username, password, phone#, etc, it is
	// just used for log data and can be removed later
	
	public String SqlQuery(String sqlQuery){
		InputStream is = null;
		String result = "";     	//result of server response in string format
		String returnString = "";  //converted json result
		
		ArrayList<NameValuePair> pairedSqlQuery = new ArrayList<NameValuePair>();   // Pairs sqlQuery with identifier query ...will be used
		   																			// during php POST
		pairedSqlQuery.add(new BasicNameValuePair("Query",sqlQuery));
		
		//http post:  Basically simulates a php POST method and sends to host for a response
		try{
			HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(_host);
            httppost.setEntity(new UrlEncodedFormEntity(pairedSqlQuery));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}
		
		
		//convert response to string from json format
		try{
			Log.e("OurTest",is.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
            }
            is.close();
            result=sb.toString();
		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}
		
		
		//parse json data
		try{
			if(!result.equals("")){
				JSONArray jArray = new JSONArray(result);
	            for(int i=0;i<jArray.length();i++){
	                    //Get an output to the screen
	                    returnString += "\n" + jArray.getJSONObject(i); 
	                    }
            }
	    }catch(JSONException e){
	            Log.e("log_tag", "Error parsing data "+e.toString());
	    }
	    
		return returnString;
	}
	
	//Returns server response that needs to be converted to json array
	public String JsonSqlQuery(String sqlQuery){
		InputStream is = null;
		String result = "";     	//result of server response in string format
		ArrayList<NameValuePair> pairedSqlQuery = new ArrayList<NameValuePair>();   // Pairs sqlQuery with identifier query ...will be used
		   																			// during php POST
		pairedSqlQuery.add(new BasicNameValuePair("Query",sqlQuery));
		
		//http post:  Basically simulates a php POST method and sends to host for a response
		try{
			HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(_host);
            httppost.setEntity(new UrlEncodedFormEntity(pairedSqlQuery));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}
		
		
		//convert response to string from json format
		try{
			Log.e("OurTest",is.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
            }
            is.close();
            result=sb.toString();
		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}
		
		return result;
	}
}

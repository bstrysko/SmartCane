package com.nodeshareclient;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.*;

import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

public class NodeShareConnection
{
	private final static String TAG = "NodeShareClient";
	
	private WebSocketClient connection;
	private static List<BasicNameValuePair> extra_headers = Arrays.asList(
			new BasicNameValuePair("Cookie", "publisher=true")
		);
	private static String private_key = "";
	
	public NodeShareConnection(String url)
	{
		/*
		 *  Upon connection NodeShare will check for cookies.
		 *  If publisher is set to true, NodeShare will send a
		 *  random segment of data that must be encrypted with the
		 *  predetermined private key.
		 */		
		connection = new WebSocketClient(URI.create(url),new WebSocketClient.Listener()
				{	
					@Override
					public void onMessage(byte[] data)
					{
						Log.d(TAG, "Received binary data: " + data.toString());
					}
			
					@Override
					public void onMessage(String message) 
					{
						Log.d(TAG, "Received string message: " + message.toString());
						
						JSONObject object;
						
						try 
						{
							object = new JSONObject(message);
						} 
						catch (JSONException e) 
						{
							e.printStackTrace();
							return;
						}
						
						String publisher_handshake = null;
		
						/*
						 * If an exception is thrown the message should
						 * be passed to the main application otherwise
						 * it is a publisher_key message
						 */
						try
						{
							publisher_handshake = object.getString("publisher_key");
							
							/*
							 * No exception at this point
							 */
							
							JSONObject response = new JSONObject();
							response.put("publisher_handshake", create_handshake(publisher_handshake));
							connection.send("");
						}
						catch(Exception e)
						{
							
						}
						
					}
					
					@Override
					public void onError(Exception error) 
					{
						// TODO Auto-generated method stub	
					}
					
					@Override
					public void onDisconnect(int code, String reason) 
					{
						// TODO Auto-generated method stub	
					}
					
					@Override
					public void onConnect() 
					{
						// TODO Auto-generated method stub
					}
				}, extra_headers);
		
		connection.connect();
	}
	
	private String create_handshake(String public_key)
	{
		//TODO: implement
		return public_key;
	}
}

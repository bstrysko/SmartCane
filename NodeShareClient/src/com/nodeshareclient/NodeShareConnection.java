/*
 * NodeShareConnection.java
 * NodeShareClient
 * 
 * Written by Brent Strysko
 * 
 * Provides client and publisher functionality for a NodeShare node.
 * Data can only be publisher through the NodeShare server if the node
 * authenticates as a publisher upon connection.  This is accomplished by
 * placing a cookie in the Websocket handshake that provides a field called
 * 'publisher' which should contain the name of the publisher(pending a successful
 * authentication handshake).  The server then once having established a proper
 * Websocket connection with the node, will send a 'publisher_key' method that
 * the node should properly encode using the private key and send back the encoded
 * key to the server with the field 'publisher_handshake'.  If the node successfully 
 * encoded the message the server will respond with a boolean 'publishing' field
 * corresponding to whether the node now has publisher rights.  There is currently 
 * no support to re-attempt or disable publisher privileges after it is granted.
 * Only a successfully authenticated publisher can send messages.
 * 
 *  API
 *  
 *  public boolean send(JSONObject message)
 *  
 */

package com.nodeshareclient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.*;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

public class NodeShareConnection
{
	private final static String TAG = "NodeShareClient";
	
	private WebSocketClient connection;
	private List<BasicNameValuePair> extra_headers;
	private static String private_key = "n0desHAR3C0NNET1on";
	private boolean publishing = false;
	
	private INodeShareListener listener;
	
	/*
	 * Creates a client NodeShare connection.
	 */
	public NodeShareConnection(String url, final INodeShareListener listener)
	{
		this(url,listener,null);
	}
	
	/*
	 * Attempts to create a publisher NodeShare connection
	 * if publisher is not null or the empty string.
	 */
	public NodeShareConnection(String url, final INodeShareListener listener, String publisher_name)
	{
		this.listener = listener;
		this.extra_headers = Arrays.asList();
		
		if(publisher_name != null && publisher_name != "")
		{
			extra_headers.add(new BasicNameValuePair("Cookie","publisher=" + publisher_name));
		}
				
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
				Log.i(TAG, "Received binary data: " + data.toString());
			}
			
			@Override
			public void onMessage(String message) 
			{
				Log.i(TAG, "Received string message: " + message.toString());
						
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
						
				if(object.optString("publisher_key") != "")
				{
					JSONObject response = new JSONObject();
					
					try 
					{
						response.put("publisher_handshake", create_handshake(object.optString("publisher_key")));
					} 
					catch (JSONException e) 
					{
						e.printStackTrace();
						Log.d(TAG, "Could not create publisher handshake message: " + e.toString());
						return;
					}
					
					connection.send(response.toString());
				}
				else if(object.optBoolean("publishing") == true)
				{
					publishing = true;
				}
				else
				{
					listener.onMessage(object);
				}
			}
					
			@Override
			public void onError(Exception error) 
			{
				error.printStackTrace();
			}
					
			@Override
			public void onDisconnect(int code, String reason) 
			{
				Log.i(TAG,"onDisconnect code:" + code + " reason:" + reason);
				listener.onDisconnect();
			}
					
			@Override
			public void onConnect() 
			{
				Log.i(TAG, "onConnect");
				listener.onConnect();
			}			
		}, extra_headers);
		
		connection.connect();
	}
	
	/*
	 * Returns whether or not the connection has
	 * publisher privileges.
	 */
	public boolean is_publisher()
	{
		return publishing;
	}
	
	/*
	 * Sends a message to the NodeShare server.
	 * Can only be sent by a valid publisher.
	 */
	public boolean send(JSONObject message)
	{
		if(publishing)
		{
			connection.send(message.toString());
			return true;
		}
		else
		{
			Log.i(TAG, "Cannot send message because not an authenticated publisher: " + message.toString());
			return false;
		}
	}
	
	/*
	 * Encodes the public key via the private
	 * key and SHA1.
	 */
	private String create_handshake(String public_key)
	{
		MessageDigest md_sha1 = null;
		
		try
		{
			md_sha1 = MessageDigest.getInstance("SHA-1");
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			Log.i(TAG, "SHA-1 algorithm not found.  Could not create handshake for public_key: " + public_key);
			return null;
		}
		
		String message = public_key + private_key;
		
		byte[] message_bytes = null;
		
		try
		{
			message_bytes = message.getBytes("ASCII");
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
			Log.i("TAG", "Could not get message bytes");
			return null;
		}
		
		md_sha1.update(message_bytes);		
		byte[] handshake_bytes = md_sha1.digest();
		
		String handshake = Base64.encodeToString(handshake_bytes, 0);
		
		return handshake;
	}
	
	/*
	 * Sets the private key used by all instances of
	 * the NodeShareConnection class.
	 */
	public static void set_private_key(String private_key)
	{
		NodeShareConnection.private_key = private_key;
	}
}
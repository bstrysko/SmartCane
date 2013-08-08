package com.smartcane;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.canelib.*;
import com.nodeshareclient.*;
import com.smartcane.R;

public class MainActivity extends Activity implements ICaneListener, INodeShareListener
{
	private static final String TAG = "MainActivity";
	
	private NodeShareConnection connection;
	private TextView ip_address_input;
	private String ip = "192.168.0.5";
	
	private Cane cane;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ip_address_input = (TextView)findViewById(R.id.ip_address);
		connection = null;
		cane = new Cane(this);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		cane.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		cane.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void connect_to_server(View view)
	{
		ip = ip_address_input.getText().toString();
		Log.i(TAG, "IP Address " + ip + " submitted");
		connection = new NodeShareConnection("ws://" + ip + ":8080", this, "phone1");
	}
	
	@Override
	public void onData() 
	{
		Log.i(TAG, "onData()");
		
		JSONObject message = new JSONObject();
		
		try
		{
			message.put("force", cane.get_force_value());
			message.put("sonar", cane.get_sonar_value());
			message.put("fsr", cane.get_fsr_value());
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		
		if(connection != null)
		{
			connection.send(message);
		}
	}

	@Override
	public void onConnect() 
	{
		Log.i(TAG, "onConnect");
	}
	
	@Override
	public void onPublishing(boolean publishing)
	{
		Log.i(TAG, "onPublishing(" + publishing + ")");
	}

	@Override
	public void onMessage(JSONObject message) 
	{
		Log.i(TAG, "onMessage(" + message + ")");
	}

	@Override
	public void onDisconnect() 
	{
		Log.i(TAG, "onDisconnect");
	}
}

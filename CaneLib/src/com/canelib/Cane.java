package com.canelib;

import android.util.Log;
import ioio.lib.api.*;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOConnectionRegistry;

public class Cane
{
	private final static String TAG = "Cane";
	
	private ICaneListener listener;
	private CaneThread thread;
	private byte[][] leds = new byte[21][3];
	private float force_value;
	private float sonar_value;
	private float fsr_value;
		
	public Cane(ICaneListener listener)
	{
		this.listener = listener;
	}
	
	public void onResume()
	{
		thread = new CaneThread();
		thread.start();
	}
	
	public void onPause() 
	{
		thread.abort();
		
		try 
		{
			thread.join();
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public boolean set_led()
	{
		return false;
	}
	
	public float get_sonar_value()
	{
		return sonar_value;
	}
	
	public float get_force_value()
	{
		return force_value;
	}
	
	public float get_fsr_value()
	{
		return fsr_value;
	}
	
	public class CaneThread extends Thread
	{
		private IOIO ioio;
		private boolean abort = false;
		
		private AnalogInput force_sensor;
		private AnalogInput sonar_sensor;
		private AnalogInput fsr_sensor; 
		
		@Override
		public void run() 
		{
			super.run();
			
			while (true) 
			{
				synchronized(this) 
				{
					if (abort) 
					{
						break;
					}
					
					ioio = IOIOFactory.create();
				}
				
				try 
				{
					ioio.waitForConnect();
					force_sensor = ioio.openAnalogInput(31);
					sonar_sensor = ioio.openAnalogInput(32);
					fsr_sensor = ioio.openAnalogInput(33);

					while(true)
					{
						force_value = force_sensor.read();
						sonar_value = sonar_sensor.read();
						
						fsr_value = fsr_sensor.read();
						
						listener.onData();
						
						sleep(10);
					}
					
				} 
				catch(ConnectionLostException e)
				{
					e.printStackTrace();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					Log.e(TAG, "Unexpected exception caught", e);
					ioio.disconnect();
					break;
				} 
				finally 
				{
					if (ioio != null) 
					{
						try 
						{
							ioio.waitForDisconnect();
						} 
						catch(InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					synchronized (this) 
					{
						ioio = null;
					}
				}
			}
		}
		
		synchronized public void abort() 
		{
			abort = true;
			
			if (ioio != null) 
			{
				ioio.disconnect();
			}
		}
	}
	static {
		IOIOConnectionRegistry.addBootstraps(new String[] 
		{
			"ioio.lib.impl.SocketIOIOConnectionBootstrap",
			"ioio.lib.android.accessory.AccessoryConnectionBootstrap",
			"ioio.lib.android.bluetooth.BluetoothIOIOConnectionBootstrap" 
		});
	}
}

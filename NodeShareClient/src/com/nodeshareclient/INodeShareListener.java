package com.nodeshareclient;

import org.json.JSONObject;

public interface INodeShareListener
{
	public void onConnect();
	public void onPublishing(boolean publishing);
	public void onMessage(JSONObject message);
	public void onDisconnect();
}
/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class of JSON object
 * @version 0.8
 * @see org.json.JSONArray
 * @see org.json.JSONObject
 * @see java.util.Map
 *
 */
public class JsonUtil
{
	/**
	 * Function to convert hash map to JSON object
	 * @param map hash map
	 * @return converted JSON object
	 */
	public static JSONObject toJsonString(Map<String, Object> map)
    {
		JSONObject jsonObject = new JSONObject();
		try
		{
			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				String key = entry.getKey();
				Object value = entry.getValue();

				if (value instanceof String)
				{
					jsonObject.put(key, (String)value);
				}
				else if (value instanceof Number)
				{
					jsonObject.put(key, (Number)value);
				}
				else if (value instanceof Collection)
				{
					jsonObject.put(key, (Collection<?>)value);
				}
				else
				{
					jsonObject.put(key, value);
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
        return jsonObject;
    }

	/**
	 * Function to convert list map to JSON array 
	 * @param maps list map
	 * @return converted JSON array
	 */
	public static JSONArray toJsonArray(List<Map<String, Object>> maps)
	{
	   JSONArray jsonObjectArray = new JSONArray();
	   for (Map<String, Object> map : maps)
	   {
	      jsonObjectArray.put(toJsonString(map));
	   }
	   return jsonObjectArray;
	}
	
	/**
	 * Function to convert JSON object to map
	 * @param map target map
	 * @param jsonObject JSON object to convert
	 * @throws JSONException 
	 */
	public static void fromJsonObject(Map<String, Object> map, JSONObject jsonObject) throws JSONException
	{
		if ((map == null) || (jsonObject == null))
			return;

		map.clear();

		Iterator<String> i = jsonObject.keys();
		while (i.hasNext())
		{
			String key = i.next();
			Object value = jsonObject.get(key);

			map.put(key, value);
		}
	}

	/**
	 * Function to convert JSON object to map
	 * @param jsonObject JSON object to convert
	 * @return converted map
	 * @throws JSONException
	 */
	public static Map<String, Object> fromJsonObject(JSONObject jsonObject) throws JSONException
	{
		Map<String, Object> map = new HashMap<String, Object>();               
		if (jsonObject != null)
		{
			Iterator<String> i = jsonObject.keys();
			while (i.hasNext())
			{
				String key = i.next();
				Object value = jsonObject.get(key);

				map.put(key, value);
			}
		}
		return map;
	}
}
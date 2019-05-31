/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.anchor;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hdac.db.MariaDao;
import com.hdac.proterty.ServerConfig;
import com.hdac.util.HdacUtil;
import com.hdac.util.StringUtil;

/**
 * HandleAsset class
 * Make data of asset
 * @version 0.8
 * @see java.util.HashMap
 * @see org.json.JSONArray
 * @see org.json.JSONObject
 */
public class HandleAsset {
	
	private Map<String, Object> serverConfig_ = null;
	private MariaDao mDao;
		
	/**
	 * Initialize variable and make asset info data
	 * @param config (ServerConfig) side chain server config
	 * @param dao (MariaDao) 
	 * @return (String) asset info 
	 */
	public String getAssetInfo(Map<String, Object> config, MariaDao dao)
	{
		serverConfig_ = config;
		mDao = dao;
				
		String assetInfo = getListAsset();
		
		return assetInfo;
	}
	
	/**
	 * make asset info data from RPC (Total token qty, Remain qty of side contract address, balance of main contract address)
	 * @return (String) asset info 
	 */
	private String getListAsset() 
	{
		JSONObject returnObj = new JSONObject();
				
		try 
		{
			List<Map<String, Object>> tokenDB = mDao.getTokenName();
						
			String assetResult = HdacUtil.getDataFromRPC("listassets", new String[0], serverConfig_);
					
			int idx = assetResult.indexOf(":");
			if(assetResult.charAt(idx+1) == '[') 
			{
				JSONArray assetList = new JSONObject(assetResult).getJSONArray("result");
				for(int j = 0; j < tokenDB.size(); j++) 
				{
					for(int i = 0; i < assetList.length(); i++)
					{
						String assetName = assetList.getJSONObject(i).get("name").toString();
						
						if(tokenDB.get(j).get("tokenName").equals(assetName)) 
						{
							//--> Total issued token Qty
							String assetQty = assetList.getJSONObject(i).get("issueqty").toString();
																					
							//--> Remain token on contract address in side chain
							Map<String, Object> param = new HashMap<String, Object>();
							param.put("address", tokenDB.get(j).get("contractAddress"));
							param.put("asset", tokenDB.get(j).get("tokenName"));
							//String assetRemain = new HandleTransaction().getHttpData("http://" + webIP + ":" + webPort + "/asset/addrs/"+ tokenDB.get(j).get("contractAddress") +"/balance?name="+tokenDB.get(j).get("tokenName"), "GET");
							String assetRemain = getAddressAsset(param, serverConfig_);
							
							//--> balance on contract address in main chain (used token)
							Object[] params = new Object[1];
							params[0] = tokenDB.get(j).get("contractAddress");
							String balanceResult = HdacUtil.getDataFromRPC("getaddressbalance", params, ServerConfig.getInstance().getMainChainInfo());
							JSONObject balanceObj = new JSONObject(balanceResult).getJSONObject("result");
							int swapRatio = Integer.parseInt(tokenDB.get(j).get("tokenSwapRatio").toString());
							String mainBalance = String.valueOf(balanceObj.getDouble("balance") * Math.pow(10, -8) * swapRatio);
							
							returnObj.put(assetName, assetQty + "/" + assetRemain + "/" + mainBalance);
						}
					}
				}
			}
			/*else 
			{
				JSONObject assetOne = new JSONObject(assetResult).getJSONObject("result");
				String assetName = assetOne.get("name").toString();
				String assetQty = assetOne.get("issueqty").toString();
				
				//--> Remain token on contract address in side chain
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("address", tokenDB.get(0).get("contractAddress"));
				param.put("asset", tokenDB.get(0).get("tokenName"));
				String assetRemain = api.getAddressAsset(param, serverConfig_);
				
				//--> balance on contract address in main chain (used token)
				Object[] params = new Object[1];
				params[0] = tokenDB.get(0).get("contractAddress");
				String balanceResult = HdacUtil.getDataFromRPC("getaddressbalance", params, HdacUtil.getServerType("public"));
				JSONObject balanceObj = new JSONObject(balanceResult).getJSONObject("result");
				int swapRatio = Integer.parseInt(tokenDB.get(0).get("tokenSwapRatio").toString());
				String mainBalance = String.valueOf(balanceObj.getDouble("balance") * Math.pow(10, -8) * swapRatio);
				
				returnObj.put(assetName, assetQty + "/" + assetRemain + "/" + mainBalance);
			}*/
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return returnObj.toString();
	}
	
	private String getAddressAsset(Map<String, Object> paramMap, Map<String, Object> config) throws JSONException
	{
		double balance = 0;
		
		try 
		{
			String address = StringUtil.nvl(paramMap.get("address"));
			
			Object[] params = new Object[3];
			params[0] = address;
			params[1] = StringUtil.nvl(paramMap.get("asset"));
			params[2] = 0;
			
			String balanceResult = HdacUtil.getDataFromRPC("getmultibalances", params, config);
			
			JSONObject objBalance = new JSONObject(balanceResult).getJSONObject("result");
			JSONArray arrayBalance = new JSONArray();
			
			if (objBalance.has(address))
				arrayBalance = objBalance.getJSONArray(address);
			
			int length = arrayBalance.length();
			for (int i = 0; i < length; i++)
			{
				balance += arrayBalance.getJSONObject(i).getDouble("qty");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		DecimalFormat format = new DecimalFormat(".########");
		return format.format(Math.round(balance * Math.pow(10, 8)) / Math.pow(10, 8));
		
		//return balance * Math.pow(10, 8);
	}
}

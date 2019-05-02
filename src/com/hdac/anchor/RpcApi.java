/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.anchor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hdac.util.HdacUtil;
import com.hdac.util.ServerConfig;
import com.hdac.util.StringUtil;

/**
 * Rpc mehod class
 * @version 0.8
 * @see org.json.JSONArray
 * @see org.json.JSONObject 
 *
 */
public class RpcApi{
	
	/**
	 * Generator of RpcApi class
	 */
	public RpcApi() 
	{
		
	}
	
	/**
	 * Get UTXO from RPC command
	 * @param paramMap map of addresses
	 * @param config configuration of server
	 * @return utxo map
	 */
	public List<Map<String, Object>> getUtxos(Map<String, Object> paramMap, ServerConfig config)
	{
		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();

		try
		{
			//make RPC param
			JSONObject param_ = new JSONObject();
			param_.put("addresses", StringUtil.nvl(paramMap.get("addresses")).split(","));

			Object[] params = new Object[1];
			params[0] = param_;

			String strUtxoResult = HdacUtil.getDataFromRPC("getaddressutxos", params, config);
			String strMempoolResult = HdacUtil.getDataFromRPC("getaddressmempool", params, config);
			System.out.println(strUtxoResult);
			JSONObject objUtxoResult = new JSONObject(strUtxoResult);
			JSONObject objMempoolResult = new JSONObject(strMempoolResult);

			JSONArray objUtxoBlockArray = objUtxoResult.getJSONArray("result");
			JSONArray obMempoolArray = objMempoolResult.getJSONArray("result");

			//add mempool result to utxo list
			for(int i = 0; i < obMempoolArray.length(); i++) {
				objUtxoBlockArray.put(obMempoolArray.getJSONObject(i));
			}

			//except some duplicate mempool object when param is multi address
			JSONArray resultArray = new JSONArray();
			boolean excp = false;
			for(int i = 0; i < objUtxoBlockArray.length(); i++) {
				String checkTx = objUtxoBlockArray.getJSONObject(i).getString("txid");
				excp = false;
				for(int j = i; j < objUtxoBlockArray.length(); j++) {
					if(objUtxoBlockArray.getJSONObject(j).has("prevtxid")) {
						if(checkTx.equals(objUtxoBlockArray.getJSONObject(j).getString("prevtxid"))) {
							excp = true;
						}
					}
				}
				if(!excp && !objUtxoBlockArray.getJSONObject(i).has("prevtxid")) {
					resultArray.put(objUtxoBlockArray.getJSONObject(i));
				}
			}

			int cnt = resultArray.length();
			for(int i = cnt - 1; i >= 0; i--) {
				System.out.println("resultArray = " + resultArray.getJSONObject(i).toString());

				Object[] params_ = new Object[2];
				params_[0] = resultArray.getJSONObject(i).get("txid"); 
				params_[1] = 1;

				strUtxoResult = HdacUtil.getDataFromRPC("getrawtransaction", params_, config);
				objUtxoResult = new JSONObject(strUtxoResult);

				//make confirmations field to utxo object, mempool object set to 0
				long confirm = 0;
				if(objUtxoResult.getJSONObject("result").has("confirmations"))
					 confirm = objUtxoResult.getJSONObject("result").getLong("confirmations");

				//make script, outputindex field to mempool object
				if(!resultArray.getJSONObject(i).has("script")) {
					JSONArray voutArray = objUtxoResult.getJSONObject("result").getJSONArray("vout");
					resultArray.getJSONObject(i).put("outputIndex", resultArray.getJSONObject(i).get("index"));
					for(int j = 0; j < voutArray.length(); j++) {
						if (voutArray.getJSONObject(j).has("scriptPubKey") && voutArray.getJSONObject(j).getJSONObject("scriptPubKey").has("addresses"))
						{
							JSONArray adrArray = voutArray.getJSONObject(j).getJSONObject("scriptPubKey").getJSONArray("addresses");
							for(int k = 0; k < adrArray.length(); k++) {
								if(adrArray.getString(k).equals(resultArray.getJSONObject(i).getString("address"))) {
									resultArray.getJSONObject(i).put("script", voutArray.getJSONObject(j).getJSONObject("scriptPubKey").get("hex"));
									break;
								}
							}
						}
					}
				}

				//make new utxo object list
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("unspent_hash", resultArray.getJSONObject(i).get("txid"));
				map.put("address", resultArray.getJSONObject(i).get("address"));
				map.put("scriptPubKey", resultArray.getJSONObject(i).get("script"));
				map.put("amount", (double)(resultArray.getJSONObject(i).getLong("satoshis")/Math.pow(10, 8)));
				map.put("vout", resultArray.getJSONObject(i).get("outputIndex"));
				map.put("confirmations", confirm);
				map.put("satoshis", resultArray.getJSONObject(i).getLong("satoshis"));
				map.put("txid", resultArray.getJSONObject(i).get("txid"));
				listMap.add(map);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
				
		return listMap;
	}
	
	/**
	 * Send transaction from RPC command
	 * @param paramMap map of raw transaction id
	 * @param config configuration of server
	 * @return result of sending transaction
	 * @throws JSONException
	 */
	public Map<String, Object> sendTx(Map<String, Object> paramMap, ServerConfig config) throws JSONException
	{
		Map<String, Object> map = new HashMap<String, Object>();

		Object[] params = new Object[1];
		params[0] = paramMap.get("rawtx");

		String strSendResult = HdacUtil.getDataFromRPC("sendrawtransaction", params, config);
		JSONObject objSendResult = new JSONObject(strSendResult);

		String resultTX = "";
		boolean success = false;

		if (objSendResult.get("error").equals(null))
		{
			resultTX = objSendResult.getString("result");
			success = true;
		}
		else
		{
			resultTX = objSendResult.getJSONObject("error").toString();
		}

		map.put("txid", resultTX);
		map.put("success", success);
		
		return map;
	}
	
	/**
	 * Get token balance from RPC command
	 * @param paramMap address, token name
	 * @param config configuration of server
	 * @return token balance
	 * @throws JSONException
	 */
	public String getAddressAsset(Map<String, Object> paramMap, ServerConfig config) throws JSONException
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

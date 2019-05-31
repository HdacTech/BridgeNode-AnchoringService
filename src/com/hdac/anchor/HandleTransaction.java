/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.anchor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hdac.db.MariaDao;
import com.hdac.proterty.ServerConfig;
import com.hdac.service.RpcService;
import com.hdac.util.HdacUtil;
import com.hdacSdk.hdacWallet.HdacWallet;

/**
 * HandleTransaction class
 * Send transaction and insert result of transaction
 * @version 0.8
 * @see org.apache.http.HttpEntity
 * @see org.apache.http.client.methods.CloseableHttpResponse
 * @see org.apache.http.impl.client.HttpClients
 */
public class HandleTransaction {
	
	private MariaDao mDao;
	private HdacWallet mWallet;
	
	private String anchorMode;
	private int changeAddressTxCount;
	private int anchorCount;
	private int changeAddressTerm;
	private Map<String, Object> serverConfig_ = null;
	
	private int currentAddressIndex = 0;
	private int nextAddressIndex = 0;
	
	/**
	 * Initialize variable and make anchoring address, insert DB after send transaction 
	 * @param wallet (HdacWallet) 
	 * @param dao (MariaDao) 
	 * @param serverConfig (ServerConfig) 
	 * @param anchorConfig (AnchorConfig) 
	 * @param data (String) anchoring data to send
	 * @return (boolean) result of sending transaction 
	 */
	public boolean handleTransaction(HdacWallet wallet, MariaDao dao,  Map<String, Object> serverConfig,  Map<String, Object> anchorConfig, String data)
	{
		mDao = dao;
		mWallet = wallet;
		anchorMode = anchorConfig.get("anchor_mode").toString();
		changeAddressTxCount = Integer.valueOf(anchorConfig.get("change_size").toString());
		anchorCount = Integer.valueOf(anchorConfig.get("anchor_size").toString());
		changeAddressTerm = Integer.valueOf(anchorConfig.get("change_term").toString());
		serverConfig_ = serverConfig;
		
		boolean initDB = checkInit();
		System.out.println("***** HandleTransaction : initDB : " + initDB);
		
		String fromAddress = makeFromAddress();
		String toAddress = makeToAddress(initDB);
		System.out.println("***** HandleTransaction : fromAddress : " + fromAddress + " toAddress : " + toAddress);
		
		boolean result = handleDB(initDB, fromAddress, toAddress, data);
					
		return result;
	}
	
	/**
	 * Check anchor_histoy DB table is null or not 
	 * @return (boolean) result of check (table empty is true)
	 */
	private boolean checkInit() 
	{
		boolean result = false;
		Map<String, Object> address = new HashMap<String, Object>();
		
		address = mDao.getLastHistory();
		if(address == null) 
			return true;
		
		return result;
	}
	
	/**
	 * Make anchoring 'from address' 
	 * @return (String) address
	 */
	private String makeFromAddress()
	{
		return mWallet.getHdacAddress(true, 0);
	}
	
	/**
	 * Get count of already sent transaction 
	 * @return (int) count
	 */
	private int getSentCount()
	{
		int count = 1;
		Map<String, Object> result = mDao.getLastHistory();
		
		if(result != null)
			count = Integer.valueOf(result.get("block_cnt").toString());
		
		return count / anchorCount;
	}
	
	/**
	 * Verify that can send transaction by check current side block height
	 * @return (boolean) check result (can send is true)
	 */
	private boolean checkSend()
	{
		boolean possible = false;
		
		int currentCount = (int) (getBlockCount() / anchorCount);
		int dbCount = getSentCount();
		System.out.println("***** HandleTransaction : checkSend : " + currentCount + " / " + dbCount);
		
		if(dbCount + 1 <= currentCount)
			return true;
		
		return possible;
	}
	
	/**
	 * Get current side chain block count from RPC
	 * @return (long) block count
	 */
	private long getBlockCount() 
	{
		String countResult = HdacUtil.getDataFromRPC("getblockcount", new String[0], serverConfig_);
		long currentCount = 0;
		try 
		{
			currentCount = new JSONObject(countResult).getLong("result");
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return currentCount;
	}
	
	/**
	 * Get anchoring 'to address'. Change it according to anchor mode
	 * @param init (boolean) DB null check result  
	 * @return (String) address
	 */
	private String makeToAddress(boolean init) 
	{
		String toAddress = "";
		int alreadySent = getSentCount();
		List<Map<String, Object>> result;
		
		try 
		{
			if(init) 
			{
				nextAddressIndex = (int) mDao.getAnchorSeq();
				toAddress = mWallet.getHdacAddress(true, nextAddressIndex);
			}
			else 
			{
				result = mDao.getLastCount();
				
				currentAddressIndex = Integer.parseInt(result.get(0).get("address_index").toString());
								
				if(anchorMode.equals("count"))
				{
					if(result.size() > 1) 
					{
						alreadySent = (Integer.parseInt(result.get(0).get("block_cnt").toString()) - 
								Integer.parseInt(result.get(1).get("block_cnt").toString())) / anchorCount;
					}
				
					if(alreadySent >= changeAddressTxCount) 
					{
						nextAddressIndex = (int) mDao.getAnchorSeq();
						toAddress = mWallet.getHdacAddress(true, nextAddressIndex);
					}
					else 
					{
						toAddress = mWallet.getHdacAddress(true, currentAddressIndex);
						nextAddressIndex = currentAddressIndex;
					}
				}
				else if(anchorMode.equals("date"))
				{
					
					String lastCreateDate = mDao.getLastHistory().get("create_dt").toString();
					String parseDate = lastCreateDate.substring(0, lastCreateDate.indexOf(" "));
					
					LocalDate startDate = LocalDate.parse(parseDate);
					LocalDate endDate = startDate.plusMonths(changeAddressTerm);
					LocalDate today = LocalDate.now();
					
					if(today.isEqual(endDate)) 
					{
						nextAddressIndex = (int) mDao.getAnchorSeq();
						toAddress = mWallet.getHdacAddress(true, nextAddressIndex);
					}
					else 
					{
						toAddress = mWallet.getHdacAddress(true, currentAddressIndex);
						nextAddressIndex = currentAddressIndex;
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return toAddress;
	}
	
	/**
	 * Get anchoring 'to address'. Change it according to anchor mode
	 * @param checkInit (boolean) DB null check result
	 * @param fromAddress (String) 
	 * @param toAddress (String) 
	 * @param data (String) anchoring data  
	 * @return (boolean) transaction send result
	 */
	private boolean handleDB(boolean checkInit, String fromAddress, String toAddress, String data)
	{
		Map<String, Object> param = new HashMap<String, Object>();
		boolean sendResult = false;
		
		if(checkInit)
		{
			try 
			{	
				param.put("blockcnt", 1);
				param.put("blockhash", "Ready");
				param.put("to_address", toAddress);
				param.put("from_address", fromAddress);
				param.put("address_index", nextAddressIndex);
				param.put("anchor_size", anchorCount);
				param.put("change_size", changeAddressTxCount);
				mDao.insertAnchorInfo(param);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			if(checkSend())
			{
				sendResult = sendTransaction(toAddress, fromAddress, data);
				
				if(sendResult) 
				{
					try 
					{
						param.put("blockcnt", (getSentCount() + 1) * anchorCount);
						param.put("blockhash", data);
						param.put("to_address", toAddress);
						param.put("from_address", fromAddress);
						param.put("address_index", nextAddressIndex);
						param.put("anchor_size", anchorCount);
						param.put("change_size", changeAddressTxCount);
						
						if(currentAddressIndex == nextAddressIndex || checkInit) 
						{
							mDao.updateAnchorInfo(param);
						}
						else 
						{
							mDao.insertAnchorInfo(param);
						}
					}
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
			}
		}

		return sendResult;
	}
	
	/**
	 * send transaction 
	 * @param toAddress (String) 
	 * @param fromAddress (String)
	 * @param hash (String) anchoring data   
	 * @return (boolean) transaction send result
	 */
	private boolean sendTransaction(String toAddress, String fromAddress, String hash) 
	{
		boolean bSuccess = false;
		
		try
		{
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("addresses", fromAddress);
						
			//String utxoData = getHttpData("http://" + webIP + ":" + webPort + "/addrs/" + fromAddress +"/utxo", "GET");
			List<JSONObject> utxoData = RpcService.getInstance().getUtxos(paramMap, ServerConfig.getInstance().getMainChainInfo());
			JSONArray jsonArray = new JSONArray(utxoData);
						
			Map<String, Object> map = new HashMap<String, Object>();
			List<String> seedWords = mDao.getSeed(map);
			List<String> seed = HdacUtil.decodeSeed(seedWords, HdacUtil.getKey());
			HdacWallet wallet = HdacUtil.getHdacWallet(seed, null);
			
			paramMap.clear();
			paramMap.put("blockhash", hash);
						
			String raw_tx = HdacUtil.getRawTransaction(wallet, jsonArray, paramMap, toAddress);

			paramMap.clear();
			paramMap.put("rawtx", raw_tx);

			//String sendData = getHttpData("http://" + webIP + ":" + webPort + "/tx/send?rawtx=" + raw_tx, "POST");
			String sendData = RpcService.getInstance().sendRawTransaction(raw_tx, ServerConfig.getInstance().getMainChainInfo());
			if(sendData != null)
			{
				System.out.println("***** HandleTransaction : Result tx : " + sendData);
				bSuccess = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return bSuccess;
	}
	
	/**
	 * Get data from webserver API (utxo, tx send, asset info) 
	 * @param url (String) API url 
	 * @param request (String) request method  
	 * @return (String) result of API
	 */
	public String getHttpData(String url, String request)
	{
		StringBuilder result = new StringBuilder();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response1 = null;
		HttpUriRequest rq = null;
		if(request.equals("GET"))
		{
			rq = new HttpGet(url);
		}
		else
		{
			rq = new HttpPost(url);
		}

		try
		{
			response1 = httpclient.execute(rq);
			HttpEntity entity1 = response1.getEntity();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(entity1.getContent()));

		    String line = "";
		    while ((line = rd.readLine()) != null)
		    {
		    	result.append(line);
		    }

		    EntityUtils.consume(entity1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		    try
		    {
		    	if (response1 != null)
		    		response1.close();
			}
		    catch (IOException e)
		    {
			}
		}

		return result.toString();
	}

}

/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.anchor;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.crypto.digests.SHA256Digest;

import com.hdac.util.AnchorConfig;
import com.hdac.util.AnchorUtil;
import com.hdac.util.HashUtil;
import com.hdac.db.MariaDao;
import com.hdac.util.HdacUtil;
import com.hdac.util.JsonUtil;
import com.hdac.util.ServerConfig;
import com.hdacSdk.hdacWallet.HdacWallet;

/**
 * Anchoring main class
 * @version 0.8
 * @see java.util.ArrayList
 * @see java.util.HashMap
 * @see org.json.JSONArray
 * @see org.json.JSONObject
 */
public class Anchor {
	
	private int anchorCount;
	private int changeAddressTxCount;
	private String anchorMode;
	private int changeAddressTerm;
	private String contractLibPath;
	
	private ServerConfig serverConfig_;
	private AnchorConfig anchorConfig_;
	private MariaDao mDao;
	
	private boolean firstCheck = false;
	private int currentAddressIndex = 0;
	private int nextAddressIndex = 0;
			
	/**
	 * Set initial field from configuration 
	 */
	public Anchor() 
	{
		serverConfig_ = HdacUtil.getServerType("private");
		anchorConfig_ = AnchorUtil.getAnchorConfig();
		
		anchorCount = Integer.parseInt(anchorConfig_.getAnchorCount());
		anchorMode = anchorConfig_.getAnchorMode();
		changeAddressTxCount = Integer.parseInt(anchorConfig_.getChangeAddressCount());
		changeAddressTerm = Integer.parseInt(anchorConfig_.getChangeAddressTerm());
		contractLibPath = anchorConfig_.getContractLibPath();
		
		mDao = new MariaDao();
	}
		
	public static void main(String[] args)
	{
		Anchor anchor = new Anchor();
		long currentBlockCnt = anchor.getBlockCount();
		anchor.anchorData(currentBlockCnt);
	}
			
	/**
	 * Get current block count of side chain through RPC command
	 * @return Current block count
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
		sysOut("getBlockCount", "current block count : " + currentCount);
		return currentCount;
	}
	
	/**
	 * Get last block count from DB already sent 
	 * @return block count from DB
	 */
	private long getCurrentAnchorCount() 
	{
		int currentCnt = 0;

		try 
		{
			Map<String, Object> result = mDao.getLastHistory();
			if(result == null) 
			{
				currentCnt = 1;
			}
			else 
			{
				currentCnt = Integer.valueOf(result.get("block_cnt").toString());
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		sysOut("getBlockCount", "saved db block count : " + currentCnt);		
		return currentCnt;
	}
		
	/**
	 * Save anchoring history to DB after sending transaction 
	 * @param blockcnt current block count of side chain
	 */
	private void anchorData(long blockcnt) 
	{
		Map<String, Object> param = new HashMap<String, Object>();
						
		int idx = (int) (blockcnt / anchorCount);  //current Block height
		long currentCnt = getCurrentAnchorCount();  //saved DB Block height
				
		int startIdx = 0;
		boolean sendResult = true;
		if(currentCnt / anchorCount > 0) 
			startIdx = (int)(currentCnt / anchorCount);  //set start height if DB is not null   
		
		Map<String, Object> address = this.getAnchorAddress(currentCnt);
		sysOut("anchorData", "idx : " + idx + " startIdx : " + startIdx + " first : " + firstCheck);
		if(firstCheck) 
		{
			try {	
					param.put("blockcnt", 1);
					param.put("blockhash", "Ready");
					param.put("to_address", address.get("to_address").toString());
					param.put("from_address", address.get("from_address").toString());
					param.put("address_index", nextAddressIndex);
					param.put("anchor_size", anchorCount);
					param.put("change_size", changeAddressTxCount);
					mDao.insertAnchorInfo(param);
				} catch (Exception e) {
					e.printStackTrace();
			}
		}
		
		if(startIdx + 1 <= idx) 
		{
			try 
			{
				//-->send merkle root of all block
				List<String> merkleList = makeMerkleList(startIdx);
				List<String> finalMerkle = makeHash(merkleList);
				String hash = finalMerkle.get(0).toString();
				String contractHash = getFileHash();
				String finalData = hash + "|" + getListAsset() + "|" + contractHash;
				sendResult = sendTransaction(address.get("to_address").toString(), address.get("from_address").toString(), finalData);
								
				if(sendResult) 
				{
					param.put("blockcnt", (startIdx + 1) * anchorCount);
					param.put("blockhash", finalData);
					param.put("to_address", address.get("to_address").toString());
					param.put("from_address", address.get("from_address").toString());
					param.put("address_index", nextAddressIndex);
					param.put("anchor_size", anchorCount);
					param.put("change_size", changeAddressTxCount);
					
					if(currentAddressIndex == nextAddressIndex || firstCheck) 
					{
						mDao.updateAnchorInfo(param);
					}
					else 
					{
						mDao.insertAnchorInfo(param);
					}
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Make token information from RPC command. Total qty, qty of contract address in side chain and main chain
	 * @return token information
	 */
	private String getListAsset() 
	{
		JSONObject returnObj = new JSONObject();
		RpcApi api = new RpcApi();
		
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
							String assetRemain = api.getAddressAsset(param, serverConfig_);
							
							//--> balance on contract address in main chain (used token)
							Object[] params = new Object[1];
							params[0] = tokenDB.get(j).get("contractAddress");
							String balanceResult = HdacUtil.getDataFromRPC("getaddressbalance", params, HdacUtil.getServerType("public"));
							JSONObject balanceObj = new JSONObject(balanceResult).getJSONObject("result");
							int swapRatio = Integer.parseInt(tokenDB.get(j).get("tokenSwapRatio").toString());
							String mainBalance = String.valueOf(balanceObj.getDouble("balance") * Math.pow(10, -8) * swapRatio);
							
							returnObj.put(assetName, assetQty + "/" + assetRemain + "/" + mainBalance);
						}
					}
				}
			}
			else 
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
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		sysOut("getListAsset", "info : " + returnObj.toString());		
		return returnObj.toString();
	}
	
	/**
	 * Get seed words from DB or initial generating
	 * @return seed words
	 */
	private List<String> seedCheck()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> seedWords = new ArrayList<String>();
		
		seedWords = mDao.getSeed(map);
		
		if ((seedWords == null) || (seedWords.size() <= 0)) 
		{
			List<String> seed = HdacUtil.getSeedWord(null);
			if (seed != null)
			{
				map.clear();
				List<String> encSeed = HdacUtil.encodeSeed(seed, HdacUtil.getKey());
				map.put("seedWords", encSeed);
				mDao.insertSeedWords(map);
			}
			map.clear();
			seedWords = mDao.getSeed(map);
		}
		
		return seedWords;
	}
	
	/**
	 * Get anchoring address from DB or initial generating
	 * @param currentCnt current block count
	 * @return anchoring address
	 */
	private Map<String, Object> getAnchorAddress(long currentCnt) 
	{
		String fromAddress = "";
		String toAddress = "";
		Map<String, Object> address = new HashMap<String, Object>();
		
		int sentTxCount = (int) (currentCnt / anchorCount);
				
		try
		{
			address = mDao.getLastHistory();
			
			List<String> seedWords = seedCheck();
			List<String> seed = HdacUtil.decodeSeed(seedWords, HdacUtil.getKey());
			HdacWallet wallet = HdacUtil.getHdacWallet(seed, null);
			
			if(address != null) //Case there is at least one history in DataBase
			{
				toAddress = makeToAddress(sentTxCount, wallet);
				address.put("to_address", toAddress);
			}
			else  //Case there is no data in DB 
			{
				firstCheck = true;
				
				fromAddress = wallet.getHdacAddress(true, 0);
				toAddress = makeToAddress(sentTxCount, wallet);
				
				address = new HashMap<String, Object>();
				address.put("to_address", toAddress);
				address.put("from_address", fromAddress);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		sysOut("getAnchorAddress", "address : " + address.toString());
		return address;
	}
	
	/**
	 * Make anchoring destination address depends on anchor mode
	 * @param alreadySent transaction count already sent
	 * @param wallet HdacWallet
	 * @return anchoring destination address
	 */
	private String makeToAddress(int alreadySent, HdacWallet wallet) 
	{
		String toAddress = "";
		List<Map<String, Object>> result;
		
		try 
		{
			if(firstCheck) 
			{
				nextAddressIndex = (int) mDao.getAnchorSeq();
				toAddress = wallet.getHdacAddress(true, nextAddressIndex);
			}
			else 
			{
				result = mDao.getLastCount();
				
				currentAddressIndex = Integer.parseInt(result.get(0).get("address_index").toString());
								
				if(anchorMode.equals("COUNT"))
				{
					if(result.size() > 1) 
					{
						alreadySent = (Integer.parseInt(result.get(0).get("block_cnt").toString()) - 
								Integer.parseInt(result.get(1).get("block_cnt").toString())) / anchorCount;
					}
				
					if(alreadySent >= changeAddressTxCount) 
					{
						nextAddressIndex = (int) mDao.getAnchorSeq();
						toAddress = wallet.getHdacAddress(true, nextAddressIndex);
					}
					else 
					{
						toAddress = wallet.getHdacAddress(true, currentAddressIndex);
						nextAddressIndex = currentAddressIndex;
					}
				}
				else if(anchorMode.equals("DATE"))
				{
					
					String lastCreateDate = mDao.getLastHistory().get("create_dt").toString();
					String parseDate = lastCreateDate.substring(0, lastCreateDate.indexOf(" "));
					
					LocalDate startDate = LocalDate.parse(parseDate);
					LocalDate endDate = startDate.plusMonths(changeAddressTerm);
					LocalDate today = LocalDate.now();
					
					if(today.isEqual(endDate)) 
					{
						nextAddressIndex = (int) mDao.getAnchorSeq();
						toAddress = wallet.getHdacAddress(true, nextAddressIndex);
					}
					else 
					{
						toAddress = wallet.getHdacAddress(true, currentAddressIndex);
						nextAddressIndex = currentAddressIndex;
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		sysOut("makeToAddress", "alreadySent : " + alreadySent + " anchorMode : " + anchorMode + " address : " + toAddress);
		return toAddress;
	}
		
	/**
	 * Make merkle list of side chain block from RPC command
	 * @param startIdx block index
	 * @return list of merkle list
	 */
	private List<String> makeMerkleList(int startIdx)
	{
		List<String> merkleList = new ArrayList<String>();
		
		Object[] params = new Object[1];
		
		try 
		{
			for(int i = 0; i < anchorCount; i++) 
			{
				params[0] = String.valueOf(startIdx * anchorCount + i);
				String blockResult = HdacUtil.getDataFromRPC("getblock", params, serverConfig_);
				String merkle = new JSONObject(blockResult).getJSONObject("result").getString("merkleroot");
				merkleList.add(merkle);
			}
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		sysOut("makeMerkleList", "merkle list length : " + merkleList.size());
		return merkleList;
	}
	
	/**
	 * Calculate hash data from merkle list
	 * @param list merkle list
	 * @return last hash data
	 */
	private List<String> makeHash(List<String> list) 
	{
		List<String> tempArray = new ArrayList<String>();
		int arraySize = list.size();
								
		for(int i = 0; i < arraySize; i += 2) 
		{
			byte[] hex1 = reverseHexStringToByteArray(list.get(i).toString());
			byte[] hex2 = reverseHexStringToByteArray(list.get(i + 1).toString());
			tempArray.add(binaryHash(hex1, hex2));
		}
		sysOut("makeHash", "hash list length : " + tempArray.size());
		if(tempArray.size() > 1) makeHash(tempArray);
						
		return tempArray;
	}
	
	/**
	 * Reverse byte array
	 * @param arr byte array
	 * @return reversed byte array
	 */
	private byte[] reverse(byte[] arr) 
	{
		byte[] buf = new byte[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			buf[i] = (byte)((arr[arr.length - i - 1] >>> (arr.length * i)) & 0xFF);
		}

		return buf;		
	}
	
	/**
	 * Make hash data from two byte array
	 * @param hex1 first byte array
	 * @param hex2 second byte array
	 * @return hash data
	 */
	private String binaryHash(byte[] hex1, byte[] hex2)	
	{
		byte[] sum_byte = ArrayUtils.addAll(hex1, hex2);

 		// execute double hash
 		byte[] hash_byte = HashUtil.SHA256toByte(HashUtil.SHA256toByte(sum_byte));

 		// change binary to String
 		StringBuilder sb = new StringBuilder();
 		for (byte bb : hash_byte)
		{
 			sb.insert(0, String.format("%02X", bb & 0xFF)); 
		}
		return sb.toString();
	}
	
	/**
	 * Make reversed byte array from hex string
	 * @param str hex string
	 * @return byte array
	 */
	private byte[] reverseHexStringToByteArray(String str) 
	{
	    int len = str.length() / 2;
	    byte[] data = new byte[len];
	    for (int i = 0; i < len; i++)
	    {
	        data[i] = (byte)((Character.digit(str.charAt((len - i - 1) * 2), 16) << 4) + Character.digit(str.charAt((len - i) * 2 - 1), 16));
	    }
	    return data;
	}
		
	/**
	 * Get Hash data from contract library file
	 * @return hash data
	 */
	private String getFileHash() 
	{
		String hashedResult = "";
		
		File file = new File(contractLibPath);
		if(file.exists()) 
		{
			byte[] data = new byte[(int)file.length()];
			
			try 
			{
				FileInputStream input = new FileInputStream(contractLibPath);
				input.read(data);
												
				SHA256Digest md = new SHA256Digest();
				md.update(data, 0, data.length);
				
				byte[] hashed = new byte[md.getDigestSize()];
				md.doFinal(hashed, 0);
				input.close();
				
				StringBuilder sb = new StringBuilder(); 
				for (byte b : hashed)
				{
					sb.append(String.format("%02X", b & 0xff)); 
				}
				hashedResult = sb.toString();
				
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else 
		{
			sysOut("getFileHash", "file exists : false");
		}
		
		return hashedResult;
	}
		
	/**
	 * Send raw transaction
	 * @param toAddress anchoring destination address
	 * @param fromAddress anchoring from address
	 * @param hash data to send
	 * @return result of sending transaction
	 */
	private boolean sendTransaction(String toAddress, String fromAddress, String hash) 
	{
		boolean bSuccess = false;
		
		try
		{
			RpcApi api = new RpcApi();
						
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("addresses", fromAddress);
			List<Map<String, Object>> utxoResult = api.getUtxos(paramMap, HdacUtil._PUBLIC_);
			JSONArray jsonArray = JsonUtil.toJsonArray(utxoResult);
						
			Map<String, Object> map = new HashMap<String, Object>();
			List<String> seedWords = mDao.getSeed(map);
			List<String> seed = HdacUtil.decodeSeed(seedWords, HdacUtil.getKey());
			HdacWallet wallet = HdacUtil.getHdacWallet(seed, null);
			
			paramMap.clear();
			paramMap.put("blockhash", hash);
						
			String raw_tx = HdacUtil.getRawTransaction(wallet, jsonArray, paramMap, toAddress);

			paramMap.clear();
			paramMap.put("rawtx", raw_tx);

			Map<String, Object> resultMap = api.sendTx(paramMap, HdacUtil._PUBLIC_);

			bSuccess = (boolean)resultMap.get("success");
			if (bSuccess)
			{
				String anchorTxid = resultMap.get("txid").toString();
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return bSuccess;
	}
	
	private void sysOut(String method, String outStr)
	{
		System.out.println("***** " + method + " : " + outStr);
	}

}

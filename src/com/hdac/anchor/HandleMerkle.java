/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.anchor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

import com.hdac.db.MariaDao;
import com.hdac.util.HashUtil;
import com.hdac.util.HdacUtil;

/**
 * HandleMerkle class
 * Make merkleroot hash data
 * @version 0.8
 * @see java.util.ArrayList
 * @see org.json.JSONObject
 */
public class HandleMerkle {
	
	private Map<String, Object> serverConfig_ = null;
	private MariaDao mDao;
	private int anchorCount;
	
	/**
	 * Initialize variable and make merkle root hash data
	 * @param config (ServerConfig) side chain server config
	 * @param dao (MariaDao)
	 * @param count (int) anchorCount properties
	 * @return (String) merkle root hash 
	 */
	public String getMerkleRootHash(Map<String, Object> config, MariaDao dao, int count)
	{
		serverConfig_ = config;
		mDao = dao;
		anchorCount = count;
		
		long dbHeight = getDBCount();
		System.out.println("***** HandleMerkle : dbHeight : " + dbHeight);
		
		int startIdx = 0;
		if(dbHeight / anchorCount > 0) 
			startIdx = (int)(dbHeight / anchorCount);
				
		List<String> merkleList = makeMerkleList(startIdx);
		List<String> finalMerkle = makeHash(merkleList);
		String hash = finalMerkle.get(0).toString();
				
		return hash;
	}
	
	/**
	 * Get last block count from anchor_history DB table
	 * @return (long) saved block count 
	 */
	public long getDBCount() 
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
		return currentCnt;
	}
	
	/**
	 * Make merkleroot list of side chain, size of list is anchorCount properties 
	 * @param startIdx (int) start block index
	 * @return (List) merkleroot list 
	 */
	private List<String> makeMerkleList(int startIdx)
	{
		List<String> merkleList = new ArrayList<String>();
		
		Object[] params = new Object[1];
		
		try 
		{
			System.out.println("***** HandleMerkle : startIdx : " + startIdx * anchorCount);
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
		return merkleList;
	}
	
	/**
	 * Get final merkleroot hash from list by recursive
	 * @param list (List) merkleroot list
	 * @return (List) final merkleroot hash list, size 1 
	 */
	private List<String> makeHash(List<String> list) 
	{
		List<String> tempArray = new ArrayList<String>();
		int arraySize = list.size();
		System.out.println("***** HandleMerkle : makeHash : " + arraySize);
								
		for(int i = 0; i < arraySize; i += 2) 
		{
			byte[] hex1 = reverseHexStringToByteArray(list.get(i).toString());
			byte[] hex2 = reverseHexStringToByteArray(list.get(i + 1).toString());
			tempArray.add(binaryHash(hex1, hex2));
		}
		if(tempArray.size() > 1) makeHash(tempArray);
						
		return tempArray;
	}
	
	/**
	 * Binary hash function
	 * @param hex1 (byte[]) first byte array 
	 * @param hex2 (byte[]) second byte array 
	 * @return (String) hash result 
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
	 * Get Byte array from hex string after reverse
	 * @param str (String) hex string
	 * @return (byte[]) converted byte array 
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

}

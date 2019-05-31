/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.anchor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hdac.db.MariaDao;
import com.hdac.proterty.AnchorConfig;
import com.hdac.proterty.ServerConfig;
import com.hdac.util.HdacUtil;
import com.hdacSdk.hdacWallet.HdacWallet;

/**
 * Anchor class
 * @version 0.8
 * @see java.util.ArrayList
 * @see java.util.HashMap
 * @see org.json.JSONArray
 * @see org.json.JSONObject
 */
public class Anchor {
	
	private int anchorCount;
	private String contractLibPath;
	
	private Map<String, Object> serverConfig_ = null;
	private Map<String, Object> anchorConfig_ = null;
	private MariaDao mDao;
	private HdacWallet wallet;
				
	/**
	 * Constructor
	 * Set initial field from configuration
	 */
	public Anchor() 
	{
		serverConfig_ = ServerConfig.getInstance().getSideChainInfo();
		anchorConfig_ = AnchorConfig.getInstance().getAnchorConfig();
		System.out.println(serverConfig_.toString());
		System.out.println(anchorConfig_.toString());
		anchorCount = Integer.parseInt(anchorConfig_.get("anchor_size").toString());
		contractLibPath = anchorConfig_.get("lib_path").toString();
				
		mDao = new MariaDao();
		
		List<String> seedWords = seedCheck();
		List<String> seed = HdacUtil.decodeSeed(seedWords, HdacUtil.getKey());
		wallet = HdacUtil.getHdacWallet(seed, null);
	}
	
	/**
	 * Send anchor transaction after make data (Merkleroot hash, asset info, contract lib hash)
	 */
	public void anchorStart()
	{
		String hash = new HandleMerkle().getMerkleRootHash(serverConfig_, mDao, anchorCount);
		System.out.println("***** Anchor : hash : " + hash);
		
		String assetInfo = new HandleAsset().getAssetInfo(serverConfig_, mDao);
		System.out.println("***** Anchor : assetInfo : " + assetInfo);
		
		String contracHash = new HandleContract().getContractHash(contractLibPath);
		System.out.println("***** Anchor : contracHash : " + contracHash);
		
		String finalData = hash + "|" + assetInfo + "|" + contracHash;
		
		boolean success = new HandleTransaction().handleTransaction(wallet, mDao, serverConfig_, anchorConfig_, finalData);
		System.out.println("***** Anchor : transaction : " + success);
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
}

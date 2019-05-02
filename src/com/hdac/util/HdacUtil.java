/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.io.Resources;
import org.bitcoinj.core.ECKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hdac.util.HTransaction;
import com.hdac.util.JsonUtil;
import com.hdacSdk.hdacWallet.HdacCoreAddrParams;
import com.hdacSdk.hdacWallet.HdacWallet;
import com.hdacSdk.hdacWallet.HdacWalletManager;
import com.hdacSdk.hdacWallet.HdacWalletUtils;
import com.hdacSdk.hdacWallet.HdacWalletUtils.NnmberOfWords;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
//import com.hdacSdk.hdacWallet.HdacTransaction;

/**
 * Utility class of Hdac RPC
 * @version 0.8
 * @see org.apache.http.client.methods.HttpPost
 * @see org.apache.http.impl.client.HttpClients
 * @see org.json.JSONArray
 * @see org.json.JSONObject
 *
 */
public class HdacUtil
{
	public static ServerConfig _PUBLIC_;
	public static ServerConfig _PRIVATE_;
	
	static
	{
		_PUBLIC_ = new ServerConfig();
		_PRIVATE_ = new ServerConfig();

		try
		{
			setServerConfig(_PUBLIC_, "config/hdac-hdac.properties");
			setServerConfig(_PRIVATE_, "config/hdac-side.properties");
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Function to set configuration of server
	 * @param config ServerConfig object
	 * @param resource path of properties
	 */
	private static void setServerConfig(ServerConfig config, String resource)
	{
		Properties properties = new Properties();

		try
		{
			Reader reader = Resources.getResourceAsReader(resource);
			properties.load(reader);

			config.setRpcIp(properties.getProperty("rpcIp"));
			config.setRpcPort(properties.getProperty("rpcPort"));
			config.setRpcUser(properties.getProperty("rpcUser"));
			config.setRpcPassword(properties.getProperty("rpcPassword"));
			config.setChainName(properties.getProperty("chainName"));

			reader.close();
		}
		catch (Exception e)
		{
			System.out.println("hdac rpc error : " + e);
		}
	}

	/**
	 * Get server configuration according to path
	 * @param path server type
	 * @return server configuration
	 */
	public static ServerConfig getServerType(String path)
	{
		if ("public".equals(path))
			return HdacUtil._PUBLIC_;

		return HdacUtil._PRIVATE_;
	}

	/**
	 * Function to get data through RPC command
	 * @param method RPC method name
	 * @param params parameter of RPC command
	 * @param config server configuration
	 * @return result string of RPC 
	 */
	public static String getDataFromRPC(String method, Object[] params, ServerConfig config)
	{
		StringBuilder result = new StringBuilder();
		CloseableHttpResponse response1 = null;

		//System.out.println("RPC CONFIG = " + config.getRpcIp() + " : " + config.getRpcPort() + " / " + config.getChainName());
		try
		{
			StringBuilder auth = new StringBuilder("Basic ");
			//auth.append(Base64.encode("hdacrpc:hdac1234".getBytes()));
			auth.append(Base64.encode((config.getRpcUser() + ":" + config.getRpcPassword()).getBytes()));

			String body = getBody(method, params);

			CloseableHttpClient httpclient = HttpClients.createDefault();
			//HttpPost httpPost = new HttpPost(_RPC_);
			HttpPost httpPost = new HttpPost(config.getRpcIp() + ":" + config.getRpcPort());

			httpPost.addHeader("content-type", "application/json");
			httpPost.addHeader("Authorization", auth.toString());

			HttpEntity entity = new StringEntity(body);
	        httpPost.setEntity(entity);

			response1 = httpclient.execute(httpPost);

		    HttpEntity entity1 = response1.getEntity();

		    BufferedReader rd = new BufferedReader(new InputStreamReader(entity1.getContent()));

		    String line = "";
		    while ((line = rd.readLine()) != null)
		    {
		    	result.append(line);
		    }

		    //System.out.println(result);

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

	/**
	 * Function to make RPC command body
	 * @param method RPC method name
	 * @param params parameter of RPC command
	 * @return string of RPC body
	 * @throws JSONException
	 */
	private static String getBody(String method, Object[] params) throws JSONException
	{
		String id = (int)(Math.random() * 10000) + "";

		JSONObject obj = new JSONObject();
		obj.put("jsonrpc", "2.0");
		obj.put("id", id);
		obj.put("method", method);
		obj.put("params", params);

		return obj.toString();
	}

	/**
	 * Function to get secret key to encode
	 * @return string key
	 */
	public static String getKey()
	{
		return "IOT";//HdacUtil.chainName;
	}

	/**
	 * Function to generate random seed words
	 * @param passPhrase string of passPhrase
	 * @return seed words list
	 */
	public static List<String> getSeedWord(String passPhrase)
	{
		HdacWalletUtils.NnmberOfWords[] num =
		{
			NnmberOfWords.MNEMONIC_12_WORDS,
			NnmberOfWords.MNEMONIC_15_WORDS,
			NnmberOfWords.MNEMONIC_18_WORDS,
			NnmberOfWords.MNEMONIC_21_WORDS,
			NnmberOfWords.MNEMONIC_24_WORDS,
		};
		int rand = (int)(Math.random() * 5);
		List<String> seedWords = HdacWalletUtils.getRandomSeedWords(num[rand]);

		HdacWallet hdacWallet = getHdacWallet(seedWords, passPhrase);

		if (hdacWallet.isValidWallet())
			return seedWords;

		return null;
	}

	/**
	 * Function to get HdacWallet object generated from seed words and passPhrase 
	 * @param seedWords list of seed words
	 * @param passPhrase passPhrase
	 * @return new HdacWallet object
	 */
	public static HdacWallet getHdacWallet(List<String> seedWords, String passPhrase)
	{
		HdacCoreAddrParams params = new HdacCoreAddrParams(true);	// hdac network parameter (true : public network / false : private network)
		return HdacWalletManager.generateNewWallet(seedWords, passPhrase, params);
	}

	/**
	 * Function to encode seed words
	 * @param seed list of seed words
	 * @param key secret key
	 * @return encoded seed words list
	 */
	public static List<String> encodeSeed(List<String> seed, String key)
	{
		List<String> encSeed = new ArrayList<String>();
		for (String word : seed)
		{
			encSeed.add(CipherUtil.AesEncode(word, key));
		}
		return encSeed;
	}

	/**
	 * Function to decode seed words
	 * @param seed encoded seed words list
	 * @param key secret key
	 * @return decoded seed words list
	 */
	public static List<String> decodeSeed(List<String> seed, String key)
	{
		List<String> decSeed = new ArrayList<String>();
		for (String word : seed)
		{
			decSeed.add(CipherUtil.AesDecode(word, key));
		}
		return decSeed;
	}
	
	/**
	 * Function to make raw transaction
	 * @param wallet HdacWallet object
	 * @param data UTXO data 
	 * @param paramMap map of data to send
	 * @param toAddress destination address
	 * @return hex string of raw transaction 
	 */
	public static String getRawTransaction(HdacWallet wallet, JSONArray data, Map<String, Object> paramMap, String toAddress)
	{
		System.out.println("getRawTransaction data " + data);

		//HdacTransaction transaction = new HdacTransaction(wallet);
		HTransaction transaction = new HTransaction(wallet);
		
		String sendData = "";
		
		if (paramMap.size() > 0)
			sendData = JsonUtil.toJsonString(paramMap).toString();

		double balance = 0;
		try
		{
			int len = data.length();
	    	for (int i = 0; i < len; i++)
	    	{
				JSONObject utxo;
				utxo = data.getJSONObject(i);
				balance += utxo.getDouble("amount");
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}

		System.out.println("balance " + balance);

		//for checking balance 
		long lBalance = (long)(balance * Math.pow(10, 8));
		long fee = (long)(2 * Math.pow(10, 6) + sendData.length() * Math.pow(10, 3));
		long remain = lBalance - fee;

		System.out.println("lBalance " + lBalance);
		System.out.println("fee " + fee);
		System.out.println("remain " + remain);
		
		if (remain >= 0)
		{
			transaction.addOutput(toAddress, 0);
			transaction.addOutput(wallet.getHdacAddress(true, 0), remain);
			transaction.addOpReturnOutput(paramMap.get("blockhash").toString(), "UTF-8");
			
			try
			{
				int len = data.length();
		    	for (int i = 0; i < len; i++)
				{
					JSONObject utxo = data.getJSONObject(i);
					ECKey sign = wallet.getHdacSigKey(utxo.getString("address"));

					if (sign != null)
						transaction.addSignedInput(utxo, sign);
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

			String raw_tx = transaction.getTxBuilder().build().toHex();
			System.out.println("raw_tx " + raw_tx);
			return raw_tx;
		}
		else
		{
			System.out.println("Raw Transaction : not enough hdac");
			System.out.println("Invalid raw transaction");
		}
		return null;
	}
}
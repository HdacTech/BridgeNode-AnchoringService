/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.anchor;

import java.io.File;
import java.io.FileInputStream;

import org.spongycastle.crypto.digests.SHA256Digest;

/**
 * HandleContract class
 * Make data of contract lib hash
 * @version 0.8
 */
public class HandleContract {
	
	private String contractLibPath;
	
	/**
	 * Initialize variable and make contract lib hash data
	 * @param path (String) lib path
	 * @return (String) lib hash 
	 */
	public String getContractHash(String path)
	{
		contractLibPath = path;
		String libHash = getFileHash();
		
		return libHash;
	}
	
	/**
	 * Make contract lib file hash(SHA256) data
	 * @return (String) lib hash 
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
			System.out.println("***** HandleContract : getFileHash : File no exist");
		}
		
		return hashedResult;
	}

}

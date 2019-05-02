/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

import java.security.MessageDigest;

/**
 * Utility class of SHA256 hash method
 * @version 0.8
 * @see java.security.MessageDigest
 *
 */
public class HashUtil
{
	/**
	 * Function of SHA256, string to string
	 * @param str string to do hash
	 * @return hashed string
	 */
	public static String SHA256(String str)
	{
		try
		{
			MessageDigest sh = MessageDigest.getInstance("SHA-256"); 
			sh.update(str.getBytes()); 

			byte byteData[] = sh.digest();

			StringBuilder sb = new StringBuilder(); 
			for (byte b : byteData)
			{
				sb.append(String.format("%02X", b & 0xff)); 
			}
			return sb.toString();
		}
		catch (Exception e)
		{
		}
		return null;
	}
	
	/**
	 * Function of SHA256, byte array to byte array
	 * @param str byte array to do hash
	 * @return hashed byte array
	 */
	public static byte[] SHA256toByte(byte[] str)
	{
		try
		{
			MessageDigest sh = MessageDigest.getInstance("SHA-256"); 
			sh.update(str); 

			return sh.digest();
		}
		catch (Exception e)
		{
		}
		return null;
	}
	
	/**
	 * Function of SHA256, byte array to hex string
	 * @param str byte array to do hash
	 * @return hashed string
	 */
	public static String SHA256toHex(byte[] str)
	{
		try
		{
			MessageDigest sh = MessageDigest.getInstance("SHA-256"); 
			sh.update(str); 

			byte byteData[] = sh.digest();

			StringBuilder sb = new StringBuilder(); 
			for (byte b : byteData)
			{
				sb.append(String.format("%02X", b & 0xff)); 
			}
			return sb.toString();
		}
		catch (Exception e)
		{
		}
		return null;
	}
}
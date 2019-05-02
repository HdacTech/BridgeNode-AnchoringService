/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class of Cipher method
 * @version 0.8
 * @see javax.crypto.Cipher
 * @see javax.crypto.spec.IvParameterSpec
 * @see javax.crypto.spec.SecretKeySpec
 *
 */
public class CipherUtil
{
	/**
	 * AES encode function to seed words
	 * @param str word to encode
	 * @param key secret specific key using encode
	 * @return encoded hex string
	 */
	public static String AesEncode(String str, String key)
	{
		try
		{
			byte[] ip = getIp(key);
			SecretKeySpec keySpec = getSecretKeySpec();

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(ip));

			byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
			return toHexString(encrypted);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
 
	/**
	 * AES decode function to seed words
	 * @param str encoded string
	 * @param key secret key used to encode
	 * @return decoded string
	 */
	public static String AesDecode(String str, String key)
	{
		try
		{
			byte[] ip = getIp(key);
			SecretKeySpec keySpec = getSecretKeySpec();

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ip));
 
			byte[] byteStr = toByteArray(str);
			return new String(cipher.doFinal(byteStr), "UTF-8");
 		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Function to make 16byte of key  
	 * @param key secret key
	 * @return 16byte of key
	 */
	private static byte[] getIp(String key)
	{
		while (key.length() < 16)
			key += "|" + key;
			
		return key.substring(0, 16).getBytes();
	}
	
	/**
	 * Get secret key specification
	 * @return AES key specification object
	 */
	private static SecretKeySpec getSecretKeySpec()
	{
		byte[] keyBytes = new byte[16];
		return new SecretKeySpec(keyBytes, "AES");
	}
	
	/**
	 * Function to convert byte array to hex string
	 * @param bytes byte array
	 * @return converted hex string
	 */
	private static String toHexString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder(); 
		for (byte b : bytes)
		{
			sb.append(String.format("%02X", b & 0xff)); 
		}

		return sb.toString();
	}
	
	/**
	 * Function to convert hex string to byte array
	 * @param str hex string
	 * @return converted byte array 
	 */
	public static byte[] toByteArray(String str)
	{
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = (byte)Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
		}
		return bytes;
	}
}
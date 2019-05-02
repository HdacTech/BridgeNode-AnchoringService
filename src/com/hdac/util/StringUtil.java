/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

import java.text.DecimalFormat;

/**
 * Utility class of string method
 * @version 0.8
 * @see java.text.DecimalFormat
 *
 */
public class StringUtil
{
	public static String nvl(Object obj)
	{
		return nvl(obj, "");
	}

	public static String nvl(Object obj, String defaultStr)
	{
		if (obj == null)
			return defaultStr;

		return obj.toString();
	}

	public static String toSmallLetter(String str, int beginIndex)
	{
		if (str == null)
			return str;

		return toSmallLetter(str, beginIndex, str.length());
	}

	public static String toSmallLetter(String str, int beginIndex, int endIndex)
	{
		if (str == null)
			return str;
		if (beginIndex < 0)
			return str;
		if (endIndex > str.length())
			return str;
		if (beginIndex > endIndex)
			return str;

		String small = str.substring(beginIndex, endIndex).toLowerCase();
		str = small.concat(str.substring(endIndex));

		return str;
	}

	public static String toNumber(double num)
	{
		String formatMask = "###############";	
		DecimalFormat df = new DecimalFormat(formatMask);

		return df.format(num);
	}

	public static String toHexString(String str)
	{
		if (str == null)
			return str;

		StringBuilder sb = new StringBuilder();
		int size = str.length();
		for (int i = 0; i < size; i++)
		{
			char ch = str.charAt(i);
			sb.append(String.format("%02X", (int)ch));
		}
		System.out.println("%%%%" + sb);
		return sb.toString();
	}

	public static String hexToString(String hexStr)
	{
		if (hexStr == null)
			return hexStr;

		StringBuilder sb = new StringBuilder();

		if (hexStr.startsWith("0x"))
			hexStr = hexStr.substring(2);

		int size = hexStr.length();
		for (int i = 0; i < size; i += 2)
		{
			String str = hexStr.substring(i, i + 2);
			sb.append((char)Integer.parseInt(str, 16));
		}

		return sb.toString();
	}
	
	public static String reverseHexString(String hexStr)
	{
	    StringBuilder result = new StringBuilder();
	    for (int i = 0; i <=hexStr.length()-2; i=i+2)
	    {
	        result.append(new StringBuilder(hexStr.substring(i,i+2)).reverse());
	    }
	    return result.reverse().toString();
	}
	
	public static String toLittleEndian(long value)
	{
		byte[] a = getLittleEndian(value);

		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}

	private static byte[] getLittleEndian(long v)
	{
		byte[] buf = new byte[8];
		buf[0] = (byte)((v >>> 0x00) & 0xFF);
		buf[1] = (byte)((v >>> 0x08) & 0xFF);
		buf[2] = (byte)((v >>> 0x10) & 0xFF);
		buf[3] = (byte)((v >>> 0x18) & 0xFF);
		buf[4] = (byte)((v >>> 0x20) & 0xFF);
		buf[5] = (byte)((v >>> 0x28) & 0xFF);
		buf[6] = (byte)((v >>> 0x30) & 0xFF);
		buf[7] = (byte)((v >>> 0x38) & 0xFF);
		return buf;		
	}
}
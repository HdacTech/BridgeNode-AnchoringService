/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

import java.io.Reader;
import java.util.Properties;
import org.apache.ibatis.io.Resources;

/**
 * Class to set, get anchor configuration
 * @version 0.8
 * @see org.apache.ibatis.io.Resources
 * @see java.util.Properties
 * @see java.io.Reader
 *
 */
public class AnchorUtil
{
	public static AnchorConfig _ANCHOR_;
		
	static
	{
		_ANCHOR_ = new AnchorConfig();
		
		try
		{
			setAnchorConfig(_ANCHOR_, "config/anchor.properties");
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Set anchor configuration from properties
	 * @param config AnchorConfig Object
	 * @param resource path of properties
	 */
	private static void setAnchorConfig(AnchorConfig config, String resource)
	{
		Properties properties = new Properties();

		try
		{
			Reader reader = Resources.getResourceAsReader(resource);
			properties.load(reader);

			config.setAnchorCount(properties.getProperty("anchorCount"));
			config.setAnchorMode(properties.getProperty("anchorMode"));
			config.setChangeAddressCount(properties.getProperty("changeAddressTxCount"));
			config.setChangeAddressTerm(properties.getProperty("changeAddressTerm"));
			config.setContractLibPath(properties.getProperty("contractLibPath"));
						
			reader.close();
		}
		catch (Exception e)
		{
			System.out.println("Read Anchor Config Error : " + e);
		}
	}

	/**
	 * Get anchor configuration
	 * @return AnchorConfig object
	 */
	public static AnchorConfig getAnchorConfig()
	{
		return AnchorUtil._ANCHOR_;
	}
}
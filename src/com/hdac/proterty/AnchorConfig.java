package com.hdac.proterty;

import java.util.Map;

import com.hdac.db.MariaDao;

public class AnchorConfig
{
	private MariaDao mDao;
	private Map<String, Object> anchorConfig = null;
	
	private AnchorConfig()
	{
		mDao = new MariaDao();
		
		try
		{
			this.anchorConfig = mDao.getAnchorConfig();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static AnchorConfig getInstance()
	{
		return LazyHolder.INSTANCE;
	}	  
	
	private static class LazyHolder
	{
		private static final AnchorConfig INSTANCE = new AnchorConfig();  
	}
	
	public Map<String, Object> getAnchorConfig()
	{
		return this.anchorConfig;
	}
}
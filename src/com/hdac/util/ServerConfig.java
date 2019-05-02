/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

/**
 * Configuration class of server
 * @version 0.8
 *
 */
public class ServerConfig
{
	private String rpcIp;
	private String rpcPort;
	private String rpcUser;
	private String rpcPassword;
	private String chainName;

	public String getRpcIp()
	{
		return rpcIp;
	}
	public void setRpcIp(String rpcIp)
	{
		this.rpcIp = rpcIp;
	}
	public String getRpcPort()
	{
		return rpcPort;
	}
	public void setRpcPort(String rpcPort)
	{
		this.rpcPort = rpcPort;
	}
	public String getRpcUser()
	{
		return rpcUser;
	}
	public void setRpcUser(String rpcUser)
	{
		this.rpcUser = rpcUser;
	}
	public String getRpcPassword()
	{
		return rpcPassword;
	}
	public void setRpcPassword(String rpcPassword)
	{
		this.rpcPassword = rpcPassword;
	}
	public String getChainName()
	{
		return chainName;
	}
	public void setChainName(String chainName)
	{
		this.chainName = chainName;
	}
}
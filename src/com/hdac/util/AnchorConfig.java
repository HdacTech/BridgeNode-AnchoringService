/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.util;

/**
 * Configuration class of anchor 
 * @version 0.8
 * 
 */
public class AnchorConfig
{
	private String AnchorCount;
	private String AnchorMode;
	private String changeAddressTxCount;
	private String changeAddressTerm;
	private String contractLibPath;

	public String getContractLibPath() {
		return contractLibPath;
	}
	public void setContractLibPath(String contractLibPath) {
		this.contractLibPath = contractLibPath;
	}
	public String getAnchorCount() {
		return AnchorCount;
	}
	public String getChangeAddressTerm() {
		return changeAddressTerm;
	}
	public void setChangeAddressTerm(String changeAddressTerm) {
		this.changeAddressTerm = changeAddressTerm;
	}
	public void setAnchorCount(String anchorCount) {
		AnchorCount = anchorCount;
	}
	public String getAnchorMode() {
		return AnchorMode;
	}
	public void setAnchorMode(String anchorMode) {
		AnchorMode = anchorMode;
	}
	public String getChangeAddressCount() {
		return changeAddressTxCount;
	}
	public void setChangeAddressCount(String changeAddressCount) {
		this.changeAddressTxCount = changeAddressCount;
	}
}
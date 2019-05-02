/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.db;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * Interface of maria DB mapper 
 * @version 0.8
 * @see org.apache.ibatis.annotations.Mapper
 * 
 * */
@Mapper
public interface MariaMapper
{
	public List<Map<String, Object>> getAnchorList();
	public int insertAnchorInfo(Map<String, Object> paramMap);
	public void deleteAnchorInfo(Map<String, Object> paramMap);
	public List<String> getSeed(Map<String, Object> paramMap);
	public Map<String, Object> getLastHistory();
	public int insertSeedWords(Map<String, Object> paramMap);
	public int updateAnchorInfo(Map<String, Object> paramMap);
	public int getAnchorSeq(Map<String, Object> paramMap);
	public List<Map<String, Object>> getLastCount();
	public Map<String, Object> getVerifyAddress(Map<String, Object> paramMap);
	public List<Map<String, Object>> getTokenName();
}	

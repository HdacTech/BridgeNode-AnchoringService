/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;
import com.hdac.util.StringUtil;

/**
 * Dao class of maria DB
 * @version 0.8
 * @see org.mybatis.spring.SqlSessionTemplate
 *
 */
public class MariaDao {
	
	private SqlSessionTemplate sqlSessionTemplate = MariaConfig.sqlSessionTemplate();
	
	/**
	 * Get all anchoring history from DB
	 * @return anchoring history
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAnchorList() throws Exception{
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		List<Map<String, Object>> result = mapper.getAnchorList();		
		return result;
	}
	
	/**
	 * Insert anchoring history to DB
	 * @param paramMap information map to save
	 * @return result DB insert
	 * @throws Exception
	 */
	public int insertAnchorInfo(Map<String, Object> paramMap) throws Exception{
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		int result = mapper.insertAnchorInfo(paramMap);		
		return result;
	}
	
	/**
	 * Delete specific anchoring history from DB
	 * @param paramMap address_index field
	 * @throws Exception
	 */
	public void deleteAnchorInfo(Map<String, Object> paramMap) throws Exception{
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		mapper.deleteAnchorInfo(paramMap);
	}
	
	/**
	 * Get seed words from DB
	 * @param paramMap empty map
	 * @return seed words list
	 */
	public List<String> getSeed(Map<String, Object> paramMap){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		List<String> result = mapper.getSeed(paramMap);
		return result;
	}
	
	/**
	 * Get last anchoring history from DB
	 * @return last history of anchoring 
	 */
	public Map<String, Object> getLastHistory(){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		Map<String, Object> result = mapper.getLastHistory();
		return result;
	}
	
	/**
	 * Get last two of block count info from DB
	 * @return last two of block count info 
	 */
	public List<Map<String, Object>> getLastCount(){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		List<Map<String, Object>> result = mapper.getLastCount();
		return result;
	}
		
	/**
	 * Insert seeds words to DB
	 * @param paramMap seeds words
	 * @return result DB insert
	 */
	public int insertSeedWords(Map<String, Object> paramMap){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		int result = mapper.insertSeedWords(paramMap);
		return result;
	}
	
	/**
	 * Update anchoring history DB table 
	 * @param paramMap information to update 
	 * @return result DB update
	 */
	public int updateAnchorInfo(Map<String, Object> paramMap){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		int result = mapper.updateAnchorInfo(paramMap);
		return result;
	}
	
	/**
	 * Get seq_val of anchor history DB table
	 * @return seq_val
	 */
	public long getAnchorSeq(){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		mapper.getAnchorSeq(paramMap);
		long result = Long.parseLong(StringUtil.nvl(paramMap.get("seq_val"), "0"));
		return result;
	}
	
	/**
	 * Get anchoring destination address to verify transaction
	 * @param paramMap block count
	 * @return address
	 */
	public Map<String, Object> getVerifyAddress(Map<String, Object> paramMap){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		Map<String, Object> result = mapper.getVerifyAddress(paramMap);
		return result;
	}
	
	/**
	 * Get token information from DB
	 * @return token information
	 */
	public List<Map<String, Object>> getTokenName(){
		MariaMapper mapper = sqlSessionTemplate.getMapper(MariaMapper.class);
		List<Map<String, Object>> result = mapper.getTokenName();
		return result;
	}
}

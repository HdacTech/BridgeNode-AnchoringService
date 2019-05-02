/* 
 * Copyright(c) 2018-2019 hdactech.com
 * Original code was distributed under the MIT software license.
 *
 */

package com.hdac.db;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Configuration class of maria DB
 * @version 0.8
 * @see org.apache.ibatis.io.Resources
 * @see org.mybatis.spring.SqlSessionTemplate
 *
 */
public class MariaConfig
{
	private static SqlSessionFactory sqlSession;
	private static SqlSessionTemplate sqlTemplate;

	static
	{
		String resource = "config/mybatis-config.xml";

		try
		{
			Reader reader = Resources.getResourceAsReader(resource);
			sqlSession = new SqlSessionFactoryBuilder().build(reader);
			sqlTemplate = new SqlSessionTemplate(sqlSession);
			reader.close();
		}
		catch (Exception e)
		{
			System.out.println("SqlMapConfig error : " + e);
		}
	}
	
	/**
	 * Get sql session factory of DB connect
	 * @return sql session
	 */
	public static SqlSessionFactory getSqlSessionFactory()
	{
		return sqlSession;
	}
	
	/**
	 * Get sql session template from sql session
	 * @return sql session template
	 */
	public static SqlSessionTemplate sqlSessionTemplate()
	{
		return sqlTemplate;
	}
	
}
package com.haoyong.salel.util;

import net.sf.mily.support.database.DatabaseInfo;
import net.sf.mily.support.database.DatabaseInfoReader;
import net.sf.mily.support.database.SchemaTool;
import net.sf.mily.support.throwable.LogicException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.dao.SLoginSessionProvider;
import com.haoyong.salel.common.dao.TransactionManager;

public class SchemaToolUtil {
	
	/**
	 * 给ant调用
	 */
	public static void main(String a[]){
		new SchemaToolUtil().testSchemaUpdate();
	}
	
	/**
	 * 更新<业务数据库>和<历史备份数据库>
	 * 如果更新不成功:提示(在database.cfg.xml屏蔽掉 datasource节点或设置为null)
	 */
	@Test
	public void testSchemaUpdate() {
		try {
			DatabaseInfo databaseInfo = DatabaseInfoReader.getReader().getDatabaseInfo();
			if (!StringUtils.isEmpty(databaseInfo.getLogin())) {
				databaseUpdate();/* 更新业务数据库 */
			} else {
				throw new LogicException(53, "没有当前业务数据库");
			}
		} catch (Exception e) {
			throw new RuntimeException("更新不成功！", e);
		}
	}
	
	/**
	 * 收缩文件
	 * @param dbname
	 * @param sizeM
	 */
	private void shrinkFile(String dbname,String filename,int sizeM){
		StringBuffer sql=new StringBuffer();
		sql.append("use ").append(dbname).append(";GO")
		.append("ALTER DATABASE ").append(dbname).append(" SET RECOVERY SIMPLE  ;GO") 		/*--简单模式*/
		.append("DBCC SHRINKFILE (N'").append(filename).append("',1)  ;GO") 		/*日志文件缩减为500m*/
		.append("ALTER DATABASE ").append(dbname).append(" SET RECOVERY FULL   ;") ; 	/*完全模式*/
		BaseDAO dao = new BaseDAO();
		try {
			TransactionManager.begin();
			dao.getSQLQuery(sql.toString()).executeUpdate();
			TransactionManager.commit();
		} catch(Exception e) {
			e.printStackTrace();
			TransactionManager.rollback();
		}
	}
	/**
	 * 更新当前业务数据库 JUnit Test
	 */
	private void databaseUpdate() throws Exception {
		SLoginSessionProvider cooperSessionProvider= new SLoginSessionProvider();
		Configuration cfg = cooperSessionProvider.getHbmConfiguration();
		SchemaTool schemaTool = new SchemaTool(cfg);
		schemaTool.update();
	}
}

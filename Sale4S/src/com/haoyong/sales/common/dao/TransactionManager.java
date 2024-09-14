package com.haoyong.sales.common.dao;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;

/**
 * <pre>
 * Title: 事务管理器
 * </pre>
 */
public class TransactionManager {

	/**
	 * 用于存放 事务的 ThreadLocal
	 */
	private static final ThreadLocal<Transaction> tlTransaction = new ThreadLocal<Transaction>();
	
	/**
	 * 是否提交到数据库
	 */
	public static boolean isRealCommit() {
		// false不提交事务, true提交事务
		return 1==1;
	}

	/**
	 * 开启当前线程相关的事务
	 */
	public static void begin() {
		TraceItem traceItem = getTrace().onceStart("begin");
		Transaction tx = null;
		Session session = BaseDAO.getSessionInThread();
		session.clear();
		tx = session.getTransaction();
		tx.begin();
		Assert.assertTrue("要开启二级事务？", tlTransaction.get()==null);
		tlTransaction.set(tx);
		traceItem.onceEnd();
	}

	/**
	 * 提交当前线程相关的事务
	 * 
	 */
	public static void commit() {
		TraceItem traceItem = getTrace().onceStart("changelog");
		if (isRealCommit()==false) { // false不提交事务
			rollback();
			return;
		}
		traceItem.onceEnd();
		traceItem = getTrace().onceStart("commit");
		Transaction tx = tlTransaction.get();
		Assert.assertTrue("被内嵌二级事务关闭了？", tx!=null);
		try {
			tx.commit();
		} catch (Exception re) { // 真正提交数据库事务时出错
			BaseDAO.getDomainChangeTracing().rollback();
			if ((tx != null) && (tx.isActive())) {
				tx.rollback();
			}
			tlTransaction.set(null);
			throw LogUtil.getRuntimeException(re);
		} finally {
			tlTransaction.remove();
			traceItem.onceEnd();
			new SessionProvider().clear();
//			LogUtil.error(new StringBuffer("\nTransactionManager.commit").toString());
		}
	}

	/**
	 * 回滚
	 * 
	 */
	public static void rollback() {
		TraceItem traceItem = getTrace().onceStart("rollback");
		Transaction tx = tlTransaction.get();
		if ((tx != null) && (tx.isActive())) {
			tx.rollback();
		}
		tlTransaction.remove();
		BaseDAO.getDomainChangeTracing().rollback();
		BaseDAO.close();
		traceItem.onceEnd();
	}

	public static boolean isOpenTransaction() {
		return tlTransaction.get() != null;
	}

	private static TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(TransactionManager.class.getSimpleName());
	}
}

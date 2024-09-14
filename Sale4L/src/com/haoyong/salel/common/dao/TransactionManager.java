package com.haoyong.salel.common.dao;

import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;

import org.hibernate.Session;
import org.hibernate.Transaction;

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
		return 1==1; // false不提交事务, true提交事务
	}

	/**
	 * 开启当前线程相关的事务
	 * 
	 * @return 是否开启了一个全新事务， 如果只是进入嵌套事务， 返回否。
	 */
	public static void begin() {
		TraceItem traceItem = getTrace().onceStart("begin");
		Session session = BaseDAO.getSessionInThread();
		session.clear();
		Transaction tx = session.getTransaction();
		tx.begin();
		tlTransaction.set(tx);
		traceItem.onceEnd();
	}

	/**
	 * 提交当前线程相关的事务
	 * 
	 */
	public static void commit() {
		BaseDAO dao = new BaseDAO();
		TraceItem traceItem = getTrace().onceStart("changelog");
		if (isRealCommit()==false) { // false不提交事务
			rollback();
			return;
		}
		dao.getDomainChangeTracing().changelog();
		traceItem.onceEnd();
		traceItem = getTrace().onceStart("commit");
		Transaction tx = tlTransaction.get();
		try {
			tx.commit();
			Session session = BaseDAO.getSessionInThread();
			session.clear();
		} catch (RuntimeException re) { // 真正提交数据库事务时出错
			dao.getDomainChangeTracing().rollback();
			if ((tx != null) && (tx.isActive())) {
				tx.rollback();
			}
			tlTransaction.set(null);
			throw re;
		} finally {
			tlTransaction.remove();
			traceItem.onceEnd();
			dao.close();
		}
	}

	/**
	 * 回滚
	 * 
	 */
	public static void rollback() {
		BaseDAO dao = new BaseDAO();
		TraceItem traceItem = getTrace().onceStart("rollback");
		Transaction tx = tlTransaction.get();
		Session session = BaseDAO.getSessionInThread();
		session.clear();
		if ((tx != null) && (tx.isActive())) {
			tx.rollback();
		}
		tlTransaction.remove();
		dao.getDomainChangeTracing().rollback();
		dao.close();
		traceItem.onceEnd();
	}

	public static boolean isOpenTransaction() {
		return tlTransaction.get() != null;
	}

	private static TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(TransactionManager.class.getSimpleName());
	}
}

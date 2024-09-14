package com.haoyong.salel.common.dao;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.NoResultException;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;

import com.haoyong.salel.common.domain.AbstractDomain;

/**
 * 所有DAO的父类 管理与数据库的所有操作
 */
public class BaseDAO {
	
	private static ThreadLocal<DomainChangeTracing> transactionDomainList=new ThreadLocal<DomainChangeTracing>();
	
	/**
	 * 获得线程相关的 EntityManager
	 * @return
	 */
	public static Session getSessionInThread() {
		return SessionProvider.getSessionInThread();
	}

	private Session getSession() {
		return getSessionInThread();
	}

	/**
	 * <pre>
	 * 让 JPA 上下文管理 <code> t </code> 的属性修改。 
	 * 这个方法是模拟 Hibernate 的同名方法的行为。
	 * <OL>根据传入的 Abstract 或子类 对象 <code>t</code> 的不同状态， 调用 {@link Session} 的不同方法：
	 * <li>当 t 的 id 大于0， 认为它已经被持久化过， 会调用 {@link Session#merge(Object)}， 
	 * 并返回一个托管的 t 对象的拷贝， 原 t 对象的托管状态不变。 当JPA 事务提交时， 
	 * t 对象的修改内容将通过托管的拷贝对象反应出来并由JPA负责持久化到数据库。</li>
	 * <li>当 t 的 id 小于等于0， 认为它未被持久化过， 会调用{@link Session#persist(Object)}，
	 * 该方法会把 t 直接变成托管对象， 方法的返回值 和 传入的 t 是指向相同的对象。 当JPA事务提交时， 
	 * JPA负责把 t 的信息新增到数据库中。</li>
	 * </OL>
	 * @param t
	 * </pre>
	 */
	public <T extends AbstractDomain> T saveOrUpdate(T t) {
		TraceItem traceItem = getTrace().onceStart("saveOrUpdate");
		try {
			t.setModifytime(new Date());
			if (t.getId()==0) {
				getSession().save(t);
				this.getDomainChangeTracing().insert(t);
			} else {
				getSession().update(t);
				this.getDomainChangeTracing().update(t);
			}
			return t;
		} catch(Exception e) {
			throw new LogicException(4, t.getClass().getSimpleName(), LogUtil.getCauseMessage(e));
		} finally {
			traceItem.onceEnd();
		}
	}

	public void close() {
		if(transactionDomainList.get()!=null){
			DomainChangeTracing tDomainList=transactionDomainList.get();
			tDomainList.close();
			transactionDomainList.remove();
		}
	}
	
	public void refresh(Object o){
		if (o == null)			return;
		TraceItem traceItem = getTrace().onceStart("refresh");
		getSession().refresh(o);
		traceItem.onceEnd();
	}

	/**
	 * 把 p 从“托管”状态变成“脱管”状态， 就是移出JPA上下文。
	 * @param p
	 */
	public void detach(Object p) {
		TraceItem traceItem = getTrace().onceStart("detach");
		getSession().evict(p);
		traceItem.onceEnd();
	}
	
	/**
	 * 删除 p
	 * @param p
	 * @return
	 */
	public <T extends AbstractDomain> void remove(T p) {
		TraceItem traceItem = getTrace().onceStart("remove");
		getSession().delete(p);
		this.getDomainChangeTracing().delete(p);
		traceItem.onceEnd();
	}

	/**
	 * 装载 唯一标识为 id 的 tClass 实例
	 * @param tClass
	 * @param id
	 * @return
	 */
	public <T extends AbstractDomain> T load(T d) {
		TraceItem traceItem = getTrace().onceStart("load");
		T t = (T)this.load(d.getClass(), d.getId());
		traceItem.onceEnd();
		return t;
	}
	public <T extends AbstractDomain> T load(Class<T> tClass, long id) {
		TraceItem traceItem = getTrace().onceStart("load");
		try {
			String tableName = getClassTableName(tClass);
			String hql = new StringBuffer("select l.* from ").append(tableName).append(" l where l.id=?").toString();
			traceItem.appendOper(hql);
			SQLQuery query = getSQLQuery(hql, tClass, id);
			query.setFirstResult(0).setMaxResults(2);
			T t = (T)query.uniqueResult();
			return t;
		} finally {
			traceItem.onceEnd();
		}
	}
	
	/**
	 * 获得查询结果集
	 * 
	 * @param <T>
	 * @param sql
	 *      参数通过 ?1 到 ?9 的方式传入， 例如“select clazz from MaterialType clazz
	 *      where clazz.name = ?”
	 * @param params
	 *      按声明顺序的参数列表， 可不传
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> List<T> nativeQuery(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuery");
		Query q = getSQLQuery(sql, clazz, params);
		List list = q.list();
		traceItem.onceEnd();
		return list;
	}

	/**
	 * 获得查询结果集////////////////////////////////////////////////////////////////////////////////////////////
	 * 
	 * @param <T>
	 * @param sql
	 *      参数通过 ? 的方式传入， 例如“select t.* from b_MaterialType t
	 *      where t.name = ?”
	 * @param params
	 *      按声明顺序的参数列表， 可不传
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> nativeSqlQuery(String sql, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuery");
		try{
			Query q = getSQLQuery(sql, params);
			List list = q.list();
			return list;
		} finally{
			traceItem.onceEnd();
		}
	}

	/**
	 * <pre>
	 * 获得查询的单一结果
	 * 如果一条记录都没有， 就返回 null， 其它行为类似 {@link Query#getSingleResult()}。
	 * @param <T>
	 * @param sql 参数通过 ? 的方式传入， 例如“select t.* from t_MaterialType t
	 *      where t.name = ?”
	 * @param params 按声明顺序的参数列表， 可不传
	 * @return
	 * </pre>
	 * 
	 * @see Query#getSingleResult()
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQuerySingleResult(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuerySingleResult");
		try{
			SQLQuery q = getSQLQuery(sql, clazz, params);
			T result = null;
			try {
				q.setFirstResult(0).setMaxResults(2);
				result = (T) q.uniqueResult();
			} catch (NoResultException nre) {
				// 没有记录就返回null
			}
			return result;
		}finally{
			traceItem.onceEnd();
		}
	}

	/**
	 * <pre>
	 * 获得查询的第一个结果
	 * 如果一条记录都没有， 就返回 null， 其它行为类似 {@link Query#getSingleResult()}。
	 * @param <T>
	 * @param sql 参数通过 ? 的方式传入， 例如“select t.* from t_MaterialType t
	 *      where t.name = ?”
	 * @param params 按声明顺序的参数列表， 可不传
	 * @return
	 * </pre>
	 * 
	 * @see Query#getSingleResult()
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQueryFirstResult(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQueryFirstResult");
		try{
			SQLQuery q = getSQLQuery(sql, clazz, params);
			T result = null;
			try {
				q.setFirstResult(0).setMaxResults(1);
				result = (T) q.uniqueResult();
			} catch (NoResultException nre) {
				// 没有记录就返回null
			}
			return result;
		}finally{
			traceItem.onceEnd();
		}
	}

	/**
	 * SQL查询
	 * 
	 * @param sql
	 * @return
	 */
	public SQLQuery getSQLQuery(String sql0, Object...params) {
		String sql = this.getParametedSql(sql0, params);
		SQLQuery q = this.getSession().createSQLQuery(sql);
		return q;
	}

	/**
	 * SQL查询
	 */
	private SQLQuery getSQLQuery(String sql, Class<?> clazz, Object...params) {
		SQLQuery query = this.getSQLQuery(sql, params);
		query.addEntity(clazz);
		return query;
	}
	
	public String getParametedSql(String sql, Object... params) {
		Matcher m = Pattern.compile("(\\s*)[(\\=)|(\\>)|(\\<)|(\\>=)|(\\<=)]?(\\s*)(\\?)").matcher(sql);
		StringBuffer sb = new StringBuffer();
		for (int si=0, pi=0, slen=sql.length(); si<slen; si++) {
			if (m.find()) {
				String sitem = sql.substring(m.start(), m.end());
				Object param = params[pi++];
				sb.append(sql.substring(si, m.start()));
				if (param != null) {
					sb.append(sitem.replaceFirst("\\?", TypeFactory.createType(param.getClass()).sqlFormat(param)));
				} else {
					sb.append(" is null");
				}
				si = m.end()-1;
			} else {
				sb.append(sql.substring(si, sql.length()));
				si = sql.length();
			}
		}
		return sb.toString();
	}
	
	public <T extends AbstractDomain> String getClassTableName(Class<T>clazz) {
		SessionFactory sessionFactory=SessionProvider.getSessionFactory();
		AbstractEntityPersister classMetadata = (AbstractEntityPersister)sessionFactory.getClassMetadata(clazz);
		return classMetadata.getTableName();
	}
	
	private TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(this);
	}
	
	public DomainChangeTracing getDomainChangeTracing(){
		if(transactionDomainList.get()==null){
			DomainChangeTracing newTransactionDomain=new DomainChangeTracing();
			transactionDomainList.set(newTransactionDomain);
		}
		return transactionDomainList.get();
	}
}

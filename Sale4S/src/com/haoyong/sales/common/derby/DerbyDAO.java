package com.haoyong.sales.common.derby;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.NoResultException;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;
import net.sf.mily.webObject.query.Identity;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;

import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.common.dao.DomainChangeTracing;

/**
 * 所有DAO的父类 管理与数据库的所有操作
 */
public class DerbyDAO {
	
	private long sellerId = 0;
	
	private static ThreadLocal<Session> localSession=new ThreadLocal<Session>();
	
	public DerbyDAO() {
		this.sellerId = SellerLogic.getSellerId();
	}
	
	public DerbyDAO(long sellerId) {
		this.sellerId = sellerId;
	}

	public Session getSession() {
		Session session=localSession.get();
		if(session==null || !session.isOpen()){
			session = new DerbyUpdateListener().getSessionFactory().openSession();
			localSession.set(session);
		}
		return localSession.get();
	}

	/**
	 * 保存
	 */
	public <T extends Identity> T saveOrUpdate(T t) {
		StringBuffer sb = new StringBuffer().append("Derby\n").append(t.getId()==0? "新增": "更新").append("！");
		boolean ok = true;
		try {
			if (t instanceof AbstractDerby) {
				AbstractDerby d = (AbstractDerby)t;
				d.setSellerId(sellerId);
				d.setModifytime(new Date());
			}
			if (t.getId()==0) {
				getSession().save(t);
			} else {
				getSession().update(t);
			}
			return t;
		} catch(Exception e) {
			ok = false;
			throw new LogicException(4, t.getClass().getSimpleName(), LogUtil.getCauseMessage(e));
		} finally {
			sb.append(DomainChangeTracing.printDomains(null, t));
			if (ok)
				DomainChangeTracing.getLog().info(sb.toString());
			else
				LogUtil.error(sb.toString());
		}
	}

	public static void close() {
		Session s = null;
		if ((s=localSession.get())!=null && s.isOpen())
			s.clear();
		localSession.remove();
	}

	/**
	 * 删除
	 */
	public <T extends Identity> void remove(T p) {
		TraceItem traceItem = getTrace().onceStart("remove");
		this.getSession().delete(p);
		p.setId(p.getId()*-1);
		traceItem.onceEnd();
		StringBuffer sb = new StringBuffer().append("Derby\n删除").append("！");
		sb.append(DomainChangeTracing.printDomains(null, p));
		DomainChangeTracing.getLog().info(sb.toString());
	}

	/**
	 * 装载
	 */
	public <T extends AbstractDerby> T load(T d) {
		TraceItem traceItem = getTrace().onceStart("load");
		T t = (T)this.load(d.getClass(), d.getId());
		traceItem.onceEnd();
		return t;
	}
	
	public <T extends AbstractDerby> T load(Class<T> tClass, long id) {
		TraceItem traceItem = getTrace().onceStart("load");
		try {
			String tableName = getClassTableName(tClass);
			String hql = new StringBuffer("select l.* from ").append(tableName).append(" l where l.id=? and l.sellerId=?").toString();
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
		List list = null;
		try {
		list = q.list();
		}catch(Exception e) {
			LogUtil.error(sql, e);
			throw LogUtil.getRuntimeException(e);
		}
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
	public <T extends AbstractDerby> T nativeQuerySingleResult(String sql, Class<T> clazz, Object... params) {
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
	public <T extends AbstractDerby> T nativeQueryFirstResult(String sql, Class<T> clazz, Object... params) {
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
	 */
	public SQLQuery getSQLQuery(String sql0, Object... params) {
		String sql = getParametedSql(sql0, params);
		SQLQuery q = getSession().createSQLQuery(sql);
		return q;
	}

	/**
	 * SQL查询
	 */
	private SQLQuery getSQLQuery(String sql, Class<?> clazz, Object... params) {
		SQLQuery query = getSQLQuery(sql, params);
		query.addEntity(clazz);
		return query;
	}
	
	public String getParametedSql(String sql, Object... params0) {
		Matcher m = Pattern.compile("(\\s*)[(\\=)|(\\>)|(\\<)|(\\>=)|(\\<=)]?(\\s*)(\\?)").matcher(sql);
		StringBuffer sb = new StringBuffer();
		Object[] params = new Object[params0.length+1];
		for (int i=params0.length; i-->0;) {
			params[i] = params0[i];
		}
		params[params0.length] = sellerId;
		for (int msize=0, si=0, pi=0, slen=sql.length(); si<slen; si++) {
			if (m.find()) {
				msize++;
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
				si = sql.length()-1;
			}
			if (si==sql.length()-1 && msize<params.length)
				throw new LogicException(2, "查询Sql?个数不够放置参数个数");
		}
		LogUtil.info(sb.toString());
		return sb.toString();
	}
	
	public <T extends AbstractDerby> String getClassTableName(Class<T>clazz) {
		SessionFactory sessionFactory=new DerbyUpdateListener().getSessionFactory();
		AbstractEntityPersister classMetadata = (AbstractEntityPersister)sessionFactory.getClassMetadata(clazz);
		return classMetadata==null? null: classMetadata.getTableName();
	}
	
	private TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(this);
	}
}

package com.haoyong.sales.common.dao;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.NoResultException;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.mysql.jdbc.Field;

/**
 * 所有DAO的父类 管理与数据库的所有操作
 */
public class BaseDAO {
	
	private static ThreadLocal<DomainChangeTracing> thTraceing=new ThreadLocal<DomainChangeTracing>();
	private long sellerId = 0;
	
	public BaseDAO() {
		this.sellerId = SellerLogic.getSellerId();
	}
	public BaseDAO(long sid) {
		this.sellerId = sid;
	}
	
	/**
	 * 获得线程相关的 EntityManager
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
	 * </pre>
	 */
	public <T extends AbstractDomain> T saveOrUpdate(T t) {
		TraceItem traceItem = getTrace().onceStart("saveOrUpdate");
		try {
			t.setSellerId(sellerId);
			t.setModifytime(new Date());
			if ("连续保存".length()>0) {
				String key = "BaseDAOSaveList";
				List<Object> values = (List<Object>)WindowMonitor.getMonitor().getAttribute(key);
				if (values==null) {
					values = new ArrayList<Object>();
					WindowMonitor.getMonitor().addAttribute(key, values);
				} else {
					Object prev = values.get(values.size()-1);
					if (prev.getClass()!=t.getClass())
						values.clear();
				}
				if (values.size()==20) {
					values.clear();
					if (t.getClass()==OrderTicket.class)
						"".toCharArray();
					LogUtil.error(new StringBuffer().append("连续保存20个").append(t.getClass().getSimpleName()).toString());
				}
				values.add(t);
			}
			if (t.getId()==0) {
				t.getSnapShot();
				getSession().save(t);
				this.getDomainChangeTracing().insert(t);
			} else {
				getSession().update(t);
				this.getDomainChangeTracing().update(t);
			}
			return t;
		} catch(Exception e) {
			throw new LogicException(4, e);
		} finally {
			traceItem.onceEnd();
		}
	}

	public static void close() {
		if(thTraceing.get()!=null){
			DomainChangeTracing tDomainList=thTraceing.get();
			tDomainList.close();
			thTraceing.remove();
		}
	}

	/**
	 * 删除 p
	 */
	public <T extends AbstractDomain> void remove(T p) {
		TraceItem traceItem = getTrace().onceStart("remove");
		getSession().delete(p);
		p.setId(p.getId()*-1);
		this.getDomainChangeTracing().delete(p);
		traceItem.onceEnd();
	}

	/**
	 * 装载 唯一标识为 id 的 tClass 实例
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
	 * @param sql
	 *      参数通过 ?1 到 ?9 的方式传入， 例如“select clazz from MaterialType clazz
	 *      where clazz.name = ?”
	 *      按声明顺序的参数列表， 可不传
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> List<T> nativeQuery(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuery");
		Query q = this.getSQLQuery(sql, clazz, params);
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
	
	public <T extends AbstractDomain> List<T> nativeSqlQuery(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuery");
		List<String> slist = this.getQueryGson(sql, params);
		List<T> list = new ArrayList<T>(slist.size());
		Gson gson = new Gson();
		for (String s: slist) {
			T t = gson.fromJson(s, clazz);
			list.add(t);
		}
		traceItem.onceEnd();
		return list;
	}

	/**
	 * 获得查询结果集////////////////////////////////////////////////////////////////////////////////////////////
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
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQuerySingleResult(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuerySingleResult");
		try{
			SQLQuery q = this.getSQLQuery(sql, clazz, params);
			T result = null;
			try {
				q.setFirstResult(0).setMaxResults(2);
				result = (T) q.uniqueResult();
			} catch (NoResultException nre) {
				// 没有记录就返回null
			} catch (Exception nre) {
				LogUtil.error(new StringBuffer().append(sql).append(" ").append(Arrays.toString(params)).toString());
				throw LogUtil.getRuntimeException(nre);
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
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQueryFirstResult(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQueryFirstResult");
		try{
			SQLQuery q = this.getSQLQuery(sql, clazz, params);
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
		String sql = this.getParametedSql(sql0, params);
		SQLQuery q = this.getSession().createSQLQuery(sql);
		return q;
	}

	/**
	 * SQL查询
	 */
	private SQLQuery getSQLQuery(String sql, Class<?> clazz, Object... params) {
		SQLQuery query = this.getSQLQuery(sql, params);
		query.addEntity(clazz);
		return query;
	}
	
	public String getParametedSql(String sql, Object... params0) {
		Matcher m = Pattern.compile("(\\s*)[(\\=)|(\\>)|(\\<)|(\\>=)|(\\<=)]?(\\s*)(\\?)").matcher(sql);
		StringBuffer sb = new StringBuffer();
		Object[] params = params0;
		if (this.sellerId>0) {
			params = new Object[params0.length+1];
			for (int i=params0.length; i-->0; params[i] = params0[i]);
			params[params0.length] = sellerId;
		}
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
	private List<String> getQueryGson(String sql, Object... params){
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<String> list = new ArrayList<String>();
		try {
			sql = this.getParametedSql(sql, params);
			LogUtil.info(sql);
			ps = this.getSession().connection().prepareStatement(sql);
			resultSet = ps.executeQuery();
			Field[] columns = (Field[])ReflectHelper.getPropertyValue(resultSet, "fields");
			while(resultSet.next()){
				int icol=0;
				Map<String, Object> row = new HashMap<String, Object>();
				for (Field column: columns) {
					icol++;
					Object d = resultSet.getObject(icol), fd=d;
					if (d != null) {
						if (d instanceof Date)		fd = null;
						row.put(column.getName(), fd);
					}
				}
				Gson gson = new Gson();
				Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
				String sgson = gson.toJson(row);
				list.add(sgson);
			}
		} catch (Exception e) {
			throw LogUtil.getRuntimeException("查询失败",e);
		} finally {
			try {
				if (resultSet!=null)	resultSet.close();
			} catch (Exception e) {
				//
			}
			try {
				if (ps!=null) 			ps.close();
			} catch (Exception e) {
				//
			}
		}
		return list;
	}
	
	public <T extends AbstractDomain> String getClassTableName(Class<T>clazz) {
		SessionFactory sessionFactory=SessionProvider.getSessionFactory();
		AbstractEntityPersister classMetadata = (AbstractEntityPersister)sessionFactory.getClassMetadata(clazz);
		return classMetadata==null? null: classMetadata.getTableName();
	}
	
	private TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(this);
	}
	
	public static DomainChangeTracing getDomainChangeTracing() {
		if(thTraceing.get()==null){
			DomainChangeTracing newTransactionDomain=new DomainChangeTracing();
			thTraceing.set(newTransactionDomain);
		}
		return thTraceing.get();
	}
}

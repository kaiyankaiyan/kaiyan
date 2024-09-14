package com.haoyong.salel.common.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;
import net.sf.mily.util.ReflectHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.common.domain.AbstractDomain;
import com.mysql.jdbc.Field;

/**
 * 所有DAO的父类 管理与数据库的所有操作
 */
public class LinkSellerDAO {
	
	private Seller seller;

	public LinkSellerDAO() {
		this.seller = (Seller)WindowMonitor.getMonitor().getAttribute("seller");
	}

	/**
	 * 获得查询结果集
	 * 
	 * @param <T>
	 * @param sql
	 * @param params
	 *      按声明顺序的参数列表， 可不传
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> List<T> nativeQuery(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuery");
		List<String> slist = queryGson(sql, params);
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
	 * 获得查询的单一结果
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQuerySingleResult(String sql, Class<T> clazz, Object... params) {
		List<T> list = nativeQuery(sql, clazz, params);
		if (list.size()==1) {
			return list.get(0);
		} else if (list.size()==0) {
			return null;
		} else {
			throw new LogicException(2, "获得查询的单一记录失败");
		}
	}

	/**
	 * 获得查询的第一个结果
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQueryFirstResult(String sql, Class<T> clazz, Object... params) {
		List<T> list = nativeQuery(sql, clazz, params);
		if (list.size()>0) {
			return list.get(0);
		} else if (list.size()==0) {
			return null;
		} else {
			throw new LogicException(2, "获得查询的第一个记录失败");
		}
	}
	
	private Connection getConnection() {
		String name = "proxool.".concat(this.seller.getSqlName());
		Connection conn = new SessionProvider().getConnection(name, new String[]{name});
		return conn;
	}
	
	private List<String> queryGson(String sql, Object... params){
		Object[] paramList = new Object[params.length+1];
		int pi = 0;
		for (Object o: params) {
			paramList[pi++] = o;
		}
		paramList[pi] = seller.getId();
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<String> list = new ArrayList<String>();
		try {
			sql = new BaseDAO().getParametedSql(sql, paramList);
			LogUtil.info(sql);
			ps = this.getConnection().prepareStatement(sql);
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
	
	private TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(this);
	}
}

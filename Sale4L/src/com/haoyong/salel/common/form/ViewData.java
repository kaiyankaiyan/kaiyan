package com.haoyong.salel.common.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.common.domain.AbstractDomain;

/**
 * 页面数据包装类，作为传给service的统一参数，不持久化
 */
public class ViewData<T extends AbstractDomain> implements Serializable {

	private static final long serialVersionUID = -5365245893549396759L;
	
	private List<T> ticketDetails;

	/**
	 * 业务处理参数（非业务处理服务切莫修改）
	 */
	private final Map<String, Object> serviceParam = new HashMap<String, Object>();
	
	/**
	 * 当前操作用户
	 */
	private User currentUser;
	
	
	/**
	 * @return the serviceParam
	 */
	public Map<String, Object> getServiceParam() {
		return serviceParam;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public T getTicketFirst() {
		return ticketDetails.get(0);
	}

	public List<T> getTicketDetails() {
		return ticketDetails;
	}
	
	public void setTicketDetails(T... detailList) {
		List<T> list = new ArrayList<T>();
		for (T item: detailList) {
			list.add(item);
		}
		this.ticketDetails = list;
	}

	public void setTicketDetails(List<T> selectedList) {
		this.ticketDetails = selectedList;
	}

	public void setParam(String key, Object value) {
		this.serviceParam.put(key, value);
	}

	public Object getParam(String key) {
		return this.serviceParam.get(key);
	}
	
	public <F> void setParam(F value) {
		if (value!=null)
			this.setParam(value.getClass().getSimpleName(), value);
	}
	
	public <F> F getParam(Class<F> clss) {
		return (F)this.getParam(clss.getSimpleName());
	}
}

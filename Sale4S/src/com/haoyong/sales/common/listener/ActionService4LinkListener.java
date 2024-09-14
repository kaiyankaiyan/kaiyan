package com.haoyong.sales.common.listener;

import java.util.HashMap;
import java.util.Map;

import net.sf.mily.ui.WindowMonitor;

import com.haoyong.sales.base.domain.Seller;

/**
 * Link其他商家的,业务服务环境
 */
public class ActionService4LinkListener {
	
	private Map<String, Object> fromAttributes=new HashMap<String, Object>(), onceAttributes=new HashMap<String, Object>();
	
	public void actionBefore() {
		Seller fromSeller=(Seller)WindowMonitor.getMonitor().getAttribute("seller");
		this.getFromAttributes().clear();
		for (String key: this.getOnceAttributes().keySet()) {
			this.getFromAttributes().put(key, WindowMonitor.getMonitor().getAttribute(key));
		}
		for (Map.Entry<String, Object> entry: this.getOnceAttributes().entrySet()) {
			if (entry.getValue()!=null)
				WindowMonitor.getMonitor().addAttribute(entry.getKey(), entry.getValue());
		}
		Seller onceSeller=(Seller)WindowMonitor.getMonitor().getAttribute("seller");
	}

	public void actionAfter(Boolean commit_rollback) {
		Seller fromSeller=(Seller)WindowMonitor.getMonitor().getAttribute("seller");
		for (String key: this.getOnceAttributes().keySet()) {
			Object value = this.getFromAttributes().get(key);
			if (value!=null)
				WindowMonitor.getMonitor().addAttribute(key, value);
			else
				WindowMonitor.getMonitor().removeAttribute(key);
				
		}
	}
	
	private Map<String, Object> getFromAttributes() {
		return this.fromAttributes;
	}
	
	public Map<String, Object> getOnceAttributes() {
		return this.onceAttributes;
	}
	
	public long getToSellerId() {
		Seller to = (Seller)this.onceAttributes.get("seller");
		return to.getId();
	}
}
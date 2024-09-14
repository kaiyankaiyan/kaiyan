package com.haoyong.sales.base.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class SubmitTypeLogic {

	public List<String> getTypeList() {
		List<String> list = new ArrayList<String>();
		list.add("客户订单");
		list.add("分公司订单");
		return list;
	}
	
	public boolean isClientType(String type) {
		return StringUtils.equals(type, "客户订单");
	}
	
	public boolean isSubCompanyType(String type) {
		return StringUtils.equals(type, "分公司订单");
	}
}

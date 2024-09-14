package com.haoyong.sales.sale.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.TypeInfosLogic;
import com.haoyong.sales.sale.domain.OrderType;

/**
 * 订单种类列举 Logic
 */
public class OrderTypeLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(OrderType.class);
		Type gtype = new TypeToken<ArrayList<OrderType>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<OrderType> getTypeList() {
		TypeInfos infos = getDomain();
		List<OrderType> types = infos.getInfoList();
		if (types.isEmpty()) {
			String[] slist = new String[]{"客户订单", "备货订单", "销售单", "原物料订单"};
			int id=10;
			for (String s: slist) {
				OrderType t = new OrderType();
				t.setId(++id);
				t.setName(s);
				types.add(t);
			}
		}
		return types;
	}
	
	public String getBackType() {
		return "备货订单";
	}
	
	public String getClientType() {
		return "客户订单";
	}
	
	public String getSaleType() {
		return "销售单";
	}

	public String getBomType() {
		return "原物料订单";
	}
	
	public boolean isBackType(String stype) {
		return stype!=null && stype.indexOf(getBackType())>-1;
	}
	
	public boolean isClientType(String stype) {
		return stype!=null && stype.indexOf(getClientType())>-1;
	}
	
	public boolean isBomType(String stype) {
		return stype!=null && stype.indexOf(getBomType())>-1;
	}
}

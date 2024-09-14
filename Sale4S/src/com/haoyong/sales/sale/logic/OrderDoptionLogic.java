package com.haoyong.sales.sale.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.TypeInfosLogic;
import com.haoyong.sales.sale.domain.OrderDoption;

/**
 * 订单明细选项列举 Logic
 */
public class OrderDoptionLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(OrderDoption.class);
		Type gtype = new TypeToken<ArrayList<OrderDoption>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<OrderDoption> getTypeList() {
		TypeInfos infos = getDomain();
		List<OrderDoption> types = infos.getInfoList();
		if (types.isEmpty()) {
			String[] slist = new String[]{"已付款", "赠送"};
			int id=10;
			for (String s: slist) {
				OrderDoption t = new OrderDoption();
				t.setId(++id);
				t.setName(s);
				types.add(t);
			}
		}
		return types;
	}
	
	public String getPaid() {
		return "已付款";
	}
	
	public String getPresent() {
		return "赠送";
	}
	
	public boolean isPaid(String stype) {
		return stype!=null && stype.indexOf("已付款")>-1;
	}
	
	public boolean isPresent(String stype) {
		return stype!=null && stype.indexOf("赠送")>-1;
	}
}

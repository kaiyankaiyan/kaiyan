package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.domain.TypeInfos;

/**
 * 商品供货方式
 */
public class SupplyTypeLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(SupplyType.class);
		Type gtype = new TypeToken<ArrayList<SupplyType>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<SupplyType> getTypeList() {
		TypeInfos infos = getDomain();
		List<SupplyType> list = infos.getInfoList();
		if (list.isEmpty()) {
			String[] slist = new String[]{"采购", "生产", "甲供"};
			int id=0;
			for (String s: slist) {
				SupplyType t = new SupplyType();
				t.setId(++id);
				t.setName(s);
				list.add(t);
			}
		}
		return list;
	}
	
	public boolean isPurchaseType(String type) {
		return getPurchaseType().equals(type);
	}
	
	public boolean isProductType(String type) {
		return getProductType().equals(type);
	}
	
	public String getPurchaseType() {
		return "采购";
	}
	
	public String getProductType() {
		return "生产";
	}
	
	public String getClientType() {
		return "甲供";
	}
}

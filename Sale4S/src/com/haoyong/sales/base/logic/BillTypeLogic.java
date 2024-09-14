package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.BillType;
import com.haoyong.sales.base.domain.TypeInfos;

/**
 * 商品供货方式
 */
public class BillTypeLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(BillType.class);
		Type gtype = new TypeToken<ArrayList<BillType>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<BillType> getTypeList() {
		TypeInfos infos = getDomain();
		List<BillType> list = infos.getInfoList();
		if (list.isEmpty()) {
			String[] slist = new String[]{"销售出货", "采购入库"};
			int id=0;
			for (String s: slist) {
				BillType t = new BillType();
				t.setId(++id);
				t.setName(s);
				list.add(t);
			}
		}
		return list;
	}
	
	public String getSaleType() {
		return "销售出货";
	}
	
	public String getPurchaseType() {
		return "采购入库";
	}
}

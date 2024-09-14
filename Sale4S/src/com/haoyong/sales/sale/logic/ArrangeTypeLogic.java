package com.haoyong.sales.sale.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.TypeInfosLogic;
import com.haoyong.sales.sale.domain.ArrangeType;

/**
 * 安排方式列举 Logic 
 */
public class ArrangeTypeLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(ArrangeType.class);
		Type gtype = new TypeToken<ArrayList<ArrangeType>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<String> getArrangeTypes() {
		List<String> list = new ArrayList<String>();
		list.addAll(new DeliverTypeLogic().getStringTypes());
		list.add("常规库存");
		return list;
	}
	
	private List<String> getStringTypes() {
		List<String> list = new ArrayList<String>();
		for (ArrangeType t: getTypeList()) {
			list.add(t.getName());
		}
		return list;
	}
	
	private List<ArrangeType> getTypeList() {
		TypeInfos infos = getDomain();
		List<ArrangeType> arranges = infos.getInfoList();
		if (arranges.isEmpty()) {
			String[] slist = new String[]{"占用在途", "占用库存", "常规库存"};
			int id=10;
			for (String s: slist) {
				ArrangeType t = new ArrangeType();
				t.setId(++id);
				t.setName(s);
				arranges.add(t);
			}
		}
		return arranges;
	}
	
	public boolean isNormal(String type) {
		return getNormal().equals(type);
	}
	
	public String getNormal() {
		return "常规库存";
	}
	
	public String getUsePassage() {
		return "占用在途";
	}
	
	public String getUseStore() {
		return "占用库存";
	}
	
	public String getUseSubCompany() {
		return "用分公司库存";
	}
}

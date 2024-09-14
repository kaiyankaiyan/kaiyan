package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.josql.Query;
import org.josql.QueryResults;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.PurchaseAgent;
import com.haoyong.sales.base.domain.TypeInfos;

/**
 * 采购|生产单位Logic
 */
public class PurchaseAgentLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(PurchaseAgent.class);
		Type gtype = new TypeToken<ArrayList<PurchaseAgent>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<PurchaseAgent> getTypeList(String supplyType) {
		TypeInfos infos = getDomain();
		List<PurchaseAgent> list = infos.getInfoList();
		StringBuffer sql = new StringBuffer("select * from ");
		sql.append(PurchaseAgent.class.getName()).append(" where supply='").append(supplyType).append("'");
		try {
			Query q = new Query();
			q.parse(sql.toString());
			QueryResults queryResults = q.execute(list);
			return queryResults.getResults();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<String> getStringTypes(String supplyType) {
		List<String> list = new ArrayList<String>();
		for (PurchaseAgent t: getTypeList(supplyType)) {
			list.add(t.getName());
		}
		return list;
	}
}

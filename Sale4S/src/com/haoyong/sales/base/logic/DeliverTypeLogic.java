package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.DeliverType;
import com.haoyong.sales.base.domain.TypeInfos;

/**
 * 发货方式Logic
 */
public class DeliverTypeLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(DeliverType.class);
		Type gtype = new TypeToken<ArrayList<DeliverType>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<DeliverType> getTypeList() {
		TypeInfos infos = getDomain();
		List<DeliverType> list = infos.getInfoList();
		if (list.isEmpty()) {
			String[] slist = new String[]{"普通", "直发", "当地购"};
			for (String s: slist) {
				DeliverType t = new DeliverType();
				t.setName(s);
				list.add(t);
			}
		}
		return list;
	}
	
	public boolean isNeedDeliverNote(String deliverType) {
		return StringUtils.equals(getDirectType(), deliverType) || StringUtils.equals(getLocalType(), deliverType);
	}
	
	public boolean isCommonType(String deliverType) {
		return getCommonType().equals(deliverType);
	}
	
	public String getCommonType() {
		return "普通";
	}
	
	public String getDirectType() {
		return "直发";
	}
	
	public String getLocalType() {
		return "当地购";
	}
	
	public List<String> getStringTypes() {
		List<String> list = new ArrayList<String>();
		for (DeliverType t: getTypeList()) {
			list.add(t.getName());
		}
		return list;
	}
}

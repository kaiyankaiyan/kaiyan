package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.domain.Yards;

public class YardsLogic {
	
	public Yards getYardsByCType(String ctype) {
		if (StringUtils.isEmpty(ctype))			return null;
		for (Yards item: getYardsList()) {
			if (item.getCommType().equals(ctype)) {
				return item;
			}
		}
		return null;
	}
	
	public List<Yards> getYardsList() {
		return getDomain().getInfoList();
	}
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(Yards.class);
		Type gtype = new TypeToken<ArrayList<Yards>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}
}

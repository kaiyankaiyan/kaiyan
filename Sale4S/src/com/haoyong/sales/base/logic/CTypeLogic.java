package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.CType;
import com.haoyong.sales.base.domain.TypeInfos;

/**
 * 商品大类Logic
 */
public class CTypeLogic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(CType.class);
		Type gtype = new TypeToken<ArrayList<CType>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}

	public List<CType> getTypeList() {
		TypeInfos infos = getDomain();
		List<CType> list = infos.getInfoList();
		return list;
	}
}

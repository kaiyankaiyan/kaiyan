package com.haoyong.sales.base.logic;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.CType2;
import com.haoyong.sales.base.domain.TypeInfos;

/**
 * 商品小类Logic
 */
public class CType2Logic {
	
	public TypeInfos getDomain() {
		TypeInfos infos = new TypeInfosLogic().getInfos(CType2.class);
		Type gtype = new TypeToken<ArrayList<CType2>>(){}.getType();
		infos.setType(gtype);
		return infos;
	}
}

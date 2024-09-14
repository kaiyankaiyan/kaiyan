package com.haoyong.sales.base.logic;

import net.sf.mily.ui.WindowMonitor;

import com.haoyong.sales.base.domain.TInfo;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.common.dao.BaseDAO;

/**
 * 分类库Logic
 */
public class TypeInfosLogic {
	
	public <T extends TInfo> TypeInfos getInfos(Class<T> clss) {
		return getTypeInfos(clss);
	}

	private <T extends TInfo> TypeInfos getTypeInfos(Class<T> clss) {
		TypeInfos typeInfos = (TypeInfos)WindowMonitor.getMonitor().getAttribute(clss.getSimpleName());
		if (1==1 && typeInfos!=null) {
			return typeInfos;
		}
		StringBuffer sql = new StringBuffer("select t.* from bs_TypeInfos t where t.clss=? and t.sellerId=?");
		typeInfos = new BaseDAO().nativeQuerySingleResult(sql.toString(), TypeInfos.class, clss.getSimpleName());
		if (typeInfos==null) {
			typeInfos = new TypeInfos();
			typeInfos.setClssName(clss);
		}
		WindowMonitor.getMonitor().addAttribute(clss.getSimpleName(), typeInfos);
		return typeInfos;
	}
}

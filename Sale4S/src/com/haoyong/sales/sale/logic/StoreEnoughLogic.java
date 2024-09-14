package com.haoyong.sales.sale.logic;

import java.util.Date;

import net.sf.mily.types.DateTimeType;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.sale.domain.StoreEnough;

public class StoreEnoughLogic {

	public boolean hasNewStoreEnough(Date fromTime) {
		StringBuffer sb = new StringBuffer("select t.* from sa_storeenough t where t.modifytime>=? and t.sellerId=?");
		StoreEnough first = new BaseDAO().nativeQueryFirstResult(sb.toString(), StoreEnough.class, new DateTimeType().format(fromTime));
		return first!=null;
	}
}

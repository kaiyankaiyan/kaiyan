package com.haoyong.sales.base.logic;

import java.util.List;

import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.StorehouseT;
import com.haoyong.sales.base.form.StorehouseForm;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;

public class StorehouseLogic {
	
	public PropertyChoosableLogic.Choose12<StorehouseForm, Storehouse, StorehouseT> getPropertyChoosableLogic() {
		return new PropertyChoosableLogic.Choose12<StorehouseForm, Storehouse, StorehouseT>(new StorehouseForm(), new Storehouse(), new StorehouseT());
	}
	
	public Storehouse getStorehouseByNumber(String number) {
		String sql = "select t.* from bs_company t where t.dtype=4 and t.number=? and t.sellerId=?";
		Storehouse Storehouse = new BaseDAO().nativeQuerySingleResult(sql, Storehouse.class, number);
		return Storehouse;
	}
	public Storehouse getStorehouseByName(String name) {
		String sql = "select t.* from bs_company t where t.dtype=4 and t.name=? and t.sellerId=?";
		Storehouse Storehouse = new BaseDAO().nativeQuerySingleResult(sql, Storehouse.class, name);
		return Storehouse;
	}
	
	public boolean hasRepeat(Storehouse domain, String column, Object value) {
		String sql = new StringBuffer("select t.* from bs_company t where t.dtype=4")
		.append(" and t.").append(column).append("=?").append(" and t.id!=?")
		.append(" and t.sellerId=?").toString();
		List list = new BaseDAO().nativeSqlQuery(sql, value, domain.getId());
		return list.size()>0;
	}
}

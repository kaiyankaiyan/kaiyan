package com.haoyong.sales.base.logic;

import java.util.List;

import net.sf.mily.webObject.ViewBuilder;

import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.SupplierT;
import com.haoyong.sales.base.form.SupplierForm;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;

/**
 * 查询支持类——供应商
 */
public class SupplierLogic {
	
	public PropertyChoosableLogic.Choose12<SupplierForm, Supplier, SupplierT> getPropertyChoosableLogic() {
		return new PropertyChoosableLogic.Choose12<SupplierForm, Supplier, SupplierT>(new SupplierForm(), new Supplier(), new SupplierT());
	}
	
	public PropertyChoosableLogic.TicketDetail<SupplierForm, Supplier, SupplierT> getLinkChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<SupplierForm, Supplier, SupplierT>(new SupplierForm(), new Supplier(), new SupplierT());
	}
	
	public void fromTrunk(Supplier to, Supplier from) {
		getPropertyChoosableLogic().fromTrunk(to, from);
	}
	
	public SellerViewSetting getViewSetting() {
		return getPropertyChoosableLogic().getChooseSetting( this.getPropertyChoosableLogic().getChooseBuilder() );
	}
	
	public ViewBuilder getViewBuilder() {
		return getPropertyChoosableLogic().getChooseBuilder();
	}
	
	public boolean hasRepeat(Supplier domain, String column, Object value) {
		String sql = new StringBuffer("select t.* from bs_company t where t.dtype=1")
		.append(" and t.").append(column).append("=?").append(" and t.id!=?")
		.append(" and t.sellerId=?").toString();
		List list = new BaseDAO().nativeSqlQuery(sql, value, domain.getId());
		return list.size()>0;
	}
	
	public Supplier getSupplierByLink(String submitNumber) {
		String sql = new StringBuffer("select t.* from bs_company t where t.dtype=1")
		.append(" and t.supplierNameH like '%").append(submitNumber).append("%'")
		.append(" and t.sellerId=?").toString();
		Supplier supplier = new BaseDAO().nativeQuerySingleResult(sql, Supplier.class);
		return supplier;
	}
	
	public Supplier getSupplierByNumber(String number) {
		String sql = "select t.* from bs_company t where t.dtype=1 and t.number=? and t.sellerId=?";
		Supplier supplier = new BaseDAO().nativeQuerySingleResult(sql, Supplier.class, number);
		return supplier;
	}
}

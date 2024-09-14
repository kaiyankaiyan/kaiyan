package com.haoyong.sales.base.logic;

import java.util.HashMap;

import net.sf.mily.util.ReflectHelper;

import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.SupplierT;
import com.haoyong.sales.common.dao.LinkSellerDAO;

public class Supplier4sLogic {
	
	public Supplier getSupplierByLink(String submitNumber) {
		String sql = new StringBuffer("select t.* from bs_company t where t.dtype=1")
		.append(" and t.supplierNameH like '%").append(submitNumber).append("%'")
		.append(" and t.sellerId=?").toString();
		Supplier supplier = new LinkSellerDAO().nativeQuerySingleResult(sql, Supplier.class);
		if (supplier!=null) {
			HashMap<String, Object> row = (HashMap<String, Object>)supplier.getVoParamMap().get("NativeQueryRow");
			SupplierT t = new SupplierT();
			t.setSupplierLabel((String)row.get("supplierLabel"));
			t.setSupplierName((String)row.get("supplierName"));
			t.setSupplierLabelH((String)row.get("supplierLabelH"));
			t.setSupplierNameH((String)row.get("supplierNameH"));
			ReflectHelper.invokeMethod(supplier, "setTSupplier", t);
		}
		return supplier;
	}
}

package com.haoyong.sales.base.logic;

import java.util.Iterator;
import java.util.List;

import com.haoyong.sales.base.domain.PrintViewSerial;
import com.haoyong.sales.common.derby.DerbyDAO;

public class PrintViewSerialLogic {
	
	public List<PrintViewSerial> getViewList() {
		String sql = "select t.* from bs_PrintViewSerial t where t.sellerId=? order by t.id";
		List<PrintViewSerial> viewList = new DerbyDAO().nativeQuery(sql, PrintViewSerial.class);
		for (Iterator<PrintViewSerial> iter=viewList.iterator(); iter.hasNext();) {
			PrintViewSerial view = iter.next();
			if (view.getContent().length()==0)
				iter.remove();
		}
		return viewList;
	}
	
	public List<PrintViewSerial> getTableRows() {
		String sql = "select t.* from bs_PrintViewSerial t where t.sellerId=? order by t.id";
		return new DerbyDAO().nativeQuery(sql, PrintViewSerial.class);
	}
	
	public void save(PrintViewSerial view) {
		DerbyDAO dao = new DerbyDAO();
		dao.saveOrUpdate(view);
		dao.getSession().flush();
	}
}

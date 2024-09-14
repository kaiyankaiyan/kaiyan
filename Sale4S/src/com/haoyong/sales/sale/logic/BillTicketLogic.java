package com.haoyong.sales.sale.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.BillT;
import com.haoyong.sales.sale.domain.BillTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.form.BillTicketForm;


public class BillTicketLogic {
	
	public List<BillDetail> getBillList(List<OrderDetail> purList) {
		if (purList==null || purList.size()==0)
			return new ArrayList<BillDetail>(0);
		StringBuffer sql = new StringBuffer("select t.* from bs_BillDetail t where (");
		for (Iterator<OrderDetail> iter=purList.iterator(); iter.hasNext();) {
			OrderDetail detail = iter.next();
			sql.append("t.monthnum='").append(detail.getMonthnum()).append("'").append(iter.hasNext()? " or ": "");
		}
		sql.append(") and t.sellerId=?");
		return new BaseDAO().nativeQuery(sql.toString(), BillDetail.class);
	}
	
	public PropertyChoosableLogic.TicketDetail<BillTicketForm, BillTicket, BillT> getPropertyChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<BillTicketForm, BillTicket, BillT>(new BillTicketForm(), new BillTicket(), new BillT());
	}
}
package com.haoyong.sales.sale.logic;

import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ArrangeT;
import com.haoyong.sales.sale.domain.ArrangeTicket;
import com.haoyong.sales.sale.form.ArrangeTicketForm;
import com.haoyong.sales.sale.form.PArrangeTicketForm;

public class ArrangeTicketLogic {
	
	public PropertyChoosableLogic.TicketDetail<ArrangeTicketForm, ArrangeTicket, ArrangeT> getPropertyChoosableLogic(String supplyType) {
		SupplyTypeLogic logic = new SupplyTypeLogic();
		if (logic.getProductType().equals(supplyType))
			return new PropertyChoosableLogic.TicketDetail(new PArrangeTicketForm(), new ArrangeTicket(), new ArrangeT());
		return new PropertyChoosableLogic.TicketDetail<ArrangeTicketForm, ArrangeTicket, ArrangeT>(new ArrangeTicketForm(), new ArrangeTicket(), new ArrangeT());
	}
}

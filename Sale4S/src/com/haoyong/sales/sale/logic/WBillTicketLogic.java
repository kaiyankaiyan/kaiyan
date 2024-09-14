package com.haoyong.sales.sale.logic;

import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.WBillT;
import com.haoyong.sales.sale.domain.WBillTicket;
import com.haoyong.sales.sale.form.WBillTicketForm;


public class WBillTicketLogic {
	
	public PropertyChoosableLogic.TicketDetail<WBillTicketForm, WBillTicket, WBillT> getPropertyChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<WBillTicketForm, WBillTicket, WBillT>(new WBillTicketForm(), new WBillTicket(), new WBillT());
	}
}
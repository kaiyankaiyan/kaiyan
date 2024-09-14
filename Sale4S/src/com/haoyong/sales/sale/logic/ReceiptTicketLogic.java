package com.haoyong.sales.sale.logic;

import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ReceiptT;
import com.haoyong.sales.sale.domain.ReceiptTicket;
import com.haoyong.sales.sale.form.ReceiptTicketForm;

public class ReceiptTicketLogic {
	
	public PropertyChoosableLogic.TicketDetail<ReceiptTicketForm, ReceiptTicket, ReceiptT> getTicketChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<ReceiptTicketForm, ReceiptTicket, ReceiptT>(new ReceiptTicketForm(), new ReceiptTicket(), new ReceiptT());
	}
}
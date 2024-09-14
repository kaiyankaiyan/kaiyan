package com.haoyong.sales.sale.logic;

import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.SendT;
import com.haoyong.sales.sale.domain.SendTicket;
import com.haoyong.sales.sale.form.SendTicketForm;

public class SendTicketLogic {
	
	public PropertyChoosableLogic.TicketDetail<SendTicketForm, SendTicket, SendT> getSendChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<SendTicketForm, SendTicket, SendT>(new SendTicketForm(), new SendTicket(), new SendT());
	}
}

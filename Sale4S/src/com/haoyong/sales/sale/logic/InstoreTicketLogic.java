package com.haoyong.sales.sale.logic;

import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.InstoreT;
import com.haoyong.sales.sale.domain.InstoreTicket;
import com.haoyong.sales.sale.form.InAgentTicketForm;
import com.haoyong.sales.sale.form.InStoreTicketForm;

public class InstoreTicketLogic {
	
	/**
	 * 生产车间留用料还仓,BomDetail
	 */
	public PropertyChoosableLogic.TicketDetail<InAgentTicketForm, InstoreTicket, InstoreT> getAgentChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<InAgentTicketForm, InstoreTicket, InstoreT>(new InAgentTicketForm(), new InstoreTicket(), new InstoreT());
	}
	
	/**
	 * 入库,PurchaseDetail
	 */
	public PropertyChoosableLogic.TicketDetail<InStoreTicketForm, InstoreTicket, InstoreT> getStoreChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<InStoreTicketForm, InstoreTicket, InstoreT>(new InStoreTicketForm(), new InstoreTicket(), new InstoreT());
	}
}

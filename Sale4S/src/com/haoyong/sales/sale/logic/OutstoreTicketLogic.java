package com.haoyong.sales.sale.logic;

import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.OutstoreT;
import com.haoyong.sales.sale.domain.OutstoreTicket;
import com.haoyong.sales.sale.form.OutStoreTicketForm;

public class OutstoreTicketLogic {

	/**
	 * 出库,OrderDetail
	 */
	public PropertyChoosableLogic.TicketDetail<OutStoreTicketForm, OutstoreTicket, OutstoreT> getStoreChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<OutStoreTicketForm, OutstoreTicket, OutstoreT>(new OutStoreTicketForm(), new OutstoreTicket(), new OutstoreT());
	}
}

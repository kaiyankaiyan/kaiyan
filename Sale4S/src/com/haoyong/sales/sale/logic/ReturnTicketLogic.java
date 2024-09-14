package com.haoyong.sales.sale.logic;

import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ReturnT;
import com.haoyong.sales.sale.domain.ReturnTicket;
import com.haoyong.sales.sale.form.PurchaseReturnForm;
import com.haoyong.sales.sale.form.OrderReturnForm;

public class ReturnTicketLogic {
	
	/**
	 * 采购明细退货
	 */
	public PropertyChoosableLogic.TicketDetail<PurchaseReturnForm, ReturnTicket, ReturnT> getPurchaseChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<PurchaseReturnForm, ReturnTicket, ReturnT>(new PurchaseReturnForm(), new ReturnTicket(), new ReturnT());
	}
	
	/**
	 * 订单发货的退货
	 */
	public PropertyChoosableLogic.TicketDetail<OrderReturnForm, ReturnTicket, ReturnT> getLocationChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<OrderReturnForm, ReturnTicket, ReturnT>(new OrderReturnForm(), new ReturnTicket(), new ReturnT());
	}
}
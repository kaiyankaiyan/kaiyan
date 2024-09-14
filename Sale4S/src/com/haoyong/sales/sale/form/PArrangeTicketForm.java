package com.haoyong.sales.sale.form;

import java.util.HashMap;
import java.util.List;

import com.haoyong.sales.base.logic.PurchaseAgentLogic;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ArrangeTicket;
import com.haoyong.sales.sale.logic.ArrangeTicketLogic;

public class PArrangeTicketForm extends ArrangeTicketForm {
	
	public HashMap<String, String> getParam4Supply() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("supplyType", "'生产'");
		return map;
	}
	
	protected List<String> getAgentOptions(ArrangeTicket arrangeTicket) {
		List<String> list = new PurchaseAgentLogic().getStringTypes("生产");
		return list;
	}
	
	public PropertyChoosableLogic.TicketDetail getArrangeChoosableLogic() {
		return new ArrangeTicketLogic().getPropertyChoosableLogic("生产");
	}
	
	private ArrangeTicketForm getSuperForm() {
		String k = "ArrangeTicketFormP";
		ArrangeTicketForm form = this.getAttr(k);
		if (form == null) {
			form = new PArrangeTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
}

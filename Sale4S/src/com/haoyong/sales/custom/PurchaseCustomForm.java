package com.haoyong.sales.custom;

import java.util.List;

import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.form.PurchaseTicketForm;


public class PurchaseCustomForm extends AbstractForm<OrderDetail> {
	
	private PurchaseTicketForm getPurchaseTicketForm() {
		PurchaseTicketForm form = this.getAttr(PurchaseTicketForm.class);
		if (form == null) {
			form = new PurchaseTicketForm();
			this.setAttr(form);
		}
		return form;
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
		
	}
}

package com.haoyong.sales.sale.form;

import java.util.List;

import com.haoyong.sales.base.form.BOMForm;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.sale.domain.OrderDetail;

public class SaleQueryForm extends AbstractForm<OrderDetail>{

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
	}
	
	public OrderTicketForm getOrderTicketForm() {
		OrderTicketForm form = this.getAttr(OrderTicketForm.class);
		if (form == null) {
			form = new OrderTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public PurchaseTicketForm getPurchaseTicketForm() {
		PurchaseTicketForm form = this.getAttr(PurchaseTicketForm.class);
		if (form == null) {
			form = new PurchaseTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public PPurchaseTicketForm getPPurchaseTicketForm() {
		PPurchaseTicketForm form = this.getAttr(PPurchaseTicketForm.class);
		if (form == null) {
			form = new PPurchaseTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public BOMForm getBOMForm() {
		BOMForm form = this.getAttr(BOMForm.class);
		if (form == null) {
			form = new BOMForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public ReceiptTicketForm getReceiptTicketForm() {
		ReceiptTicketForm form = this.getAttr(ReceiptTicketForm.class);
		if (form == null) {
			form = new ReceiptTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public SendTicketForm getSendTicketForm() {
		SendTicketForm form = this.getAttr(SendTicketForm.class);
		if (form == null) {
			form = new SendTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public BillTicketForm getBillTicketForm() {
		BillTicketForm form = this.getAttr(BillTicketForm.class);
		if (form == null) {
			form = new BillTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public StoreTicketForm getStoreTicketForm() {
		StoreTicketForm form = this.getAttr(StoreTicketForm.class);
		if (form == null) {
			form = new StoreTicketForm();
			this.setAttr(form);
		}
		return form;
	}
}

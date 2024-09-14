package com.haoyong.sales.test.sale;

import com.haoyong.sales.sale.form.DOrderTicketForm;
import com.haoyong.sales.sale.form.OrderTicketForm;

public class POrderTicketTest extends OrderTicketTest {

	public POrderTicketTest() {
		this.setForm(this.getModeList().contain(TestMode.SubCompany)? new DOrderTicketForm(): new OrderTicketForm());
	}
	
	protected String get客户订单生产(int... amountList) {
		TestMode[] modes0 = this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
		this.getModeList().addMode(TestMode.Product);
		String number = this.get客户订单(amountList);
		this.getModeList().removeMode(TestMode.Product);
		this.getModeList().addMode(modes0);
		return number;
	}
	
	protected String get备货订单生产(int... amountList) {
		TestMode[] modes0 = this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
		this.getModeList().addMode(TestMode.Product);
		String number = this.get备货订单(amountList);
		this.getModeList().removeMode(TestMode.Product);
		this.getModeList().addMode(modes0);
		return number;
	}
}

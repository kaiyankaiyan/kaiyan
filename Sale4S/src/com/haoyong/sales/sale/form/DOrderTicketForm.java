package com.haoyong.sales.sale.form;

import java.util.HashMap;
import java.util.List;

import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.webObject.IEditViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.form.SubCompanyForm;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;

public class DOrderTicketForm extends OrderTicketForm {
	
	public void beforeCreate4Client(IEditViewBuilder builder0) {
		OrderDetail domain = new OrderDetail();
		this.setDomain(domain);
		this.getDetailList().clear();
		getDomain().getOrderTicket().setOrderType(new OrderTypeLogic().getClientType());
		getDrawList().delete(0, getDrawList().length()).append("Client,").append("SubCompany,");
	}
	
	public void beforeCreate4Back(IEditViewBuilder builder0) {
		OrderDetail domain = new OrderDetail();
		this.setDomain(domain);
		this.getDetailList().clear();
		getDomain().getOrderTicket().setOrderType(new OrderTypeLogic().getBackType());
		getDrawList().delete(0, getDrawList().length()).append("SubCompany,");
	}
	
	private void setCreate4Service(ViewData<OrderDetail> viewData) {
		OrderDetail domain = getDomain();
		domain.getOrderTicket().genSerialNumber();
		viewData.setTicketDetails(this.getDetailList());
		PropertyChoosableLogic.TicketDetail logic = new OrderTicketLogic().getTicketChoosableLogic();
		if (domain.getSubCompany().getFromSellerId()>0) {
			domain.getClient().setFromSellerId(domain.getSubCompany().getFromSellerId());
			domain.getClient().setUaccept(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		}
		for (OrderDetail detail: viewData.getTicketDetails()) {
			detail.setMonthnum(new OrderTicketLogic().genMonthnum());
			logic.fromTrunk(logic.getTicketBuilder(), detail.getOrderTicket(), domain.getOrderTicket());
			detail.setClient(domain.getClient());
			detail.setSubCompany(domain.getSubCompany());
		}
	}
	
	public HashMap<String, String> getParam4Order() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("SubCompany", "c.subName is not null");
		return map;
	}
	
	private void getSubCompanySearchName(TextField input) {
		String name = input.getText();
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "SubCompanyQuery", 1|2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	private SubCompanyForm getSubCompanyForm() {
		return new SubCompanyForm();
	}
	private void setSubCompanySelect(List<SubCompany> subList) {
		SubCompany sub = subList.size()==0? new SubCompany(): subList.get(0);
		getDomain().getSubCompany().setName(sub.getName());
		this.getDomain().setSubCompany(sub);
	}
}

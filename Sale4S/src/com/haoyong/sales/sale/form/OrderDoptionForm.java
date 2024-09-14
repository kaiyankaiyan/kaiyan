package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.OrderDoption;
import com.haoyong.sales.sale.logic.OrderDoptionLogic;

/**
 * 界面管理——订单类型
 */
public class OrderDoptionForm extends AbstractForm<OrderDoption> {
	
	public void beforeList(IEditViewBuilder builder0) {
		this.setOrderDoptionList(new OrderDoptionLogic().getTypeList());
	}
	
	public void createPrepare(){
		this.setAttr(new OrderDoption());
	}
	
	public void createCommit()throws Exception {
		for (OrderDoption info: this.getOrderDoptionList()) {
			if (info!=this.getOrderDoption() && info.getName().equals(getOrderDoption().getName()))
				throw new LogicException(2, "名称不能重复");
		}
	}

	private void setOrder4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new OrderDoptionLogic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", getOrderDoption());
	}

	@Override
	public void setSelectedList(List<OrderDoption> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	public void editPrepare() {
		OrderDoption s = this.getSelectedList().get(0);
		this.setAttr(s);
	}
	
	private List<OrderDoption> getSelectedList() {
		String k = "SelectedList";
		List<OrderDoption> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDoption>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<OrderDoption> getOrderDoptionList() {
		String k = "OrderDoptionList";
		return this.getAttr(k);
	}
	
	private void setOrderDoptionList(List<OrderDoption> list) {
		String k = "OrderDoptionList";
		this.setAttr(k, list);
	}
	
	private OrderDoption getOrderDoption() {
		OrderDoption d = this.getAttr(OrderDoption.class);
		if (d == null) {
			d = new OrderDoption();
			this.setAttr(d);
		}
		return d;
	}
}
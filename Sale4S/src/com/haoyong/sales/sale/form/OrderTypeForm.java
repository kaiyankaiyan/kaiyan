package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.OrderType;
import com.haoyong.sales.sale.logic.OrderTypeLogic;

/**
 * 界面管理——订单类型
 */
public class OrderTypeForm extends AbstractForm<OrderType> {
	
	public void beforeList(IEditViewBuilder builder0) {
		this.setOrderTypeList(new OrderTypeLogic().getTypeList());
	}
	
	public void createPrepare(){
		this.setAttr(new OrderType());
	}
	
	public void createCommit()throws Exception {
		for (OrderType info: this.getOrderTypeList()) {
			if (info!=this.getOrderType() && info.getName().equals(getOrderType().getName()))
				throw new LogicException(2, "名称不能重复");
		}
	}

	private void setOrder4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new OrderTypeLogic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", getOrderType());
	}

	@Override
	public void setSelectedList(List<OrderType> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	public void editPrepare() {
		OrderType s = this.getSelectedList().get(0);
		this.setAttr(s);
	}
	
	private List<OrderType> getSelectedList() {
		String k = "SelectedList";
		List<OrderType> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderType>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<OrderType> getOrderTypeList() {
		String k = "OrderTypeList";
		return this.getAttr(k);
	}
	
	private void setOrderTypeList(List<OrderType> list) {
		String k = "OrderTypeList";
		this.setAttr(k, list);
	}
	
	private OrderType getOrderType() {
		OrderType d = this.getAttr(OrderType.class);
		if (d == null) {
			d = new OrderType();
			this.setAttr(d);
		}
		return d;
	}
}
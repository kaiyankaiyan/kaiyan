package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.BillType;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;


/**
 * 界面管理——票据类型
 */
public class BillTypeForm extends AbstractForm<BillType> {
	
	private BillType domain;
	private List<BillType> detailList;

	public void beforeList(IEditViewBuilder builder0) {
		this.detailList = new BillTypeLogic().getTypeList();
	}
	
	public void prepareCreate(){
		this.domain = new BillType();
	}
	
	public void prepareEdit() {
		this.domain = this.getSelectedList().get(0);
	}
	
	public void validateBill()throws Exception {
		for (BillType info: this.detailList) {
			if (info!=domain && info.getName().equals(domain.getName())) {
				throw new LogicException(2, "票据类型不能重复");
			}
		}
	}

	private List<BillType> getSelectedList() {
		String k = "SelectedList";
		List<BillType> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<BillType>();
			setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<BillType> selected) {
		getSelectedList().clear();
		getSelectedList().addAll(selected);
	}
	
	public void setBill4Service(ViewData<TypeInfos> viewData) {
		TypeInfos infos = new BillTypeLogic().getDomain();
		viewData.setTicketDetails(infos);
		viewData.setParam("info", this.domain);
	}
	
	public void setBillSelected(List<BillType> supplyList) {
		this.domain = supplyList.get(0);
	}
	
	public BillTypeForm getForm() {
		return this;
	}
}

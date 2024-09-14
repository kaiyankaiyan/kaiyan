package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.DeliverType;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;

/**
 * 界面管理——发货方式
 */
public class DeliverTypeForm extends AbstractForm<DeliverType> {
	
	private DeliverType domain;
	private List<DeliverType> selectedList;
	private List<DeliverType> detailList;

	public void beforeList(IEditViewBuilder builder0) {
		this.detailList = new DeliverTypeLogic().getTypeList();
	}
	
	public void createPrepare(){
		domain = new DeliverType();
		selectedList=new ArrayList<DeliverType>();
	}
	
	/**
	 * 提交
	 */
	public void createCommit()throws Exception {
		for (DeliverType info: detailList) {
			if (info!=domain && info.getName().equals(domain.getName())) {
				throw new LogicException(2, "大类名称不能重复");
			}
		}
	}

	public List<DeliverType> getSelectedList() {
		return this.selectedList;
	}

	private void setDeliver4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new DeliverTypeLogic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", getDomain());
	}

	@Override
	public void setSelectedList(List<DeliverType> selected) {
		this.selectedList = selected;
	}
	
	public void editPrepare() {
		this.domain = selectedList.get(0);
	}
	
	public DeliverTypeForm getForm() {
		return this;
	}
	
	public DeliverType getDomain() {
		return domain;
	}

	public void setDomain(DeliverType t) {
		this.domain = t ;
	}
}

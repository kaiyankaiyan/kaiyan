package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.PurchaseAgent;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.PurchaseAgentLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;

/**
 * 界面管理——商品大类
 */
public class SupplyTypeForm extends AbstractForm<SupplyType> {
	
	public void beforeList(IEditViewBuilder builder0) {
		this.getDetailList().clear();
		this.getDetailList().addAll(new SupplyTypeLogic().getTypeList());
	}
	
	public void prepareAgent(){
		PurchaseAgent agent = new PurchaseAgent();
		agent.setSupply(this.getSupply().getName());
		this.setAgent(agent);
	}
	
	/**
	 * 提交
	 */
	public void validateAgent()throws Exception {
		PurchaseAgent agent = this.getAgent();
		for (PurchaseAgent info: getAgentList()) {
			if (info!=agent && info.getName().equals(agent.getName())) {
				throw new LogicException(2, "单位名称不能重复");
			}
		}
	}
	
	private List<SupplyType> getSelectedList() {
		String k = "SelectedList";
		List<SupplyType> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<SupplyType>();
			setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<SupplyType> selected) {
		getSelectedList().clear();
		getSelectedList().addAll(selected);
	}

	private void setSupply4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new SupplyTypeLogic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", this.getSupply());
	}
	
	public void setAgent4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new PurchaseAgentLogic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", this.getAgent());
	}
	
	private List<SupplyType> getDetailList() {
		String k = "DetailList";
		List<SupplyType> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<SupplyType>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<PurchaseAgent> getAgentList() {
		return new PurchaseAgentLogic().getTypeList(this.getSupply().getName());
	}
	
	public void setSupplySelected(List<SupplyType> supplyList) {
		SupplyType supply = supplyList.get(0);
		this.setSupply(supply);
	}
	
	public void setAgentSelected(List<PurchaseAgent> agentList) {
		this.setAgent(agentList.get(0));
	}
	
	private PurchaseAgent getAgent() {
		PurchaseAgent agent = this.getAttr(PurchaseAgent.class);
		if (agent == null) {
			agent = new PurchaseAgent();
			this.setAttr(agent);
		}
		return agent;
	}
	
	private void setAgent(PurchaseAgent agent) {
		this.setAttr(agent);
	}
	
	private SupplyType getSupply() {
		SupplyType type = this.getAttr(SupplyType.class);
		if (type == null) {
			type = new SupplyType();
			this.setAttr(type);
		}
		return type;
	}
	
	public void setSupply(SupplyType type) {
		this.setAttr(type);
	}
	
	public SupplyTypeForm getForm() {
		return this;
	}
}

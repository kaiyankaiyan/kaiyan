package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.List;

import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.form.AbstractForm;
/**
 * 界面管理——基础查询
 */
public class BaseQueryForm extends AbstractForm<AbstractDomain>{
	
	public void setSelectedList(List<AbstractDomain> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}

	private List<AbstractDomain> getSelectedList() {
		String k = "SelectedList";
		List<AbstractDomain> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<AbstractDomain>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private CommodityForm getCommodityForm() {
		CommodityForm form = this.getAttr(CommodityForm.class);
		if (form == null) {
			form = new CommodityForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private ClientForm getClientForm() {
		ClientForm form = this.getAttr(ClientForm.class);
		if (form == null) {
			form = new ClientForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private SupplierForm getSupplierForm() {
		SupplierForm form = this.getAttr(SupplierForm.class);
		if (form == null) {
			form = new SupplierForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private SubCompanyForm getSubCompanyForm() {
		SubCompanyForm form = this.getAttr(SubCompanyForm.class);
		if (form == null) {
			form = new SubCompanyForm();
			this.setAttr(form);
		}
		return form;
	}
}

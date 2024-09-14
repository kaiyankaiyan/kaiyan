package com.haoyong.salel.base.form;

import java.util.List;

import com.haoyong.salel.common.domain.AbstractDomain;
import com.haoyong.salel.common.form.AbstractForm;
/**
 * 界面管理——基础查询
 */
public class BaseQueryForm extends AbstractForm<AbstractDomain>{
	
	private AbstractDomain domain;
	private List<AbstractDomain> selectedList;
	
	
	@Override
	public void setSelectedList(List<AbstractDomain> selected) {
		this.selectedList=selected;
	}

	public AbstractDomain getDomain() {
		return domain;
	}

	public void setDomain(AbstractDomain t) {
		this.domain = t ;
	}
	
	private ProvinceForm getProvinceForm() {
		String k = "ProvinceForm";
		ProvinceForm f = this.getAttr(k);
		if (f==null) {
			f = new ProvinceForm();
			this.setAttr(k, f);
		}
		return f;
	}
	private CityForm getCityForm() {
		String k = "CityForm";
		CityForm f = this.getAttr(k);
		if (f==null) {
			f = new CityForm();
			this.setAttr(k, f);
		}
		return f;
	}
}

package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.CType;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.CType2Logic;
import com.haoyong.sales.base.logic.CTypeLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;

/**
 * 界面管理——商品大类
 */
public class CTypeForm extends AbstractForm<CType> {
	
	private CType domain;
	private List<CType> selectedList;
	private List<CType> detailList;

	public void beforeList(IEditViewBuilder builder0) {
		this.detailList = new CTypeLogic().getTypeList();
	}
	
	public void createPrepare(){
		domain = new CType();
		selectedList=new ArrayList<CType>();
	}
	
	/**
	 * 提交
	 */
	public void createCommit()throws Exception {
		for (CType info: detailList) {
			if (info!=domain && info.getName().equals(domain.getName())) {
				throw new LogicException(2, "大类名称不能重复");
			}
		}
	}

	public List<CType> getSelectedList() {
		return this.selectedList;
	}

	private void getViewData(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new CTypeLogic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", getDomain());
	}

	@Override
	public void setSelectedList(List<CType> selected) {
		this.selectedList = selected;
	}
	
	public void editPrepare() {
		this.domain = selectedList.get(0);
	}
	
	private void setCType4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new CType2Logic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", getDomain());
	}
	
	public CTypeForm getForm() {
		return this;
	}
	
	public CType getDomain() {
		return domain;
	}

	public void setDomain(CType t) {
		this.domain = t ;
	}
}

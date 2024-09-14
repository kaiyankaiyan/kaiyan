package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.CType2;
import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.logic.CType2Logic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;

/**
 * 界面管理——商品大类
 */
public class CType2Form extends AbstractForm<CType2> {
	
	private CType2 domain;
	private List<CType2> selectedList;
	private List<CType2> detailList;

	public void beforeList(IEditViewBuilder builder0) {
		this.detailList = new CType2Logic().getDomain().getInfoList();
	}
	
	public void createPrepare(){
		domain = new CType2();
		selectedList=new ArrayList<CType2>();
	}
	
	/**
	 * 提交
	 */
	public void createCommit()throws Exception {
		for (CType2 info: detailList) {
			if (info!=domain && info.getName().equals(domain.getName())) {
				throw new LogicException(2, "大类名称不能重复");
			}
		}
	}

	public List<CType2> getSelectedList() {
		return this.selectedList;
	}

	private void setCType4Service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new CType2Logic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", getDomain());
	}

	@Override
	public void setSelectedList(List<CType2> selected) {
		this.selectedList = selected;
	}
	
	public void editPrepare() {
		this.domain = selectedList.get(0);
	}
	
	public CType2Form getForm() {
		return this;
	}
	
	public CType2 getDomain() {
		return domain;
	}

	public void setDomain(CType2 t) {
		this.domain = t ;
	}
}

package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.webObject.IEditViewBuilder;

import com.haoyong.sales.base.domain.TypeInfos;
import com.haoyong.sales.base.domain.Yards;
import com.haoyong.sales.base.logic.YardsLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;

/**
 * 界面管理——商品小类码谱
 */
public class YardsForm extends AbstractForm<Yards> {
	
	private Yards domain;
	private List<Yards> selectedList;
	private List<Yards> detailList;

	public void beforeList(IEditViewBuilder builder0) {
		this.detailList = new YardsLogic().getYardsList();
	}
	
	public void createPrepare(){
		domain = new Yards();
		selectedList=new ArrayList<Yards>();
	}
	
	/**
	 * 提交
	 */
	public void createCommit()throws Exception {
		for (Yards info: detailList) {
			if (info!=domain && info.getCommType().equals(domain.getCommType())) {
				throw new LogicException(2, "已经存在此商品分类的码谱！");
			}
		}
	}

	public List<Yards> getSelectedList() {
		return this.selectedList;
	}

	private void setYards4service(ViewData<TypeInfos> viewData) {
		TypeInfos domain = new YardsLogic().getDomain();
		viewData.setTicketDetails(domain);
		viewData.setParam("info", getDomain());
	}

	@Override
	public void setSelectedList(List<Yards> selected) {
		this.selectedList = selected;
	}
	
	public void editPrepare() {
		this.domain = selectedList.get(0);
	}
	
	public YardsForm getForm() {
		return this;
	}
	
	public Yards getDomain() {
		return domain;
	}

	public void setDomain(Yards t) {
		this.domain = t ;
	}
}

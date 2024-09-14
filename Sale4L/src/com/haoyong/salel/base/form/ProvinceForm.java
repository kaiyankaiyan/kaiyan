package com.haoyong.salel.base.form;

import java.util.List;

import net.sf.mily.support.throwable.LogicException;

import com.haoyong.salel.base.domain.Province;
import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.base.logic.SellerLogic;
import com.haoyong.salel.common.form.AbstractForm;
import com.haoyong.salel.common.form.ViewData;
import com.haoyong.salel.util.TicketUtil;

/**
 * 界面管理——省份
 */
public class ProvinceForm extends AbstractForm<Province>{

	private Province domain;
	private List<Province> selectedList;
	
	public void createPrepare(){
		domain = new Province();
		List<Seller> list = new SellerLogic().getSellerAll();
		list.size();
	}
	/**
	 *  这个方法一般用于验证数据等
	 */
	public void createCommit()throws Exception { 
		if (!TicketUtil.isValid("bs_province", "number", domain, domain.getNumber())) {
			throw new LogicException(53, "省份 " + domain.getNumber());
		}
		if (!TicketUtil.isValid("bs_province", "name", domain, domain.getName())) {
			throw new LogicException(53, "省份 " + domain.getName());
		}
	}

	private void setProvince4Service(ViewData<Province> viewData) {
		viewData.setTicketDetails(domain);
		viewData.setCurrentUser(getUser());
	}

	private void setProvinceDelete4Service(ViewData<Province> viewData) {
		viewData.setTicketDetails(this.selectedList);
		viewData.setCurrentUser(getUser());
	}
	
	@Override
	public void setSelectedList(List<Province> selected) {
		this.selectedList = selected;
	}
	/**
	 * 为编辑准备数据
	 */
	public void editPrepare() {
		this.domain = selectedList.get(0);
	}

	public Province getDomain() {
		return domain;
	}

	public void setDomain(Province t) {
		this.domain = t ;
	}
}

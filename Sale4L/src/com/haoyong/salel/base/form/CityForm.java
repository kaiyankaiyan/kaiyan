package com.haoyong.salel.base.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;

import com.haoyong.salel.base.domain.City;
import com.haoyong.salel.base.domain.Province;
import com.haoyong.salel.base.logic.ProvinceLogic;
import com.haoyong.salel.common.form.AbstractForm;
import com.haoyong.salel.common.form.ViewData;
import com.haoyong.salel.util.TicketUtil;

public class CityForm extends AbstractForm<City>{
	
	private Province province;

	private City domain;
	
	private List<City> selectedList;
	
	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}
	
	public void createPrepare(){
		domain = new City();
		domain.setNumber(province.getNumber());
		domain.setProvinceId(province.getId());
	}
	
	
	public HashMap<String, String> getListParameters() {
		HashMap<String, String> params = new HashMap<String, String>();
		if (province == null)			throw new LogicException(2, "请先建立省份");
		params.put("provinceId", ""+province.getId());
		return params;
	}
	
	/**
	 * 提交
	 * @throws Exception
	 */
	public void createCommit()throws Exception {
		Province pro = province;
		String name = domain.getName();
		if(pro==null){
			throw new Exception("请选择省份");
		}
		if (!TicketUtil.isValid("bs_city", "name", domain,  domain.getName(), " and t.PROVINCEID = " +province.getId())) {
			throw new LogicException(53, province.getName()+"的"+ domain.getName());
		}
	}

	private void setCity4Service(ViewData<City> viewData) {
		viewData.setTicketDetails(domain);
	}
	
	private void setCityDelete4Service(ViewData<City> viewData) {
		viewData.setTicketDetails(this.selectedList);
	}

	@Override
	public void setSelectedList(List<City> selected) {
		this.selectedList = selected;
	}
	
	public void editPrepare() {
		this.domain = selectedList.get(0);
	}
	
	public CityForm getForm() {
		return this;
	}
	
	public List<Province> getTreeNodes(Object parent, Object province) {
		List<Province> items = new ArrayList<Province>();
		Province prt = (Province)parent;
		if (prt == null) {
			items= new ProvinceLogic().getAll();
			if (items.size() > 0) this.province = items.get(0);
		}
		return items;
	}

	public City getDomain() {
		return domain;
	}

	public void setDomain(City t) {
		this.domain = t ;
	}
}

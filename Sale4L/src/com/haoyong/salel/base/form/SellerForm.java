package com.haoyong.salel.base.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.database.DatabaseInfoReader;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;

import org.apache.commons.lang.StringUtils;

import com.haoyong.salel.base.domain.City;
import com.haoyong.salel.base.domain.Province;
import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.common.form.AbstractForm;
import com.haoyong.salel.common.form.TicketFormer;
import com.haoyong.salel.common.form.ViewData;
import com.haoyong.salel.util.TicketUtil;

/**
 * 界面管理——商家
 */
public class SellerForm extends AbstractForm<Seller> implements TicketFormer<Seller>{

	private Seller domain;
	private List<Seller> selectedList;
	
	public void createPrepare(){
		domain = new Seller();
	}
	/**
	 *  这个方法一般用于验证数据等
	 */
	public void createCommit() throws Exception { 
		if(StringUtils.isBlank(domain.getName())) {
			throw new Exception("商家名称输入不合法");
		}
		if(StringUtils.isBlank(domain.getSqlName())) {
			throw new Exception("商家数据库名输入不合法");
		}
		if (!TicketUtil.isValid("bs_seller", "name", domain, domain.getName())) {
			throw new LogicException(53, "商家名称重复" + domain.getName());
		}
	}

	private void setSeller4Service(ViewData<Seller> viewData) {
		domain.genSerialNumber();
		viewData.setTicketDetails(domain);
	}

	private void setSellerDelete4Service(ViewData<Seller> viewData) {
		viewData.setTicketDetails(this.selectedList);
	}

	@Override
	public void setSelectedList(List<Seller> selected) {
		this.selectedList = selected;
	}
	
	/**
	 * 为编辑准备数据
	 */
	public void editPrepare() {
		this.domain = selectedList.get(0);
	}
	
	/**
	 * 为明细准备数据
	 */
	public void getDetail() {
		this.domain = selectedList.get(0);
	}
	
	public HashMap<String, Object> getParameterDetailIds() {
		HashMap<String, Object> params = new HashMap<String, Object>();
		//参数
		StringBuilder sbIds = new StringBuilder();
		//获取已经选择的商家id
		for(Iterator<Seller> ite=this.selectedList.iterator();ite.hasNext();){
			sbIds.append(ite.next().getId());
			if(ite.hasNext()){
				sbIds.append(",");
			}
		}
		
		if(sbIds.length()>0){
			params.put("detailIds",sbIds.toString());
		}else{
			params.put("detailIds",0);
		}
		return params;
	}
	
	
	private void getProvinceSearchName(TextField input) {
		this.setIsDialogOpen(false);
		String name = input.getText();
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "ProvinceQuery", 2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	private void getCitySearchName(TextField input) {
		this.setIsDialogOpen(false);
		String name = input.getText();
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "CityQuery", 2, "provinceName", this.getDomain().getProvinceName(), "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	public List<Seller> getDetailList() {
		String k = "DetailList";
		List<Seller> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Seller>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private SelectTicketFormer4Sql<SellerForm, City> getSelectFormer4City() {
		String k = "SelectFormer4City";
		SelectTicketFormer4Sql<SellerForm, City> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<SellerForm, City>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Sql<SellerForm, Province> getSelectFormer4Province() {
		String k = "SelectFormer4Province";
		SelectTicketFormer4Sql<SellerForm, Province> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<SellerForm, Province>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public HashMap<String, Object> getCityParameters () {
		HashMap<String, Object> params = new HashMap<String, Object>();
		String provinceName = domain.getProvinceName();
		if(provinceName!=null){
			params.put("condition", "p.name='"+provinceName+"'");
		}else{
			throw new LogicException(1, "请选择省份!");
		}
		return params;
	}
	
	public Seller getDomain() {
		if(domain==null){
			domain=new Seller();
		}
		return domain;
	}

	public void setDomain(Seller t) {
		this.domain = t ;
	}
	
	public void autoRes(){
		for(Seller seller:selectedList){
			seller.setSqlName(this.domain.getSqlName());
		}
	}
	
	public boolean getIsDialogOpen() {
		String k = "IsDialogOpen";
		Boolean ok = this.getAttr(k);
		if (ok == null) {
			ok = Boolean.FALSE;
			this.setAttr(k, ok);
		}
		return ok;
	}
	
	private void setIsDialogOpen(boolean open) {
		String k = "IsDialogOpen";
		this.setAttr(k, open);
	}
	
	private ProvinceForm getProvinceForm() {
		ProvinceForm form = this.getAttr(ProvinceForm.class);
		if (form == null) {
			form = new ProvinceForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private CityForm getCityForm() {
		CityForm form = this.getAttr(CityForm.class);
		if (form == null) {
			form = new CityForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private void setProvinceSelect(List<Province> provinceList) {
		Province province = provinceList.size()==0? new Province(): provinceList.get(0);
		if (StringUtils.equals(this.domain.getProvinceName(), province.getName())==false) {
			this.setCitySelect(new ArrayList<City>(0));
		}
		this.domain.setProvinceName(province.getName());
		this.domain.setProvinceNumber(province.getNumber());
	}
	
	private void setCitySelect(List<City> cityList) {
		City city = cityList.size()==0? new City(): cityList.get(0);
		Province province = city.getVoparam(Province.class);
		if (province==null)
			province = new Province();
		this.domain.setProvinceName(province.getName());
		this.domain.setProvinceNumber(province.getNumber());
		this.domain.setCityName(city.getName());
		this.domain.setCityNumber(city.getNumber());
	}
	
	public List<String> getSqlnameOptions(Object seller) {
		return DatabaseInfoReader.getReader().getDatabaseInfo().getSellerList();
	}
	
	private HashMap<String, String> getParam4Seller() {
		HashMap<String, String> map = new HashMap<String, String>();
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iter=DatabaseInfoReader.getReader().getDatabaseInfo().getSellerList().iterator(); iter.hasNext();) {
			String sname = iter.next();
			sb.append("c.sqlName='").append(sname).append("'").append(iter.hasNext()? " or ": "");
		}
		map.put("sqlName", sb.toString());
		return map;
	}
}

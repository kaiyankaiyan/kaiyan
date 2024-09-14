package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.Component;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.SubmitTypeLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;

/**
 * 界面管理——供应商
 */
public class SupplierForm extends AbstractForm<Supplier> implements FViewInitable {

	private Supplier domain;
	private List<Supplier> selectedList;

	public void prepareCreate(){
		domain = new Supplier();
	}

	public void prepareEdit() {
		this.domain = selectedList.get(0);
	}

	public void prepareSubmitLink() {
		this.domain = selectedList.get(0);
		if (StringUtils.isBlank(this.domain.getToSellerName()))
			this.domain.setToSellerName(this.domain.getName());
	}
	
	public void prepareImport() {
		this.setDomain(new Supplier());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	public void validateSupplier() {
		StringBuffer sb = validateSupplier(this.getDomain());
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private StringBuffer validateSupplier(Supplier domain) {
		StringBuffer sb = new StringBuffer();
		if (new SupplierLogic().getPropertyChoosableLogic().isValid(domain, sb)==false)
			sb.append("请补充供应商信息，");
		if (StringUtils.isBlank(domain.getName())==false && new SupplierLogic().hasRepeat(domain, "name", domain.getName()))
			sb.append("供应商名称重复").append(domain.getName()).append(",");
		if (StringUtils.isBlank(domain.getNumber())==false && new SupplierLogic().hasRepeat(domain, "number", domain.getNumber()))
			sb.append("供应商编号重复").append(domain.getNumber()).append(",");
		return sb;
	}
	
	private void validateToSeller() {
		Supplier link = this.getDomain();
		Seller seller = null;
		try {
			seller = new Seller4lLogic().getSeller(link.getToSellerName(), this.getSellerId());
		}catch(Exception e) {
		}
		if (seller==null)
			throw new LogicException(2, "无此商家信息。");
		else
			link.setToSellerId(seller.getId());
	}
	
	public void validateToImport(Component fcomp) throws Exception {
		this.getBaseImportForm().validateIndexes(fcomp);
		StringBuffer sb = new StringBuffer();
		StringBuffer input = new StringBuffer().append(getDomain().getVoParamMap().get("Remark"));
		if ("null".equals(input.toString()))
			sb.append("请粘贴入单元格内容，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else {
			// 保存列序号配置到Derby
			ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
			SellerViewInputs inputs = this.getBaseImportForm().getSellerViewInputs();
			this.getBaseImportForm().setSellerIndexes(listview, inputs);
			new SellerViewInputsLogic().saveOrUpdate(inputs);
		}
	}
	
	public void validateImport(Component fcomp) throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (getImportList().size()==0)
			sb.append("导入明细为空，");
		int ri=1;
		for (Iterator<Supplier> iter=getImportList().iterator(); iter.hasNext(); ri++) {
			Supplier d = iter.next();
			sitem = validateSupplier(d);
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateSubmitLink() {
		StringBuffer sb = new StringBuffer();
		if (new SupplierLogic().getLinkChoosableLogic().isValid(this.getDomain(), sb)==false)
			sb.append("请补充链接信息，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public SupplierForm getNewCreate(){
		return this;
	}

	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true)
			new SupplierLogic().getPropertyChoosableLogic().chooseViewBuilder(viewBuilder);
		if (true)
			new SupplierLogic().getLinkChoosableLogic().trunkViewBuilder(viewBuilder);
		if (viewBuilder.getName().equals("Import"))
			this.getBaseImportForm().setImportBuilderInit(viewBuilder);
	}
	
	public void setSupplier4Service(ViewData<Supplier> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	
	public void setDelete4Service(ViewData<Supplier> viewData) {
		viewData.setTicketDetails(this.getSelectedList());
	}
	
	public void setImport4Service(ViewData<Supplier> viewData) {
		viewData.setTicketDetails(this.getImportList());
	}

	public List<Supplier> getSelectedList() {
		return this.selectedList;
	}

	@Override
	public void setSelectedList(List<Supplier> selected) {
		this.selectedList = selected;
	}

	public Supplier getDomain() {
		return domain;
	}
	
	private Supplier getSupplier() {
		String k = "SupplierChoose";
		Supplier supplier = this.getAttr(k);
		if (supplier == null) {
			supplier = new Supplier();
			this.setAttr(k, supplier);
		}
		return supplier;
	}

	public void setDomain(Supplier t) {
		this.domain = t ;
	}
	
	public List<Supplier> getImportList() {
		String k = "ImportList";
		List<Supplier> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Supplier>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private BaseImportForm getBaseImportForm() {
		BaseImportForm form = this.getAttr(BaseImportForm.class);
		if (form==null) {
			form = new BaseImportForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private void setImportLabelLoad(Component fcomp) {
		ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
		String builderName=listview.getViewBuilder().getFullViewName();
		SellerViewInputs inputs = new SellerViewInputsLogic().get(builderName);
		if (inputs == null) {
			inputs = new SellerViewInputs();
			inputs.setBuilderName(builderName);
		}
		this.getBaseImportForm().setFormProperty("attrMap.SellerViewInputs", inputs);
	}
	
	private void setImportFormated(Component fcomp) throws Exception {
		List<Supplier> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		SupplierLogic logic = new SupplierLogic();
		former.setViewBuilder(logic.getViewBuilder());
		former.setSellerViewSetting(logic.getViewSetting());
		return former;
	}
	
	private List<String> getSubmitTypeOptions(Object submitLink) {
		return new SubmitTypeLogic().getTypeList();
	}
	
	private void setSubmitLinkNumber() {
		StringBuffer sb = new StringBuffer().append(Calendar.getInstance().getTimeInMillis());
		for (int len=sb.length(), arr[]=new int[len], ia=0; ia<len; ia++) {
			arr[ia]=Integer.parseInt(sb.substring(ia, ia+1));
			if (ia+1==len) {
				for (int i=arr.length; i-->0;) {
					int j = new Double(Math.floor(Math.random() * i)).intValue();
					int arj=arr[j], ari=arr[i];
					arr[j] = ari;
					arr[i] = arj;
				}
				sb = new StringBuffer();
				for (int i=0; i<len; i++) {
					sb.append(arr[i]);
				}
			}
		}
		this.getDomain().setSubmitNumber(sb.toString());
	}
}

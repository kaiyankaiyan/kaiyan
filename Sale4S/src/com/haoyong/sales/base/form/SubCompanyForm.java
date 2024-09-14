package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.Component;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SubmitTypeLogic;
import com.haoyong.sales.base.logic.Supplier4sLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.dao.LinkSellerDAO;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.ActionService4LinkListener;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.TicketUser;

/**
 * 界面管理——分公司
 */
public class SubCompanyForm extends AbstractForm<SubCompany> implements FViewInitable {
	
	private SubCompany domain;
	private List<SubCompany> selectedList;
	
	public void prepareCreate(){
		domain = new SubCompany();
		selectedList=new ArrayList<SubCompany>();
	}
	
	public void prepareEdit() {
		this.domain = selectedList.get(0);
	}
	
	public void prepareAcceptLink() {
		this.domain = selectedList.get(0);
		if (StringUtils.isBlank(this.domain.getFromSellerName()))
			this.domain.setFromSellerName(this.domain.getName());
	}
	
	public void prepareImport() {
		this.setDomain(new SubCompany());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	public void validateCommit() {
		StringBuffer sb = validateSubCompany(this.getDomain());
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private StringBuffer validateSubCompany(SubCompany domain) {
		StringBuffer sb = new StringBuffer();
		if(StringUtils.isBlank(domain.getName()))
			sb.append("分公司名称不能为空，");
		if (StringUtils.isBlank(domain.getNumber())==false && new SubCompanyLogic().hasRepeat(domain, "number", domain.getNumber()))
			sb.append("分公司编号重复").append(domain.getNumber()).append(",");
		return sb;
	}
	
	private void validateFromSeller() {
		SubCompany link = this.getDomain();
		Seller seller = null;
		try {
			seller = new Seller4lLogic().getSeller(link.getFromSellerName(), this.getSellerId());
		}catch(Exception e) {
		}
		if (seller==null)
			throw new LogicException(2, "无此商家名称。");
		if (true) {
			new LinkSellerDAO().setLinkSeller(seller);
			Supplier supplier = new Supplier4sLogic().getSupplierByLink(link.getSubmitNumber());
			if (supplier==null) {
				throw new LogicException(2, "链接商家授权码错误。");
			} else if (new SubmitTypeLogic().isSubCompanyType(supplier.getSubmitType())==false) {
				throw new LogicException(2, "非分公司链接类型。");
			} else {
				link.setFromSellerId(seller.getId());
				link.setSubmitType(supplier.getSubmitType());
				link.setUaccept(supplier.getUaccept());
			}
		}
		if (true) {
			TicketUser user = new TicketUser();
			user.setUser(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
			user.setDate(new Date());
			link.setFromSellerId(seller.getId());
			link.setUaccept(user.getUserDate());
		}
	}
	
	private void validateAcceptLink() {
		StringBuffer sb = new StringBuffer();
		if (new SubCompanyLogic().getLinkChoosableLogic().isValid(this.getDomain(), sb)==false)
			sb.append("请补充链接信息，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void validateToImport(Component fcomp) throws Exception {
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
	
	private void validateImport(Component fcomp) throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (getImportList().size()==0)
			sb.append("导入明细为空，");
		int ri=1;
		for (Iterator<SubCompany> iter=getImportList().iterator(); iter.hasNext(); ri++) {
			SubCompany d = iter.next();
			sitem = validateSubCompany(d);
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true) {
			new SubCompanyLogic().getPropertyChoosableLogic().chooseViewBuilder(viewBuilder);
		}
		if (true) {
			new SubCompanyLogic().getLinkChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (viewBuilder.getName().equals("Import"))
			this.getBaseImportForm().setImportBuilderInit(viewBuilder);
	}
	
	public void setCompanyA4Service(ViewData<SubCompany> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	
	public void setCompanyL4Service(ViewData<SubCompany> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4SubCompany().getSelectedList());
	}
	
	private ActionService4LinkListener getSupplierLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		Seller fromSeller = new Seller4lLogic().getSellerById(this.getDomain().getFromSellerId());
		listener.getOnceAttributes().put("seller", fromSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		this.setAttr(listener);
		this.getDomain().setVoparam(new CommodityLogic().getViewSetting());
		return listener;
	}
	
	private void setSupplierLink4Service(ViewData<Supplier> viewData) {
		Supplier supplier = new SupplierLogic().getSupplierByLink(this.getDomain().getSubmitNumber());
		supplier.getSnapShot();
		supplier.setUaccept(this.getDomain().getUaccept());
		viewData.setTicketDetails(supplier);
		if ("供应商商品的配置copy to分公司商品的配置".length()>0) {
			SellerViewSetting stSubCompany=new CommodityLogic().getViewSetting(), stSupplier=this.getDomain().getVoparam(SellerViewSetting.class);
			long sellerId=stSubCompany.getSellerId();
			TicketPropertyUtil.copyFieldsSkip(TicketPropertyUtil.deepClone(stSupplier), stSubCompany);
			stSubCompany.setSellerId(sellerId);
			new SellerViewSettingLogic().saveViewSetting(stSubCompany);
		}
	}
	
	public void setImport4Service(ViewData<SubCompany> viewData) {
		viewData.setTicketDetails(this.getImportList());
	}
	
	public List<SubCompany> getImportList() {
		String k = "ImportList";
		List<SubCompany> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<SubCompany>();
			this.setAttr(k, list);
		}
		return list;
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
		List<SubCompany> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.Choose12 logic = new SubCompanyLogic().getPropertyChoosableLogic();
		former.setViewBuilder(logic.getChooseBuilder());
		former.setSellerViewSetting(logic.getChooseSetting( logic.getChooseBuilder() ));
		return former;
	}
	public SelectTicketFormer4Sql<SubCompanyForm, SubCompany> getSelectFormer4SubCompany() {
		String k="SelectFormer4SubCompany";
		SelectTicketFormer4Sql<SubCompanyForm, SubCompany> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<SubCompanyForm, SubCompany>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public List<SubCompany> getSelectedList() {
		return this.selectedList;
	}

	@Override
	public void setSelectedList(List<SubCompany> selected) {
		this.selectedList = selected;
	}
	
	public SubCompanyForm getForm() {
		return this;
	}
	
	public SubCompany getDomain() {
		return domain;
	}

	public void setDomain(SubCompany t) {
		this.domain = t ;
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
	
	private SubCompany getSubCompany() {
		String k="GetSubCompany";
		SubCompany d = this.getAttr(k);
		if (d==null) {
			d = new SubCompany();
			this.setAttr(k, d);
		}
		return d;
	}
	
	private BaseImportForm getBaseImportForm() {
		BaseImportForm form = this.getAttr(BaseImportForm.class);
		if (form==null) {
			form = new BaseImportForm();
			this.setAttr(form);
		}
		return form;
	}
}

package com.haoyong.sales.base.form;

import java.util.ArrayList;
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

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.StorehouseLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;

public class StorehouseForm extends AbstractForm<Storehouse> implements FViewInitable {
	
	private void prepareCreate(){
		TicketPropertyUtil.copyFieldsUnskip(new Storehouse(), this.getDomain());
	}
	private void prepareEdit() {
		TicketPropertyUtil.copyFieldsUnskip(this.getSelectFormer4Storehouse().getFirst(), this.getDomain());
	}
	public void prepareImport() {
		TicketPropertyUtil.copyFieldsUnskip(new Storehouse(), this.getDomain());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	public void validateCommit() {
		StringBuffer sb = validateStorehouse(this.getDomain());
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private StringBuffer validateStorehouse(Storehouse domain) {
		StringBuffer sb = new StringBuffer();
		if(StringUtils.isBlank(domain.getName()))
			sb.append("客户名称不能为空，");
		if (StringUtils.isBlank(domain.getNumber())==false && new StorehouseLogic().hasRepeat(domain, "number", domain.getNumber()))
			sb.append("仓库编号重复").append(domain.getNumber()).append(",");
		return sb;
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
		for (Iterator<Storehouse> iter=getImportList().iterator(); iter.hasNext(); ri++) {
			Storehouse d = iter.next();
			sitem = validateStorehouse(d);
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true)
			new StorehouseLogic().getPropertyChoosableLogic().chooseViewBuilder(viewBuilder);
	}
	
	private List<Storehouse> getSelectedList() {
		String k = "FormSelectedList";
		List<Storehouse> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<Storehouse>();
			this.setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<Storehouse> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	private void setStorehouseA4Service(ViewData<Storehouse> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	private void setStorehouseL4Service(ViewData<Storehouse> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Storehouse().getSelectedList());
	}
	private void setImportL4Service(ViewData<Storehouse> viewData) {
		viewData.setTicketDetails(this.getImportList());
	}
	
	public List<Storehouse> getImportList() {
		String k = "ImportList";
		List<Storehouse> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Storehouse>();
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
		List<Storehouse> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.Choose12 logic = new StorehouseLogic().getPropertyChoosableLogic();
		former.setViewBuilder(logic.getChooseBuilder());
		former.setSellerViewSetting(logic.getChooseSetting( logic.getChooseBuilder() ));
		return former;
	}
	public SelectTicketFormer4Sql<StorehouseForm, Storehouse> getSelectFormer4Storehouse() {
		String k="SelectFormer4Storehouse";
		SelectTicketFormer4Sql<StorehouseForm, Storehouse> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<StorehouseForm, Storehouse>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public Storehouse getDomain() {
		String k = "FormDomain";
		Storehouse d = this.getAttr(k);
		if (d==null) {
			d = new Storehouse();
			this.setAttr(k, d);
		}
		return d;
	}

	private Storehouse getStorehouse() {
		String k="FormStorehouse";
		Storehouse d = this.getAttr(k);
		if (d==null) {
			d = new Storehouse();
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

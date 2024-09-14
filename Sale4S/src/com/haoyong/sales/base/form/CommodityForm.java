package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.form.SelectTicketFormer4Edit;
import net.sf.mily.support.throwable.FormException;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DoubleType;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.CType;
import com.haoyong.sales.base.domain.CType2;
import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.logic.CType2Logic;
import com.haoyong.sales.base.logic.CTypeLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.util.TicketUtil;

/**
 * 界面管理——商品
 */
public class CommodityForm extends AbstractForm<Commodity> implements FViewInitable {

	private List<Commodity> selectedList;
	
	public void canBomCopy(List<List<Object>> valiRows) {
		// commName,uneditable,BDetails
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append(",");
			if (row.get(2)==null)
				sitem.append("请先配置BOM，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void prepareCreate(){
		this.setDomain(new Commodity());
	}
	
	private void prepareCreate4Copyed(){
		Commodity comm = new Commodity();
		TicketPropertyUtil.copyFieldsSkip(this.getSelectFormer4Commodity().getFirst(), comm);
		this.setDomain(comm);
	}
	
	private void prepareEdit() {
		if(selectedList.size()>1){
			throw new FormException("每次只能编辑一种商品");
		}
		this.setDomain(selectedList.get(0));
	}
	
	private void prepareBom() {
		this.setDomain(this.selectedList.get(0));
		BOMForm bomForm = BOMForm.getForm4List(this.getDomain().getBomDetails(), this);
		bomForm.getBomCopy().clear();
		bomForm.getBomCopy().addAll(this.getBomCopy());
		this.setAttr(bomForm);
	}
	
	public void prepareImport() {
		TicketPropertyUtil.copyFieldsSkip(new Commodity(), this.getDomain());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	private void validateCommit() {
		StringBuffer sb = validateCommodity(this.getDomain());
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private StringBuffer validateCommodity(Commodity domain) {
		StringBuffer sb = new StringBuffer();
		if(StringUtils.isBlank(domain.getName()))
			sb.append("商品名称不能为空，");
		if (StringUtils.isBlank(domain.getNumber())==false && !TicketUtil.isValid("bs_commodity", "commNumber", domain, domain.getNumber()))
			sb.append("商品编号重复").append(domain.getNumber()).append(",");
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
			this.getBaseImportForm().setSellerIndexes(listview, inputs, "supplyType", this.getDomain().getSupplyType());
			new SellerViewInputsLogic().saveOrUpdate(inputs);
		}
	}
	
	private void validateImport(Component fcomp) throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (getImportList().size()==0)
			sb.append("导入明细为空，");
		int ri=1;
		for (Iterator<Commodity> iter=getImportList().iterator(); iter.hasNext(); ri++) {
			Commodity d = iter.next();
			sitem = validateCommodity(d);
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (viewBuilder.getName().startsWith("Bom")) {
		} else {
			new CommodityLogic().getPropertyChoosableLogic().chooseViewBuilder(viewBuilder);
		}
		if (viewBuilder.getName().equals("Import"))
			this.getBaseImportForm().setImportBuilderInit(viewBuilder);
	}
	
	private void setCommodityA4Service(ViewData<Commodity> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	
	private void setCommodityL4Service(ViewData<Commodity> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Commodity().getSelectedList());
	}
	
	private void setImport4Service(ViewData<Commodity> viewData) {
		viewData.setTicketDetails(this.getImportList());
	}
	
	private void setBom4Service(ViewData<Commodity> viewData) {
		Commodity commodity = getDomain();
		commodity.setBomDetails(this.getAttr(BOMForm.class).getDetailList());
		viewData.setTicketDetails(commodity);
	}

	public List<Commodity> getSelectedList() {
		return this.selectedList;
	}
	
	public List<Commodity> getDetailList() {
		String k = "DetailList";
		List<Commodity> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Commodity>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<Commodity> getImportList() {
		String k = "ImportList";
		List<Commodity> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Commodity>();
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
		String supplyType = this.getBaseImportForm().getSellerViewInputs().getInputs().get("supplyType");
		this.getDomain().setSupplyType(supplyType);
	}
	
	private void setImportFormated(Component fcomp) throws Exception {
		List<Commodity> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}

	public void setSelectedList(List<Commodity> selected) {
		this.selectedList = selected;
	}

	public Commodity getDomain() {
		Commodity d = this.getAttr(Commodity.class);
		if (d==null) {
			d = new Commodity();
			this.setAttr(d);
		}
		return d;
	}

	public void setDomain(Commodity t) {
		this.setAttr(t);
	}
	
	public void setCommoditySupplyType(){
		for(Commodity commodity:selectedList){
			commodity.setSupplyType(getDomain().getSupplyType());
		}
	}
	
	public void setCommodityCType(){
		for(Commodity commodity:selectedList){
			commodity.setCommType(getDomain().getCommType());
		}
	}
	
	public void setCommodityCType2(){
		for(Commodity commodity:selectedList){
			commodity.setCommType2(getDomain().getCommType2());
		}
	}
	
	public List<SupplyType> getSupplyTypeOptions(Object entity) {
		List<SupplyType> typeList = new SupplyTypeLogic().getTypeList();
		return typeList;
	}
	
	public List<CType> getCTypeList(Commodity entity) {
		List<CType> typeList = new CTypeLogic().getTypeList();
		return typeList;
	}
	
	public List<CType2> getCType2List(Commodity entity) {
		List<CType2> type2List = new CType2Logic().getDomain().getInfoList();
		return type2List;
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		CommodityLogic logic = new CommodityLogic();
		former.setViewBuilder(logic.getPropertyChoosableLogic().getChooseBuilder());
		former.setSellerViewSetting(logic.getViewSetting());
		return former;
	}
	public SelectTicketFormer4Edit<CommodityForm, Commodity> getSelectFormer4Commodity() {
		String k = "SelectFormer4Commodity";
		SelectTicketFormer4Edit former = this.getAttr(k);
		if (former==null) {
			former = new SelectTicketFormer4Edit<CommodityForm, Commodity>(this);
			this.setAttr(k, former);
		}
		return former;
	}
	
	public SupplyTypeForm getSupplyTypeForm() {
		SupplyTypeForm form = getAttr(SupplyTypeForm.class);
		if (form == null) {
			form = new SupplyTypeForm();
			form.beforeList(null);
			setAttr(form);
		}
		return form;
	}
	
	public CTypeForm getCTypeForm() {
		CTypeForm form = getAttr(CTypeForm.class);
		if (form == null) {
			form = new CTypeForm();
			form.beforeList(null);
			setAttr(form);
		}
		return form;
	}
	
	public CType2Form getCType2Form() {
		CType2Form form = getAttr(CType2Form.class);
		if (form == null) {
			form = new CType2Form();
			form.beforeList(null);
			setAttr(form);
		}
		return form;
	}
	
	public YardsForm getYardsForm() {
		YardsForm form = getAttr(YardsForm.class);
		if (form == null) {
			form = new YardsForm();
			form.beforeList(null);
			setAttr(form);
		}
		return form;
	}
	
	public DeliverTypeForm getDeliverTypeForm() {
		DeliverTypeForm form = getAttr(DeliverTypeForm.class);
		if (form == null) {
			form = new DeliverTypeForm();
			form.beforeList(null);
			setAttr(form);
		}
		return form;
	}
	
	public BOMForm getBOMForm() {
		return this.getAttr(BOMForm.class);
	}
	
	public void setBDetailsLink(Hyperlink link, Object bdetails, ArrayList<Object> row, int rowi) {
		Commodity comm = new Commodity();
		comm.setBDetails((String)bdetails);
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.NotOccupySizable);
		DoubleType type = new DoubleType();
		for (BomDetail d: comm.getBomDetails()) {
			StringBuffer sb = new StringBuffer();
			for (int i=d.getLevel(); i-->1; sb.append("\t\t"));
			sb.append(d.getVoparam(CommodityT.class).getCommName()).append("\t");
			sb.append(type.format(d.getBomTicket().getAunit()));
			grid.append(new Hyperlink(null, sb.toString()));
		}
		link.add(grid);
	}
	
	private List<BomDetail> getBomCopy() {
		String k="BomCopyList";
		List<BomDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public void setBomCopy() {
		List<BomDetail> list = this.getSelectFormer4Commodity().getFirst().getBomDetails();
		getBomCopy().clear();
		getBomCopy().addAll(list);
	}
}

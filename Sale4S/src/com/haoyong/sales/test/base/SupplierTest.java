package com.haoyong.sales.test.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.SupplierForm;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.util.SSaleUtil;

public class SupplierTest extends AbstractTest<SupplierForm> {
	
	public SupplierTest() {
		this.setForm(new SupplierForm());
	}
	
	private void check导入供应商(String sDomains, String sIndexs) {
		this.getForm().prepareImport();
		this.loadView("Import");
		if (sIndexs != null) {
			ViewBuilder viewBuilder = this.getEditListView().getViewBuilder();
			LinkedHashMap<String, String> mapFieldIndex = new LinkedHashMap<String, String>(); 
			for (Iterator fiter=this.getEditListView().getViewBuilder().getFieldBuilderLeafs().iterator(), citer=Arrays.asList(sIndexs.split("\\t")).iterator(); fiter.hasNext() && citer.hasNext();) {
				FieldBuilder builder = (FieldBuilder)fiter.next();
				if (builder.getClass() != TextFieldBuilder.class)
					continue;
				String index = (String)citer.next();
				StringBuffer name = new StringBuffer();
				for (FieldBuilder cur=builder; StringUtils.equals(cur.getName(),viewBuilder.getName())==false; cur=cur.getViewBuilder()) {
					name.insert(0, cur.getName().concat("."));
				}
				if (StringUtils.isBlank(index)==false)
					mapFieldIndex.put(name.deleteCharAt(name.length()-1).toString(), index);
			}
			BaseImportForm imform = this.getForm().getFormProperty("attrMap.BaseImportForm");
			SellerViewInputs inputs = imform.getFormProperty("attrMap.SellerViewInputs");
			inputs.getInputs().clear();
			inputs.getInputs().putAll(mapFieldIndex);
			imform.getSellerIndexes(this.getEditListView().getComponent());
			this.getForm().setFormProperty("attrMap.FieldIndex", mapFieldIndex);
		}
		this.getForm().getDomain().getVoParamMap().put("Remark", sDomains);
		this.onMenu("导入格式化");
		this.onMenu("导入提交");
		if (sIndexs!=null) {
			this.loadView("Import");
			SellerViewInputs inputs = this.getForm().getFormProperty("attrMap.BaseImportForm.attrMap.SellerViewInputs");
			LinkedHashMap<String, String> mapFieldIndex = (LinkedHashMap<String, String>)this.getForm().getFormProperty("attrMap.FieldIndex");
			Assert.assertTrue("有保存列序号，能加载出来", inputs.getId()>0 && inputs.getInputs().keySet().containsAll(mapFieldIndex.keySet()) && inputs.getInputs().values().containsAll(mapFieldIndex.values()));
		}
		if (true) {
			List<Supplier> importList = this.getForm().getImportList();
			Supplier domain = importList.get(importList.size()-1);
			Assert.assertTrue("导入供应商有保存", domain.getId()>0);
		}
	}
	
	public void test导入供应商() {
		this.setQ清空();
		if ("列序号记忆保存".length()>0) {
			StringBuffer sIndexs = new StringBuffer();
			for (int i=0; i<10; i++, sIndexs.append(i).append("\t"));
			this.check导入供应商(sIndexs.toString(), sIndexs.deleteCharAt(sIndexs.length()-1).toString());
		}
	}

	private void test供应商() {
		SupplierForm form = this.getForm();
		if ("新增".length()>0) {
			this.loadView("List");
			this.onMenu("新增");;
			Supplier domain = form.getDomain();
			domain.setName("测供应商");
			domain.setNumber("TSupplier");
			domain.setLinker("供应商人");
			domain.setLinkerCall("供应商电话");
			domain.setChuanzhen("供应商传真号");
			domain.setAddress("供应商地址");
			domain.setRemark("供应商备注");
			this.onMenu("提交");;
			Assert.assertTrue("供应商新增失败", domain.getId()>0);
		}
		if ("编辑".length()>0) {
			SqlListBuilder sqlBuilder = this.setFilters("number", "TSupplier");
			this.setSqlListSelect(1);
			this.onMenu("编辑");;
			form.getDomain().setName("测供应商已编辑");
			this.onMenu("提交");;
			form.getSearchSetting(sqlBuilder);
		}
		if ("删除".length()>0) {
			this.setFilters("number", "TSupplier");
			this.setSqlListSelect(1);
			this.onMenu("删除");;
			Assert.assertTrue("供应商删除失败", form.getSelectedList().get(0).getId()<0);
		}
	}
	
	private void test链接商家() {
		if ("南宁商家Supplier,吉高商家Client".length()>0) {
			if ("清空原链接商家".length()>0) {
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				TicketPropertyUtil.copyFieldsSkip(new CommodityLogic().getViewSetting(), new SellerViewSetting());
				this.setQ清空();
			}
			new SupplierTest().get南宁古城伙伴();
		}
		if ("南宁商家Supplier,吉高商家SubCompany".length()>0) {
			if ("清空原链接商家".length()>0) {
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				TicketPropertyUtil.copyFieldsSkip(new CommodityLogic().getViewSetting(), new SellerViewSetting());
				this.setQ清空();
			}
			new SupplierTest().get南宁古城总部();
		}
	}

	public void setSellerViewTrunk(String... trunkList0) {
		List<String> trunkList = Arrays.asList(trunkList0);
		SellerViewSetting vs = new SupplierLogic().getViewSetting();
		vs.setTitleList(new Supplier().getTrunkDefault());
		vs.setTrunkList(trunkList);
		LinkedHashSet chooseList = new LinkedHashSet<String>(vs.getChooseList());
		chooseList.addAll(trunkList);
		vs.setChooseList(new ArrayList<String>(chooseList));
		new SellerViewSettingLogic().saveViewSetting(vs);
	}
	
	public Supplier get永晋() {
		if ("已经存在数据库，则取出".length()>0) {
			Supplier supplier = new SupplierLogic().getSupplierByNumber("YJ");
			if (supplier!=null)
				return supplier;
		}
		if ("不存在则生成".length()>0) {
			SupplierForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("List");
				this.onMenu("新增");;
				Supplier domain = form.getDomain();
				domain.setName("永晋电瓷苏州有限公司");
				domain.setNumber("YJ");
				domain.setLinker("施Linker");
				domain.setLinkerCall("0512-Call");
				domain.setChuanzhen("0512-Chuanzhen");
				domain.setAddress("江苏省苏州市吴江区庞东路500号");
				domain.setRemark("供应商备注");
				this.onMenu("提交");;
				Assert.assertTrue("供应商永晋新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("供应商永晋新增失败");
		return null;
	}
	
	public Supplier get浙江() {
		if ("已经存在数据库，则取出".length()>0) {
			Supplier supplier = new SupplierLogic().getSupplierByNumber("ZJ");
			if (supplier!=null)
				return supplier;
		}
		if ("不存在则生成".length()>0) {
			SupplierForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("List");
				this.onMenu("新增");;
				Supplier domain = form.getDomain();
				domain.setName("浙江电瓷苏州有限公司");
				domain.setNumber("ZJ");
				domain.setLinker("施Linker");
				domain.setLinkerCall("0573-Call");
				domain.setChuanzhen("0573-Chuanzhen");
				domain.setAddress("浙江杭州市吴江区庞东路500号");
				domain.setRemark("供应商备注");
				this.onMenu("提交");;
				Assert.assertTrue("供应商浙江新增失败", domain.getId()>0);
				return domain;
			}
		}
		return null;
	}
	
	public Supplier get南宁古城伙伴() {
		SupplierForm form = this.getForm();
		Supplier supplier = new SupplierLogic().getSupplierByNumber("NNHB");
		if ("新增".length()>0 && supplier==null) {
			this.loadView("List");
			this.onMenu("新增");;
			Supplier domain = form.getDomain();
			domain.setName("南宁古城伙伴有限公司");
			domain.setNumber("NNHB");
			domain.setLinker("伙伴人");
			domain.setLinkerCall("0512-伙伴电话");
			domain.setChuanzhen("0512-6346Chuan");
			domain.setAddress("江苏省苏州市吴江区庞东路500号");
			domain.setRemark("供应商备注");
			this.onMenu("提交");
		}
		if (true) {
			this.loadView("List");
			this.setFilters("name", "南宁古城伙伴有限公司");
			this.setSqlAllSelect(this.getListViewValue().size());
			this.onMenu("申请商家链接");
			form.getDomain().setSubmitNumber("NanNingPartner");
			form.getDomain().setToSellerName("南宁古城");
			this.setRadioGroup("客户订单");
			SellerViewSetting vSource=null, vTarget=null;
			try {
				this.onMenu("提交");
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				new ClientTest().get吉高电子客户();
				vSource = new CommodityLogic().getViewSetting();
			} finally {
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				vTarget = new CommodityLogic().getViewSetting();
				Assert.assertTrue("吉高客户商品属性配置与南宁供应商，应一致", vTarget.getId()>0 && vSource.getSellerId()!=vTarget.getSellerId()
						&& StringUtils.equals(vSource.getTrunk(), vTarget.getTrunk()) && StringUtils.equals(vSource.getChoose(), vTarget.getChoose()));
			}
		}
		return form.getDomain();
	}
	
	public Supplier get南宁古城总部() {
		SupplierForm form = this.getForm();
		Supplier supplier = new SupplierLogic().getSupplierByNumber("NNZB");
		if ("新增".length()>0 && supplier==null) {
			this.loadView("List");
			this.onMenu("新增");;
			Supplier domain = form.getDomain();
			domain.setName("南宁古城总部有限公司");
			domain.setNumber("NNZB");
			domain.setLinker("总部人");
			domain.setLinkerCall("0512-总部电话");
			domain.setChuanzhen("0512-6346Chuan");
			domain.setAddress("江苏省苏州市吴江区庞东路500号");
			domain.setRemark("供应商备注");
			this.onMenu("提交");
		}
		if (true) {
			this.loadView("List");
			this.setFilters("name", "南宁古城总部有限公司");
			this.setSqlAllSelect(this.getListViewValue().size());
			this.onMenu("申请商家链接");
			form.getDomain().setSubmitNumber("NanNingZongBu");
			form.getDomain().setToSellerName("南宁古城");
			this.setRadioGroup("分公司订单");
			SellerViewSetting vSource=null, vTarget=null;
			try {
				this.onMenu("提交");
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				new SubCompanyTest().get吉高分公司();
				vSource = new CommodityLogic().getViewSetting();
			} finally {
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				vTarget = new CommodityLogic().getViewSetting();
				Assert.assertTrue("吉高分公司商品属性配置与南宁总部一致", vTarget.getId()>0 && vSource.getSellerId()!=vTarget.getSellerId()
						&& StringUtils.equals(vSource.getTrunk(), vTarget.getTrunk()) && StringUtils.equals(vSource.getChoose(), vTarget.getChoose()));
			}
		}
		return form.getDomain();
	}
	
	public void setQ清空() {
		String sql = "delete from bs_company where dtype=1 and sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
	}
}

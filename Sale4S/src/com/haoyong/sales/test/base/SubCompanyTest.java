package com.haoyong.sales.test.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.SubCompanyForm;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.util.SSaleUtil;

public class SubCompanyTest extends AbstractTest<SubCompanyForm> {
	
	public SubCompanyTest() {
		this.setForm(new SubCompanyForm());
	}
	
	private void check导入分公司(String sDomains, String sIndexs) {
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
			List<SubCompany> importList = this.getForm().getImportList();
			SubCompany domain = importList.get(importList.size()-1);
			Assert.assertTrue("导入分公司有保存", domain.getId()>0);
		}
	}
	
	public void test导入分公司() {
		this.setQ清空();
		if ("列序号记忆保存".length()>0) {
			StringBuffer sIndexs = new StringBuffer();
			for (int i=0; i<10; i++, sIndexs.append(i).append("\t"));
			this.check导入分公司(sIndexs.toString(), sIndexs.deleteCharAt(sIndexs.length()-1).toString());
		}
	}

	public void test分公司() {
		SubCompanyForm form = this.getForm();
		if ("新增".length()>0) {
			this.loadView("List");
			this.onMenu("新增");
			SubCompany domain = form.getDomain();
			domain.setName("测分公司");
			domain.setNumber("TSubCompany");
			domain.setLinker("分公司人");
			domain.setLinkerCall("分公司电话");
			domain.setChuanzhen("分公司传真号");
			domain.setAddress("分公司地址");
			domain.setRemark("分公司备注");
			this.onMenu("提交");
			Assert.assertTrue("分公司新增失败", domain.getId()>0);
		}
		if ("再编辑".length()>0) {
			this.setFilters("number", "TSubCompany");
			this.setSqlListSelect(1);
			this.onMenu("编辑");
			form.getDomain().setName("测分公司已编辑");
			this.onMenu("提交");
		}
		if ("删除".length()>0) {
			this.setFilters("number", "TSubCompany");
			this.setSqlListSelect(1);
			this.onMenu("删除");
			Assert.assertTrue("分公司删除失败", form.getSelectFormer4SubCompany().getFirst().getId()<0);
		}
	}
	public void setSellerViewTrunk(String... trunkList0) {
		List<String> trunkList = Arrays.asList(trunkList0);
		SellerViewSetting vs = new SubCompanyLogic().getViewSetting();
		vs.setTitleList(new SubCompany().getTrunkDefault());
		vs.setTrunkList(trunkList);
		LinkedHashSet chooseList = new LinkedHashSet<String>(vs.getChooseList());
		chooseList.addAll(trunkList);
		vs.setChooseList(new ArrayList<String>(chooseList));
		new SellerViewSettingLogic().saveViewSetting(vs);
	}

	public SubCompany get湖南() {
		if ("已经存在数据库，则取出".length()>0) {
			SubCompany company = new SubCompanyLogic().getSubCompanyByNumber("HN");
			if (company!=null)
				return company;
		}
		if ("不存在则生成".length()>0) {
			SubCompanyForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("List");
				this.onMenu("新增");
				SubCompany domain = form.getDomain();
				domain.setName("湖南分公司");
				domain.setNumber("HN");
				domain.setLinker("张Linker");
				domain.setLinkerCall("0512-LinkerCall");
				domain.setChuanzhen("0512-Chuanzhen");
				domain.setAddress("湖南分公司株洲中山大道108号");
				domain.setRemark("分公司备注");
				this.onMenu("提交");
				Assert.assertTrue("湖南分公司新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("分公司新增失败");
		return null;
	}

	public SubCompany get吉高分公司() {
		SubCompanyForm form = this.getForm();
		SubCompany company = new SubCompanyLogic().getSubCompanyByNumber("JGFGS");
		if ("新增".length()>0 && company==null) {
			this.loadView("List");
			this.onMenu("新增");
			SubCompany domain = form.getDomain();
			domain.setName("吉高分公司");
			domain.setNumber("JGFGS");
			domain.setLinker("分公司人");
			domain.setLinkerCall("0512-分公司电话");
			domain.setChuanzhen("0512-Chuanzhen");
			domain.setAddress("吉高分公司株洲中山大道108号");
			domain.setRemark("分公司备注");
			this.onMenu("提交");
		}
		if (true) {
			this.loadView("List");
			this.setFilters("name", "吉高分公司");
			this.setSqlListSelect(1);
			this.onMenu("受理商家链接");
			form.getDomain().setFromSellerName("吉高电子");
			form.getDomain().setSubmitNumber("NanNingZongBu");
			this.onMenu("验证商家授权码");
			this.onMenu("提交");
			Assert.assertTrue("吉高分公司新增失败", form.getDomain().getId()>0);
		}
		return form.getDomain();
	}
	
	protected void setQ清空() {
		String sql = "delete from bs_company where dtype=3 and sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
	}
}

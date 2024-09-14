package com.haoyong.sales.test.base;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.StorehouseForm;
import com.haoyong.sales.base.logic.StorehouseLogic;
import com.haoyong.sales.util.SSaleUtil;

public class StorehouseTest extends AbstractTest<StorehouseForm> {
	
	public StorehouseTest() {
		this.setForm(new StorehouseForm());
	}
	
	private void check导入仓库(String sDomains, String sIndexs) {
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
			List<Storehouse> importList = this.getForm().getImportList();
			Storehouse domain = importList.get(importList.size()-1);
			Assert.assertTrue("导入仓库有保存", domain.getId()>0);
		}
	}
	
	public void test导入仓库() {
		this.setQ清空();
		if ("列序号记忆保存".length()>0) {
			StringBuffer sIndexs = new StringBuffer();
			for (int i=0; i<10; i++, sIndexs.append(i).append("\t"));
			this.check导入仓库(sIndexs.toString(), sIndexs.deleteCharAt(sIndexs.length()-1).toString());
		}
	}

	private void test仓库() {
		StorehouseForm form = this.getForm();
		if ("新增".length()>0) {
			this.loadView("List");
			this.onMenu("新增");
			Storehouse domain = form.getDomain();
			domain.setName("测仓库");
			domain.setNumber("TStorehouse");
			domain.setLinker("仓库人");
			domain.setLinkerCall("仓库电话");
			domain.setChuanzhen("仓库传真号");
			domain.setAddress("仓库地址");
			domain.setRemark("仓库备注");
			this.onMenu("提交");
			Assert.assertTrue("仓库新增失败", domain.getId()>0);
		}
		if ("再编辑".length()>0) {
			this.setFilters("name", "测仓库");
			this.setSqlListSelect(1);
			this.onMenu("编辑");
			form.getDomain().setName("测仓库已编辑");
			this.onMenu("提交");
		}
		if ("删除".length()>0) {
			this.setFilters("name", "测仓库已编辑");
			this.setSqlListSelect(1);
			this.onMenu("删除");
			Assert.assertTrue("仓库删除失败", form.getSelectFormer4Storehouse().getFirst().getId()<0);
		}
	}
	
	public Storehouse get验货大仓() {
		if ("已经存在数据库，则取出".length()>0) {
			Storehouse Storehouse = new StorehouseLogic().getStorehouseByNumber("Da");
			if (Storehouse!=null)
				return Storehouse;
		}
		if ("不存在则生成".length()>0) {
			StorehouseForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("List");
				this.onMenu("新增");
				Storehouse domain = form.getDomain();
				domain.setName("大仓");
				domain.setStoreCheck(true);
				domain.setNumber("Da");
				domain.setLinker("张Linker");
				domain.setLinkerCall("0512-LinkerCall");
				domain.setChuanzhen("0512-Chuanzhen");
				domain.setAddress("物流区松陵镇经济开发区柳胥路430号");
				domain.setRemark("仓库备注");
				this.onMenu("提交");
				Assert.assertTrue("仓库大仓新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("仓库大仓新增失败");
		return null;
	}
	
	public Storehouse get小仓() {
		if ("已经存在数据库，则取出".length()>0) {
			Storehouse Storehouse = new StorehouseLogic().getStorehouseByNumber("Xiao");
			if (Storehouse!=null)
				return Storehouse;
		}
		if ("不存在则生成".length()>0) {
			StorehouseForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("List");
				this.onMenu("新增");
				Storehouse domain = form.getDomain();
				domain.setName("小仓");
				domain.setStoreCheck(false);
				domain.setNumber("Xiao");
				domain.setLinker("张Linker");
				domain.setLinkerCall("0512-LinkerCall");
				domain.setChuanzhen("0512-Chuanzhen");
				domain.setAddress("物流区松陵镇经济开发区柳胥路430号");
				domain.setRemark("仓库备注");
				this.onMenu("提交");
				Assert.assertTrue("仓库小仓新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("仓库小仓新增失败");
		return null;
	}
	
	public void setQ清空() {
		String sql = "delete from bs_company where dtype=4 and sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
	}
}

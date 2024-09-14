package com.haoyong.sales.test.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.form.BOMForm;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.CommodityForm;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.util.SSaleUtil;

public class CommodityTest extends AbstractTest<CommodityForm> {
	
	public CommodityTest() {
		this.setForm(new CommodityForm());
	}
	
	private void check导入商品(String supplyType, String sDomains, String sIndexs) {
		this.getForm().prepareImport();
		this.loadView("Import");
		this.setFieldText("supplyType", supplyType);
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
			Assert.assertTrue("商品类型有保存", StringUtils.equals(this.getForm().getDomain().getSupplyType(), supplyType));
			Assert.assertTrue("有保存列序号，能加载出来", inputs.getId()>0 && inputs.getInputs().keySet().containsAll(mapFieldIndex.keySet()) && inputs.getInputs().values().containsAll(mapFieldIndex.values()));
		}
		if (true) {
			List<Commodity> importList = this.getForm().getImportList();
			Commodity domain = importList.get(importList.size()-1);
			Assert.assertTrue("导入商品有保存", domain.getId()>0);
		}
	}
	
	public void test商品大类() {
		this.loadView("CType");
		int counti = this.getListViewValue().size()+1;
		this.onMenu("新增");
		this.setFieldText("name", "大类"+new Date());
		this.onMenu("提交");
		Assert.assertTrue("新增商品大类", this.getListViewValue().size()==counti);
		
		this.setEditListSelect(counti);
		this.onMenu("编辑");
		this.setFieldText("name", "大类"+new Date());
		this.onMenu("提交");
		Assert.assertTrue("编辑商品大类", this.getListViewValue().size()==counti);
		
		this.setEditListSelect(counti);
		this.onMenu("删除");
		Assert.assertTrue("删除商品大类", this.getListViewValue().size()==counti-1);
	}
	
	public void test商品小类() {
		this.loadView("CType2");
		int counti = this.getListViewValue().size()+1;
		this.onMenu("新增");
		this.setFieldText("name", "小类"+new Date());
		this.onMenu("提交");
		Assert.assertTrue("新增商品小类", this.getListViewValue().size()==counti);
		
		this.setEditListSelect(counti);
		this.onMenu("编辑");
		this.setFieldText("name", "小类"+new Date());
		this.onMenu("提交");
		Assert.assertTrue("编辑商品小类", this.getListViewValue().size()==counti);
		
		this.setEditListSelect(counti);
		this.onMenu("删除");
		Assert.assertTrue("删除商品小类", this.getListViewValue().size()==counti-1);
	}
	
	public void test导入商品() {
		this.setQ清空();
		if ("列序号记忆保存".length()>0) {
			StringBuffer sIndexs = new StringBuffer();
			for (int i=0; i<10; i++, sIndexs.append(i).append("\t"));
			this.check导入商品("采购", sIndexs.toString(), sIndexs.deleteCharAt(sIndexs.length()-1).toString());
		}
	}

	private void test采购商品() {
		CommodityForm form = getForm();
		if ("新增".length()>0) {
			this.loadView("PurchaseList");
			if ("已经存在则先删除".length()>0) {
				this.setFilters("commNumber", "TCommodityPurchase");
				int dsize = this.getListViewValue().size();
				if (dsize>0) {
					this.setSqlListSelect(dsize);
					this.onMenu("删除");
				}
			}
			this.onMenu("新增");
			Commodity domain = form.getDomain();
			domain.setName("测采购商品");
			domain.setCommNumber("TCommodityPurchase");
			domain.setModel("采购商品型号");
			domain.setFactory("供应商工厂");
			domain.setUnit("采购商品单位");
			domain.setSupplyType(new SupplyTypeLogic().getPurchaseType());
			domain.setCommType("采购大类");
			domain.setRemark("采购商品备注");
			domain.setMinInventory(1000);
			this.onMenu("提交");
			Assert.assertTrue("采购商品新增失败", domain.getId()>0);
		}
		if ("复制新增".length()>0) {
			this.setFilters("commNumber", "TCommodityPurchase");
			this.setSqlListSelect(1);
			this.onMenu("复制新增");
			Commodity domain = form.getDomain();
			domain.setName("测采购商品1");
			domain.setCommNumber("TCommodityPurchase4Delete");
			domain.setModel("采购商品型号");
			domain.setFactory("供应商工厂");
			domain.setUnit("采购商品单位");
			domain.setSupplyType(new SupplyTypeLogic().getPurchaseType());
			domain.setCommType("采购大类");
			domain.setRemark("采购商品备注");
			domain.setMinInventory(1000);
			this.onMenu("提交");
			Assert.assertTrue("采购商品复制新增失败", domain.getId()>0);
		}
		if ("删除".length()>0) {
			this.setFilters("commNumber", "TCommodityPurchase4Delete");
			this.setSqlListSelect(1);
			this.onMenu("删除");
			Assert.assertTrue("复制新增采购商品删除失败", form.getSelectFormer4Commodity().getFirst().getId()<0);
		}
		if ("再编辑".length()>0) {
			this.setFilters("commNumber", "TCommodityPurchase");
			this.setSqlListSelect(1);
			this.onMenu("编辑");
			form.getDomain().setName("测采购商品已编辑");
			this.onMenu("提交");
		}
		if ("删除".length()>0) {
			this.setFilters("commNumber", "TCommodityPurchase");
			this.setSqlListSelect(1);
			this.onMenu("删除");
			Assert.assertTrue("采购商品删除失败", form.getSelectFormer4Commodity().getFirst().getId()<0);
		}
	}
	
	private void temp() {
		this.setQ清空();
		getCommodity("CP01018002");
	}
	
	public void setSellerViewTrunk(List<String> trunkList1, List<String> chooseList1, List<String> titleList, List<String> requireList, LinkedHashMap<String, String> renameList) {
		if (chooseList1==null)
			chooseList1 = new ArrayList<String>();
		SellerViewSetting vs = new CommodityLogic().getViewSetting();
		vs.setTitleList(new Commodity().getTrunkDefault());
		vs.setTrunkList(trunkList1);
		LinkedHashSet chooseList = new LinkedHashSet<String>();
		chooseList.addAll(trunkList1);
		chooseList.addAll(chooseList1);
		vs.setChooseList(new ArrayList<String>(chooseList));
		vs.setTitleList(titleList);
		vs.setRequireList(requireList);
		vs.setRenameMap(renameList);
		new SellerViewSettingLogic().saveViewSetting(vs);
	}
	
	public void setSellerMaterialTrunk(List<String> trunkList1, List<String> chooseList1, List<String> titleList, List<String> requireList, LinkedHashMap<String, String> renameList) {
		if (chooseList1==null)
			chooseList1 = new ArrayList<String>();
		SellerViewSetting vs = new CommodityLogic().getViewSetting_Material();
		vs.setTitleList(new Commodity().getTrunkDefault());
		vs.setTrunkList(trunkList1);
		LinkedHashSet chooseList = new LinkedHashSet<String>();
		chooseList.addAll(trunkList1);
		chooseList.addAll(chooseList1);
		vs.setChooseList(new ArrayList<String>(chooseList));
		vs.setTitleList(titleList);
		vs.setRequireList(requireList);
		vs.setRenameMap(renameList);
		new SellerViewSettingLogic().saveViewSetting(vs);
	}
	
	public Commodity getCommodity(String number) {
		LinkedHashSet<String> numbers = (LinkedHashSet<String>)WindowMonitor.getMonitor().getAttribute("Commodity商品编码Numbers");
		if (numbers==null) {
			numbers = new LinkedHashSet<String>();
			WindowMonitor.getMonitor().addAttribute("Commodity生产商品Numbers", numbers);
			
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "PC03015001", "name", "御廷膜方面膜六款共用收缩膜", "model", "25ml", "unit", "张"}).getCommNumber()); 
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "PC04001001", "name", "御廷膜方红豆水润冰清面膜纸盒", "model", "25g", "unit", "个"}).getCommNumber()); 
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "PC04001002", "name", "御廷膜方红豆水润冰清面膜铝袋", "model", "25g", "unit", "个"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "PC04001005", "name", "御廷膜方红豆绿豆两款共用（黑）黑布+珠光膜", "model", "25g", "unit", "片"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLF073", "name", "YLF073", "model", " 1*25"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLF043", "name", "YLF043", "model", " 1*22"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLF045", "name", "YLF045", "model", " 1*22"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLE016", "name", "YLE016", "model", " 1*250"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLE022", "name", "YLE022", "model", " 1*20"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLF034", "name", "YLF034", "model", " 1*25"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLE026", "name", "YLE026", "model", " 1*200"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLF001", "name", "YLF001", "model", " 1*232"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLE042", "name", "YLE042", "model", " 1*1"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLH002", "name", "YLH002", "model", " 1*25"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLE021", "name", "YLE021", "model", " 1*20"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLF092", "name", "YLF092", "model", " 1*1"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLD057", "name", "YLD057", "model", " 1*25", "unit", "kg"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLF021", "name", "YLF021", "model", " 1*60"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLH023", "name", "E1003"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLE183", "name", "V400叶酸", "unit", "KG"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "YLE142", "name", "YLE142", "model", " 1*20"}).getCommNumber());
			numbers.add(this.getC采购商品(new Object[]{"commNumber", "PC04001014", "name", "御廷膜方共用纸箱", "model", " 25ml*72", "unit", "个"}).getCommNumber());

			numbers.add(this.getS生产商品(new Object[]{"commNumber", "CP01018002", "name", "红豆水润冰清面膜", "model", "25ml*5", "unit", "盒"},
					new Object[]{"PC03015001", 73, "PC04001001", 73, "PC04001002", 360, "PC04001005", 360, "PC04001014", 1, "BCP05018001", 9},
					new Object[]{"commNumber", "BCP05018001", "name", "红豆水润冰清面膜料体", "unit", "公斤", "supplyType", "生产"},
					new Object[]{"YLF073", 0.1215, "YLF043", 0.081, "YLF045", 0.0324, "YLE016", 2.43, "YLE022", 0.81, "YLF034", 0.0648, "YLE026", 2.43, "YLF001", 0.0891, "YLE042", 0.0324, "YLH002", 0.1053, "YLE021", 0.81, "YLF092", 0.081, "YLD057", 0.00081, "YLF021", 0.00405, "YLH023", 0.324, "YLE183", 0.081, "YLE142", 1.62}		
					).getCommNumber());
		}
		return new CommodityLogic().getCommodityByNumber(number);
	}
	
	private Commodity getC采购商品(Object... properties) {
 		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		for (int i=0, size=properties.length; i<size; i+=2) {
			map.put((String)properties[i], properties[i+1]);
		}
		if ("已经存在数据库，则取出".length()>0) {
			Commodity commodity = new CommodityLogic().getCommodityByNumber((String)map.get("commNumber"));
			if (commodity!=null)
				return commodity;
		}
		if ("不存在则生成，新增".length()>0) {
			this.loadView("PurchaseList");
			this.onMenu("新增");
			Commodity domain = this.getForm().getDomain();
			for (Map.Entry<String, Object> entry: map.entrySet()) {
				ReflectHelper.setPropertyValue(domain, entry.getKey(), entry.getValue());
			}
			domain.setSupplyType(new SupplyTypeLogic().getPurchaseType());
			this.onMenu("提交");
			return domain;
		}
		return null;
	}
	
	private Commodity getS生产商品(Object[]... commProps_BomItems) {
 		LinkedHashMap<String, Object> mapProps = new LinkedHashMap<String, Object>();
 		int pi=0;
 		if ("生产商品属性".length()>0) {
	 		Object[] commProps = commProps_BomItems[pi++];
			for (int i=0, size=commProps.length; i<size; i+=2) {
				mapProps.put((String)commProps[i], commProps[i+1]);
			}
 		}
		if ("已经存在数据库，则取出".length()>0) {
			Commodity commodity = new CommodityLogic().getCommodityByNumber((String)mapProps.get("commNumber"));
			if (commodity!=null)
				return commodity;
		}
		if ("不存在则生成，新增".length()>0) {
			this.loadView("ProductList");
			this.onMenu("新增");
			Commodity domain = this.getForm().getDomain();
			for (Map.Entry<String, Object> entry: mapProps.entrySet()) {
				ReflectHelper.setPropertyValue(domain, entry.getKey(), entry.getValue());
			}
			domain.setSupplyType(new SupplyTypeLogic().getProductType());
			this.onMenu("提交");
		}
		if ("设置BOM表".length()>0 && pi<=commProps_BomItems.length) {
			this.setFilters("commNumber", mapProps.get("commNumber"));
			this.setSqlListSelect(1);
			this.onMenu("设置BOM表");
			BOMForm bomForm = this.getForm().getBOMForm();
			bomForm.getDomain().setLevel(10);
			Object[] bomItems = commProps_BomItems[pi++];
			for (int bi=0, bsize=bomItems.length, br=0; bi<bsize; bi+=2, br++) {
				this.onMenu("新增同级");
				BomDetail bomDetail = bomForm.getDetailList().get(br);
				Assert.assertTrue("空白BOM明细", bomDetail.getCommodity().getCommNumber()==null);
				this.setRowFieldText(bomDetail, "commNumber", bomItems[bi]);
				this.setRowFieldText(bomDetail, "aunit", bomItems[bi+1]);
				bomDetail.setArrange(new ArrangeTypeLogic().getNormal());
			}
		}
		for (; "设置下一级BOM表".length()>0 && pi<commProps_BomItems.length; ) {
			BOMForm bomForm = this.getForm().getBOMForm();
			Object[] commProps = commProps_BomItems[pi++];
			for (int prti=0, prtsize=bomForm.getDetailList().size(); prti<prtsize; prti++) {
				BomDetail bomPrt = bomForm.getDetailList().get(prti);
				if (StringUtils.equals(bomPrt.getCommodity().getCommNumber(), (String)commProps[1])) {
					for (int i=0, size=commProps.length; i<size; i+=2) {
						this.setRowFieldText(bomPrt, (String)commProps[i], commProps[i+1]);
					}
					Object[] bomItems = commProps_BomItems[pi++];
					this.setEditListSelect(++prti);
					this.onMenu("新增子级");
					for (int bi=0,br=0,bsize=bomItems.length; bi<bsize; bi+=2,br++) {
						if (br>0) {
							this.setEditListSelect(prti+br);
							this.onMenu("新增同级");
						}
						BomDetail bomDetail = bomForm.getDetailList().get(prti+br);
						Assert.assertTrue("空白BOM明细", bomDetail.getCommodity().getCommNumber()==null);
						this.setRowFieldText(bomDetail, "commNumber", bomItems[bi]);
						this.setRowFieldText(bomDetail, "aunit", bomItems[bi+1]);
						bomDetail.setArrange(new ArrangeTypeLogic().getNormal());
					}
					break;
				}
			}
		}
		this.onMenu("提交");
		return this.getForm().getDomain();
	}

	public Commodity getC白棒() {
		if ("已经存在数据库，则取出".length()>0) {
			Commodity commodity = new CommodityLogic().getCommodityByNumber("TPurchaseBB");
			if (commodity!=null)
				return commodity;
		}
		if ("不存在则生成，新增".length()>0) {
			CommodityForm form = this.getForm();
			this.loadView("PurchaseList");
			this.onMenu("新增");
			Commodity domain = form.getDomain();
			domain.setName("白棒");
			domain.setCommNumber("TPurchaseBB");
			domain.setModel("白棒型号");
			domain.setFactory("白棒工厂");
			domain.setUnit("个");
			domain.setPrice("年平均价11");
			domain.setSupplyType(new SupplyTypeLogic().getPurchaseType());
			domain.setRemark("白棒备注");
			domain.setMinInventory(1000);
			this.onMenu("提交");
			Assert.assertTrue("白棒新增失败", domain.getId()>0);
			return domain;
		}
		Assert.fail("商品白棒新增失败");
		return null;
	}

	public Commodity getC浆料() {
		if ("已经存在数据库，则取出".length()>0) {
			Commodity commodity = new CommodityLogic().getCommodityByNumber("TPurchaseJL");
			if (commodity!=null)
				return commodity;
		}
		if ("不存在则生成".length()>0) {
			CommodityForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("PurchaseList");
				this.onMenu("新增");
				Commodity domain = form.getDomain();
				domain.setName("浆料");
				domain.setCommNumber("TPurchaseJL");
				domain.setModel("浆料型号");
				domain.setFactory("资料工厂");
				domain.setUnit("克");
				domain.setPrice("年平均价12");
				domain.setSupplyType(new SupplyTypeLogic().getPurchaseType());
				domain.setRemark("浆料备注");
				domain.setMinInventory(1000);
				this.onMenu("提交");
				Assert.assertTrue("浆料新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("商品资料新增失败");
		return null;
	}

	public Commodity getC铁帽() {
		if ("已经存在数据库，则取出".length()>0) {
			Commodity commodity = new CommodityLogic().getCommodityByNumber("TPurchaseTM");
			if (commodity!=null)
				return commodity;
		}
		if ("不存在则生成".length()>0) {
			CommodityForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("PurchaseList");
				this.onMenu("新增");
				Commodity domain = form.getDomain();
				domain.setName("铁帽");
				domain.setCommNumber("TPurchaseTM");
				domain.setModel("铁帽型号");
				domain.setFactory("铁帽工厂");
				domain.setUnit("个");
				domain.setPrice("年平均价3");
				domain.setSupplyType(new SupplyTypeLogic().getPurchaseType());
				domain.setRemark("铁帽备注");
				domain.setMinInventory(1000);
				this.onMenu("提交");
				Assert.assertTrue("铁帽新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("商品铁帽新增失败");
		return null;
	}

	private void test生产商品() {
		CommodityForm form = this.getForm();
		if ("新增".length()>0) {
			this.loadView("ProductList");
			if ("已经存在则先删除".length()>0) {
				this.setFilters("commNumber", "TCommodityProduct");
				int dsize = this.getListViewValue().size();
				if (dsize>0) {
					this.setSqlListSelect(dsize);
					this.onMenu("删除");
				}
			}
			this.onMenu("新增");
			Commodity domain = form.getDomain();
			domain.setName("测生产商品");
			domain.setCommNumber("TCommodityProduct");
			domain.setModel("生产商品型号");
			domain.setFactory("生产厂家");
			domain.setUnit("生产商品单位");
			domain.setPrice("年平均价14");
			domain.setSupplyType(new SupplyTypeLogic().getProductType());
			domain.setCommType("生产大类");
			domain.setRemark("生产商品备注");
			domain.setMinInventory(1000);
			this.onMenu("提交");
			Assert.assertTrue("生产商品新增失败", domain.getId()>0);
		}
		if ("复制新增".length()>0) {
			this.setFilters("commNumber", "TCommodityProduct");
			this.setSqlListSelect(1);
			this.onMenu("复制新增");
			Commodity domain = form.getDomain();
			domain.setName("测生产商品1");
			domain.setCommNumber("TCommodityProduct4Delete");
			domain.setModel("生产商品型号");
			domain.setFactory("生产厂家");
			domain.setUnit("生产商品单位");
			domain.setSupplyType(new SupplyTypeLogic().getProductType());
			domain.setCommType("生产大类");
			domain.setRemark("生产商品备注");
			domain.setMinInventory(1000);
			this.onMenu("提交");
			Assert.assertTrue("生产商品复制新增失败", domain.getId()>0);
		}
		if ("删除".length()>0) {
			this.setFilters("commNumber", "TCommodityProduct4Delete");
			this.setSqlListSelect(1);
			this.onMenu("删除");
			Assert.assertTrue("复制新增生产商品删除失败", form.getSelectFormer4Commodity().getFirst().getId()<0);
		}
		if ("再编辑".length()>0) {
			this.setFilters("commNumber", "TCommodityProduct");
			this.setSqlListSelect(1);
			this.onMenu("编辑");
			form.getDomain().setName("测生产商品已编辑");
			this.onMenu("提交");
		}
		if ("删除".length()>0) {
			this.setFilters("commNumber", "TCommodityProduct");
			this.setSqlListSelect(1);
			this.onMenu("删除");
			Assert.assertTrue("生产商品删除失败", form.getSelectFormer4Commodity().getFirst().getId()<0);
		}
	}
	
	public Commodity getS黑棒() {
		if ("已经存在数据库，则取出".length()>0) {
			Commodity commodity = new CommodityLogic().getCommodityByNumber("TProductHB");
			if (commodity!=null)
				return commodity;
		}
		if ("不存在则生成".length()>0) {
			CommodityForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("ProductList");
				this.onMenu("新增");
				Commodity domain = form.getDomain();
				domain.setName("黑棒");
				domain.setCommNumber("TProductHB");
				domain.setModel("黑棒型号");
				domain.setFactory("本厂家");
				domain.setUnit("千个");
				domain.setPrice("年平均价15");
				domain.setSupplyType(new SupplyTypeLogic().getProductType());
				domain.setRemark("黑棒备注");
				domain.setMinInventory(1000);
				this.onMenu("提交");
				Assert.assertTrue("黑棒新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("商品黑棒新增失败");
		return null;
	}
	
	public Commodity getS电磁棒1加工1生产3常规() {
		if ("已经存在数据库，则取出".length()>0) {
			Commodity commodity = new CommodityLogic().getCommodityByNumber("TProductDCB");
			if (commodity!=null)
				return commodity;
		}
		if ("不存在则生成".length()>0) {
			this.getS黑棒();
			this.getC浆料();
			this.getC白棒();
			this.getC铁帽();
			CommodityForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("ProductList");
				this.onMenu("新增");
				Commodity domain = form.getDomain();
				domain.setName("电磁棒");
				domain.setCommNumber("TProductDCB");
				domain.setModel("电磁棒型号");
				domain.setFactory("本厂家");
				domain.setUnit("千个");
				domain.setPrice("年平均价16");
				domain.setSupplyType(new SupplyTypeLogic().getProductType());
				domain.setRemark("电磁棒备注");
				domain.setMinInventory(1000);
				this.onMenu("提交");
				Assert.assertTrue("电磁棒新增失败", domain.getId()>0);
			}
			if ("设置BOM表".length()>0) {
				this.setFilters("commNumber", "TProductDCB");
				this.setSqlListSelect(1);
				this.onMenu("设置BOM表");
				BOMForm bomForm = this.getForm().getBOMForm();
				bomForm.getDomain().setLevel(10);
				if (true) {
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(0);
					bomDetail.setCommodity(this.getS黑棒());
					bomDetail.getBomTicket().setAunit(1);
					bomDetail.setArrange("去请购");
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增子级");
					BomDetail bomDetail = bomForm.getDetailList().get(1);
					bomDetail.setCommodity(this.getC浆料());
					bomDetail.getBomTicket().setAunit(0.84);
					bomDetail.setArrange(new ArrangeTypeLogic().getNormal());
				}
				if (true) {
					this.setEditListSelect(2);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(2);
					bomDetail.setCommodity(this.getC白棒());
					bomDetail.getBomTicket().setAunit(1);
					bomDetail.setArrange(new ArrangeTypeLogic().getNormal());
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(3);
					bomDetail.setCommodity(this.getC铁帽());
					bomDetail.getBomTicket().setAunit(2);
					bomDetail.setArrange("去请购");
				}
			}
			if ("提交".length()>0) {
				this.onMenu("提交");
				Assert.assertTrue("电磁棒设置BOM表失败", form.getDomain().getBomDetails().size()==4);
				return form.getDomain();
			}
		}
		Assert.fail("商品电磁棒新增失败");
		return null;
	}

	protected void setQ清空() {
		String sql = "delete from bs_commodity where sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
		WindowMonitor.getMonitor().removeAttribute("Commodity商品编码Numbers");
	}
}

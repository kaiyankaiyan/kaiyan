package com.haoyong.sales.test.base;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.bean.BeanClass;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.Window;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.ClientForm;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.util.SSaleUtil;

public class ClientTest extends AbstractTest<ClientForm> {
	
	public ClientTest() {
		this.setForm(new ClientForm());
	}
	
	public HashMap<Set<TestMode>, List<OrderDetail>> getSplitOrders(List<OrderDetail> orderList) {
		HashMap<Set<TestMode>, List<OrderDetail>> map = new HashMap<Set<TestMode>, List<OrderDetail>>();
		for (OrderDetail order: orderList) {
			LinkedHashSet<TestMode> kmode = new LinkedHashSet<TestMode>();
			for (TestMode mode: TestMode.values()) {
				if (mode==TestMode.Purchase && new SupplyTypeLogic().isPurchaseType(order.getCommodity().getSupplyType())) {
					kmode.add(mode);
				} else if (mode==TestMode.Product && new SupplyTypeLogic().isProductType(order.getCommodity().getSupplyType())) {
					kmode.add(mode);
				} else if (mode==TestMode.SubCompany && StringUtils.isBlank(order.getSubCompany().getName())==false) {
					kmode.add(mode);
				} else {
					continue;
				}
			}
			List<OrderDetail> kvalue = null;
			for (Set<TestMode> key: map.keySet()) {
				if (StringUtils.equals(Arrays.toString(key.toArray(new TestMode[0])), Arrays.toString(kmode.toArray(new TestMode[0])))) {
					kvalue = map.get(key);
					break;
				}
			}
			if (kvalue==null) {
				kvalue = new ArrayList<OrderDetail>();
				map.put(kmode, kvalue);
			}
			kvalue.add(order);
		}
		return map;
	}
	public HashMap<Set<TestMode>, List<OrderDetail>> getSplitPurchases(List<OrderDetail> clientPurchases) {
		HashMap<Set<TestMode>, List<OrderDetail>> map = new HashMap<Set<TestMode>, List<OrderDetail>>();
		for (OrderDetail purchase: clientPurchases) {
			LinkedHashSet<TestMode> kmode = new LinkedHashSet<TestMode>();
			for (TestMode mode: TestMode.values()) {
				if (mode==TestMode.Purchase && new SupplyTypeLogic().isPurchaseType(purchase.getCommodity().getSupplyType())) {
					kmode.add(mode);
				} else if (mode==TestMode.Product && new SupplyTypeLogic().isProductType(purchase.getCommodity().getSupplyType())) {
					kmode.add(mode);
				} else if ("客户商家的供应商是上级，我是上级的分公司".length()>0 && mode==TestMode.SubCompany && purchase.getSupplier().getToSellerId()>0 && new SubCompanyLogic().getSubCompany(purchase.getSupplier())!=null) {
					kmode.add(mode);
				} else {
					continue;
				}
			}
			List<OrderDetail> kvalue = null;
			for (Set<TestMode> key: map.keySet()) {
				if (StringUtils.equals(Arrays.toString(key.toArray(new TestMode[0])), Arrays.toString(kmode.toArray(new TestMode[0])))) {
					kvalue = map.get(key);
					break;
				}
			}
			if (kvalue==null) {
				kvalue = new ArrayList<OrderDetail>();
				map.put(kmode, kvalue);
			}
			kvalue.add(purchase);
		}
		return map;
	}
	
	private void check导入客户(String sDomains, String sIndexs) {
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
			List<Client> importList = this.getForm().getImportList();
			Client domain = importList.get(importList.size()-1);
			Assert.assertTrue("导入客户有保存", domain.getId()>0);
		}
	}
	
	public void test导入客户() {
		this.setQ清空();
		if ("列序号记忆保存".length()>0) {
			StringBuffer sIndexs = new StringBuffer();
			for (int i=0; i<10; i++, sIndexs.append(i).append("\t"));
			this.check导入客户(sIndexs.toString(), sIndexs.deleteCharAt(sIndexs.length()-1).toString());
		}
	}

	private void test客户() {
		ClientForm form = this.getForm();
		if ("新增".length()>0) {
			this.loadView("List");
			this.onMenu("新增");
			Client domain = form.getDomain();
			domain.setName("测客户");
			domain.setNumber("TClient");
			domain.setLinker("客户人");
			domain.setLinkerCall("客户电话");
			domain.setChuanzhen("客户传真号");
			domain.setAddress("客户地址");
			domain.setRemark("客户备注");
			this.onMenu("提交");
			Assert.assertTrue("客户新增失败", domain.getId()>0);
		}
		if ("再编辑".length()>0) {
			this.setFilters("name", "测客户");
			this.setSqlListSelect(1);
			this.onMenu("编辑");
			form.getDomain().setName("测客户已编辑");
			this.onMenu("提交");
		}
		if ("删除".length()>0) {
			this.setFilters("name", "测客户已编辑");
			this.setSqlListSelect(1);
			this.onMenu("删除");
			Assert.assertTrue("客户删除失败", form.getSelectFormer4Client().getFirst().getId()<0);
		}
	}
	
	@Test
	public void test1() {
		String enc = "UTF-8";
		String source = "var Base64 = {_keyStr: \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\",encode: function(e) {var t = \"\";var n, r, i, s, o, u, a;var f = 0;e = Base64._utf8_encode(e);while (f < e.length) {n = e.charCodeAt(f++);r = e.charCodeAt(f++);i = e.charCodeAt(f++);s = n >> 2;o = (n & 3) << 4 | r >> 4;u = (r & 15) << 2 | i >> 6;a = i & 63;if (isNaN(r)) {u = a = 64} else if (isNaN(i)) {a = 64}t = t + this._keyStr.charAt(s) + this._keyStr.charAt(o) + this._keyStr.charAt(u) + this._keyStr.charAt(a)}return t},decode: function(e) {var t = \"\";var n, r, i;var s, o, u, a;var f = 0;e=e.replace(/[^A-Za-z0-9+/=]/g,\"\");while (f < e.length) {s = this._keyStr.indexOf(e.charAt(f++));o = this._keyStr.indexOf(e.charAt(f++));u = this._keyStr.indexOf(e.charAt(f++));a = this._keyStr.indexOf(e.charAt(f++));n = s << 2 | o >> 4;r = (o & 15) << 4 | u >> 2;i = (u & 3) << 6 | a;t = t + String.fromCharCode(n);if (u != 64) {t = t + String.fromCharCode(r)}if (a != 64) {t = t + String.fromCharCode(i)}}t = Base64._utf8_decode(t);return t}}// 定义字符串var string = \"Hello World!\";// 加密var encodedString = Base64.encode(string);console.log(encodedString); // 输出: \"SGVsbG8gV29ybGQh\"// 解密var decodedString = Base64.decode(encodedString);console.log(decodedString); // 输出: \"Hello World!\"";
		StringBuffer sb = new StringBuffer();
		sb.append("\nsource\t").append(source).append("\n");
		try {
			String sencode = URLEncoder.encode(source, enc);
			sb.append("encode\t").append(sencode).append("\n");
			String sdecode = URLDecoder.decode(sencode, enc);
			sb.append("decode\t").append(sdecode).append("\n");
			LogUtil.info(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setSellerViewTrunk(String... trunkList0) {
		List<String> trunkList = Arrays.asList(trunkList0);
		SellerViewSetting vs = new ClientLogic().getViewSetting();
		vs.setTitleList(new Client().getTrunkDefault());
		vs.setTrunkList(trunkList);
		LinkedHashSet chooseList = new LinkedHashSet<String>(vs.getChooseList());
		chooseList.addAll(trunkList);
		new SellerViewSettingLogic().saveViewSetting(vs);
	}
	
	public List<Client> getClients(String... nameList0) {
		Map<String, Object[]> map = new HashMap<String, Object[]>();
		map.put("79101", new Object[]{"number", "79101", "name", "101业主名", "linkerCall", "101电话"});
		map.put("79102", new Object[]{"number", "79102", "name", "102业主名", "linkerCall", "102电话"});
		map.put("79103", new Object[]{"number", "79103", "name", "103业主名", "linkerCall", "103电话"});
		map.put("79104", new Object[]{"number", "79104", "name", "104业主名", "linkerCall", "104电话"});
		List<String> nameList = new ArrayList<String>(Arrays.asList(nameList0));
		List<Client> clientList = new ArrayList<Client>();
		for (String key: map.keySet()) {
			if (nameList.contains(key)) {
				nameList.remove(key);
				Client domain = new Client();
				for (Iterator<Object> iter=Arrays.asList(map.get(key)).iterator(); iter.hasNext();) {
					String k=(String)iter.next();
					Object v = iter.next();
					ReflectHelper.setPropertyValue(domain, k, v);
				}
				if ("已经存在数据库，则取出".length()>0) {
					Client client = new ClientLogic().getClientByNumber(domain.getNumber());
					if (client!=null) {
						clientList.add(client);
						continue;
					}
				}
				if ("不存在则生成".length()>0) {
					this.loadView("List");
					this.onMenu("新增");
					TicketPropertyUtil.copyProperties(domain, this.getForm().getDomain());
					this.onMenu("提交");
					clientList.add(this.getForm().getDomain());
				}
			}
		}
		if (nameList.size()>0)
			Assert.fail(new StringBuffer("客户").append(nameList).append("找不到注册").toString());
		return clientList;
	}

	public Client get幸亚() {
		if ("已经存在数据库，则取出".length()>0) {
			Client client = new ClientLogic().getClientByNumber("XY");
			if (client!=null)
				return client;
		}
		if ("不存在则生成".length()>0) {
			ClientForm form = this.getForm();
			if ("新增".length()>0) {
				this.loadView("List");
				this.onMenu("新增");
				Client domain = form.getDomain();
				domain.setName("幸亚苏州电子工业有限公司");
				domain.setNumber("XY");
				domain.setProvince("江苏");
				domain.setCity("苏州");
				domain.setArea("吴江区");
				domain.setLinker("张Linker");
				domain.setLinkerCall("0512-LinkerCall");
				domain.setChuanzhen("0512-Chuanzhen");
				domain.setAddress("江苏省苏州市吴江区松陵镇经济开发区柳胥路430号");
				domain.setRemark("客户备注");
				this.onMenu("提交");
				Assert.assertTrue("客户幸亚新增失败", domain.getId()>0);
				return domain;
			}
		}
		Assert.fail("客户幸亚新增失败");
		return null;
	}
	
	public Client get吉高电子客户() {
		ClientForm form = this.getForm();
		Client client = new ClientLogic().getClientByNumber("JGKH");
		if ("新增".length()>0 && client==null) {
			this.loadView("List");
			this.onMenu("新增");
			Client domain = form.getDomain();
			domain.setName("吉高电子客户有限公司");
			domain.setNumber("JGKH");
			domain.setProvince("广东省");
			domain.setCity("广州");
			domain.setArea("天河区");
			domain.setLinker("吉高人");
			domain.setLinkerCall("0512-吉高电话");
			domain.setChuanzhen("0512-Chuanzhen");
			domain.setAddress("吉高地址广东省广州市天河区柳胥路430号");
			domain.setRemark("客户备注");
			this.onMenu("提交");
		}
		if (true) {
			this.loadView("List");
			this.setFilters("name", "吉高电子客户有限公司");
			this.setSqlListSelect(1);
			this.onMenu("受理商家链接");
			form.getDomain().setFromSellerName("吉高电子");
			form.getDomain().setSubmitNumber("NanNingPartner");
			this.onMenu("验证商家授权码");
			this.onMenu("提交");
			Assert.assertTrue("吉高电子客户商家新增失败", form.getDomain().getId()>0);
		}
		return form.getDomain();
	}
	
	private void temp() {
	}
	
	public ClientTest setWindow(Window win) {
		try {
			BeanClass.getBeanClass(this.getClass()).getFieldAccessor("window").set(this, win);
		} catch (Exception e) {
			throw LogUtil.getRuntimeException(e);
		}
		return this;
	}
	
	public void setQ清空() {
		String sql = "delete from bs_company where dtype=2 and sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
	}
}

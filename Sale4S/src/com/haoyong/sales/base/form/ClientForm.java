package com.haoyong.sales.base.form;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.Component;
import net.sf.mily.util.LogUtil;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
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
 * 界面管理——客户
 */
public class ClientForm extends AbstractForm<Client> implements FViewInitable {
	
	private Client domain;
	private List<Client> selectedList;

	public void prepareCreate(){
		domain = new Client();
		selectedList=new ArrayList<Client>();
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
		this.setDomain(new Client());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	public void validateCommit() {
		StringBuffer sb = validateClient(this.getDomain());
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private StringBuffer validateClient(Client domain) {
		StringBuffer sb = new StringBuffer();
		if(StringUtils.isBlank(domain.getName()))
			sb.append("客户名称不能为空，");
		if (StringUtils.isBlank(domain.getNumber())==false && new ClientLogic().hasRepeat(domain, "number", domain.getNumber()))
			sb.append("客户编号重复").append(domain.getNumber()).append(",");
		return sb;
	}
	
	private void validateFromSeller() {
		Client link = this.getDomain();
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
			} else if (new SubmitTypeLogic().isClientType(supplier.getSubmitType())==false) {
				throw new LogicException(2, "非客户链接类型。");
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
		if (new ClientLogic().getLinkChoosableLogic().isValid(this.getDomain(), sb)==false)
			sb.append("请补充链接信息，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void validateToImport(Component fcomp) throws Exception {
		List<TextFieldBuilder> builderList = this.getBaseImportForm().validateIndexes(fcomp);
		StringBuffer sb = new StringBuffer();
		StringBuffer input = new StringBuffer().append(getDomain().getVoParamMap().get("Remark"));
		if ("null".equals(input.toString()))
			sb.append("请粘贴入单元格内容，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else {
			this.setAttr("ImportFieldList", builderList);
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
		for (Iterator<Client> iter=getImportList().iterator(); iter.hasNext(); ri++) {
			Client d = iter.next();
			sitem = new StringBuffer();
			if(StringUtils.isBlank(d.getName()))
				sitem.append("客户名称不能为空，");
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true) {
			new ClientLogic().getPropertyChoosableLogic().chooseViewBuilder(viewBuilder);
		}
		if (true) {
			new ClientLogic().getLinkChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (viewBuilder.getName().equals("Import"))
			this.getBaseImportForm().setImportBuilderInit(viewBuilder);
	}
	
	public void setClientA4Service(ViewData<Client> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	
	public void setClientL4Service(ViewData<Client> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Client().getSelectedList());
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
		if ("供应商商品的配置copy to客户商品的配置".length()>0) {
			SellerViewSetting stClient=new CommodityLogic().getViewSetting(), stSupplier=this.getDomain().getVoparam(SellerViewSetting.class);
			long sellerId=stClient.getSellerId();
			TicketPropertyUtil.copyFieldsSkip(TicketPropertyUtil.deepClone(stSupplier), stClient);
			stClient.setSellerId(sellerId);
			new SellerViewSettingLogic().saveViewSetting(stClient);
		}
	}
	
	private void setImport4Service(ViewData<Client> viewData) {
		ClientLogic logic = new ClientLogic();
		viewData.setTicketDetails(new ArrayList<Client>());
		for (Client row: this.getImportList()) {
			Client dm = null;
			if (StringUtils.isBlank(row.getName())==false)
				dm = logic.getClientByName(row.getName());
			else if (StringUtils.isBlank(row.getNumber())==false)
				dm = logic.getClientByNumber(row.getNumber());
			if (dm!=null) {
				List<TextFieldBuilder> fieldList = this.getAttr("ImportFieldList");
				for (TextFieldBuilder builder: fieldList) {
					Object v0=builder.getEntityPropertyValue(dm), v1=builder.getEntityPropertyValue(row);
					String s0=(v0==null? "": String.valueOf(v0)), s1=(v1==null? "": String.valueOf(v1));
					if (s1.length()>s0.length())
						try {
							builder.setEntityPropertyValue(dm, v1);
						} catch (ParseException e) {
							throw LogUtil.getRuntimeException(e);
						}
				}
				TicketPropertyUtil.copyFieldsSkip(row, dm);
			}
			viewData.getTicketDetails().add(dm!=null? dm: row);
		}
	}
	
	public List<Client> getImportList() {
		String k = "ImportList";
		List<Client> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Client>();
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
		List<Client> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}
	
	private void setImportNumberAdd() {
		for (Client dm: this.getImportList()) {
			StringBuffer sb = new StringBuffer();
			if (dm.getNumber()!=null)
				sb.append(dm.getNumber()).append("_");
			sb.append(new Client().genSerialNumber());
			dm.setNumber(sb.toString());
		}
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.Choose12 logic = new ClientLogic().getPropertyChoosableLogic();
		former.setViewBuilder(logic.getChooseBuilder());
		former.setSellerViewSetting(logic.getChooseSetting( logic.getChooseBuilder() ));
		return former;
	}
	public SelectTicketFormer4Sql<ClientForm, Client> getSelectFormer4Client() {
		String k="SelectFormer4Client";
		SelectTicketFormer4Sql<ClientForm, Client> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<ClientForm, Client>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public List<Client> getSelectedList() {
		return this.selectedList;
	}

	@Override
	public void setSelectedList(List<Client> selected) {
		this.selectedList = selected;
	}
	
	public ClientForm getForm() {
		return this;
	}
	
	public Client getDomain() {
		return domain;
	}

	public void setDomain(Client t) {
		this.domain = t ;
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
	
	private Client getClient() {
		String k="GetClient";
		Client d = this.getAttr(k);
		if (d==null) {
			d = new Client();
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
	
	private String getClientText() throws Exception {
		String k = "ClientText";
		String text = this.getAttr(k);
		if (text==null) {
			URL url = Thread.currentThread().getContextClassLoader().getResource("");
			for (File cur=new File(url.getFile()).getParentFile(), file=new File(new StringBuffer(cur.getAbsolutePath()).append(File.separator).append("Client.txt").toString()); cur!=null; cur=null) {
				if(file.isFile() && file.exists()){
					InputStreamReader read = null;
					try {
						read = new InputStreamReader(new FileInputStream(file));
						BufferedReader bufferedReader = new BufferedReader(read);
						String lineTxt = null;
						StringBuffer sb = new StringBuffer();
						while((lineTxt = bufferedReader.readLine()) != null){
							sb.append(lineTxt).append("\n");
						}
						text = this.setAttr(k, sb.toString());
						break;
					} catch(Exception e) {
						text = null;
					} finally {
						if (read != null) { try { read.close(); } catch (IOException ex) { } } 
					}
				}
			}
		}
		return text;
	}
	
	private void setClientText(String text) throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		for (File cur=new File(url.getFile()).getParentFile(), file=new File(new StringBuffer(cur.getAbsolutePath()).append(File.separator).append("Client.txt").toString()); cur!=null; cur=null) {
			if (file.exists()) {
			} else {
				file.createNewFile();
			}  
			BufferedWriter output = new BufferedWriter(new FileWriter(file));  
			output.write(text);  
			output.close();  
		}
		this.setAttr("ClientText", null);
	}
	
	private void setRemarkSplit() {
		String sql = this.getFormProperty("attrMap.Remark");
		if (sql.indexOf("\n\n")>-1)
			return;
		sql = sql.replaceAll("\"Name\"\\:","\n\n\"Name\":").replaceAll("\"ContactNumber\"\\:","\n\"ContactNumber\":")
		.replaceAll("\"TelList\"\\:","\n\"TelList\":").replaceAll("\"IsAuth\"\\:", "\n\"IsAuth\":")
		.replaceAll("\"EmailList\"\\:","\n\"EmailList\":").replaceAll("\"AuthLevel\"\\:","\n\"AuthLevel\":")
		;
		this.setAttr("Remark", sql);
	}
	
	private void setRemarkEmail() {
		String sql = this.getFormProperty("attrMap.Remark");
		StringBuffer sb = new StringBuffer("\n邮箱列表\n");
		LinkedHashSet<String> mailSet = new LinkedHashSet<String>();
		for (Matcher m=Pattern.compile("\"EmailList\":\"\\[\\{").matcher(sql); m.find();) {
			String sitem = sql.substring(m.start(), sql.indexOf('\n', m.end()));
			String mail = null;
			for (Matcher mmail=Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*").matcher(sitem); mmail.find(); ) {
				mail = sitem.substring(mmail.start(), mmail.end());
				if (mailSet.add(mail)) {
					sb.append(mail).append(",");
					if (mailSet.size()%5==0)		sb.append("\n");
				}
			}
		}
		LogUtil.error(sb.append("\n邮件数").append(mailSet.size()).toString());
	}
	
	private void setRemarkCall() {
		String sql = this.getFormProperty("attrMap.Remark");
		LinkedHashSet<String> callSet = new LinkedHashSet<String>();
		for (Matcher m=Pattern.compile("\"TelList\":\"\\[\\{").matcher(sql); m.find();) {
			String sitem = sql.substring(m.start(), sql.indexOf('\n', m.end()));
			String call = null;
			for (Matcher mcall=Pattern.compile("1(3|4|5|7|8|9)\\d{9}").matcher(sitem); mcall.find();) {
				call = sitem.substring(mcall.start(), mcall.end());
				callSet.add(call);
			}
		}
		List<String> yidList=new ArrayList<String>(), dianxList=new ArrayList<String>(), liantList=new ArrayList<String>(), otherList=new ArrayList<String>();
		for (String call: callSet) {
			int cnt=0;
			if (cnt==0)
			for (String yid: new String[]{"134", "135", "136", "137", "138", "139", "147", "150", "151", "152", "157", "158", "159", "178", "182", "183", "184", "187", "188", "198"}) {
				if (call.startsWith(yid)) {
					yidList.add(call);
					cnt++;
					break;
				}
			}
			if (cnt==0)
			for (String dianx: new String[]{"133", "149", "153", "173", "177", "180", "181", "189", "199"}) {
				if (call.startsWith(dianx)) {
					dianxList.add(call);
					cnt++;
					break;
				}
			}
			if (cnt==0)
			for (String liant: new String[]{"130", "131", "132", "145", "155", "156", "170", "175", "176", "185", "186"}) {
				if (call.startsWith(liant)) {
					liantList.add(call);
					cnt++;
					break;
				}
			}
			if (cnt==0)
				otherList.add(call);
		}
		StringBuffer sb = new StringBuffer();
		sb.append("\n移动号码集合").append(yidList.size()).append("，").append(yidList);
		sb.append("\n电信号码集合").append(dianxList.size()).append("，").append(dianxList);
		sb.append("\n联通号码集合").append(liantList.size()).append("，").append(liantList);
		sb.append("\n无归属号码集合").append(otherList);
		LogUtil.error(sb.append("\n手机号数").append(callSet.size()).toString());
	}
}

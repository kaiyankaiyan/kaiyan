package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.mily.support.form.SelectTicketFormer4Edit;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.SimpleDialog;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.LocationTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;

public class LocationTicketForm extends AbstractForm<OrderDetail> implements FViewInitable {

	private void canInstore(List<List<Object>> valiRows) {
		// commName,uneditable
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append(",");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void prepareOutstore() {
		this.getDomain().setLocationTicket(new LocationTicket());
		this.getDetailList().clear();
		if (new UserLogic().isInstallRole(this.getUser()))
			this.getDomain().getLocationTicket().setOut(this.getUser());
	}
	
	private void validateOutstoreTicket() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.equals(this.getDomain().getLocationTicket().getIn().getName(), this.getDomain().getLocationTicket().getOut().getName())==true)
			sb.append("迁入迁出分账名称不能相同，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateOutstore4Full() {
		this.validateOutstoreTicket();
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectEdit4Purchase().getSelectedList().size()==0)
			sb.append("请选择分账明细！");
		if (new PurchaseTicketLogic().getLocationChoosableLogic().isValid(this.getDomain().getLocationTicket(), sb)==false)
			sb.append("请补充分账信息，");
		for (OrderDetail detail: this.getSelectEdit4Purchase().getSelectedList()) {
			sitem = new StringBuffer();
			if (detail.getAmount() < detail.getLocationTicket().getLocAmount())
				sitem.append("迁出数量不能大于库存数量，");
			else if (detail.getAmount() > detail.getLocationTicket().getLocAmount())
				sitem.append("部分迁出请先拆分，");
			if (sitem.length()>0)
				sb.append(detail.getMonthnum()).append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateOutstore4Part() {
		this.validateOutstoreTicket();
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectEdit4Purchase().getSelectedList().size()==0)
			sb.append("请选择分账明细！");
		if (new PurchaseTicketLogic().getLocationChoosableLogic().isValid(this.getDomain().getLocationTicket(), sb)==false)
			sb.append("请补充分账信息，");
		for (OrderDetail detail: this.getSelectEdit4Purchase().getSelectedList()) {
			sitem = new StringBuffer();
			if (detail.getAmount() < detail.getLocationTicket().getLocAmount())
				sitem.append("迁出数量不能大于库存数量，");
			else if (detail.getAmount() == detail.getLocationTicket().getLocAmount())
				sitem.append("迁出数量等于库存数量请走全部迁出提交，");
			if (sitem.length()>0)
				sb.append(detail.getMonthnum()).append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateOutstore4ChangeOut() {
		if (new UserLogic().isInstallRole(this.getUser()))
			throw new LogicException(2, "工程安装人员不能改迁出分账!");
	}
	
	private void prepareConfirm() {
		this.getDetailList().clear();
		this.getDetailList().addAll(this.getSelectFormer4Purchase().getSelectedList());
		this.getDomain().setLocationTicket(this.getSelectFormer4Purchase().getFirst().getLocationTicket());
	}
	
	private void setOutstorePur4Service(ViewData<OrderDetail> viewData) {
		PropertyChoosableLogic.TicketDetail logic = new PurchaseTicketLogic().getLocationChoosableLogic();
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectEdit4Purchase().getSelectedList()) {
			logic.fromTrunk(logic.getTicketBuilder(), detail.getLocationTicket(), this.getDomain().getLocationTicket());
			detail.getLocationTicket().setOut(this.getDomain().getLocationTicket().getOut());
			detail.getLocationTicket().setIn(this.getDomain().getLocationTicket().getIn());
			list.add(detail);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setOutstoreOrder4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList=new ArrayList<OrderDetail>(), createOrders=new ArrayList<OrderDetail>();
		String orderNumber = null;
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail ord=purchase, sord=ord.getSnapShot();
			Storehouse out=this.getDomain().getLocationTicket().getOut(), in=this.getDomain().getLocationTicket().getIn();
			if (sord.getStOrder()>0) {
			} else if (((out!=null && out.isStoreCheck()) || in!=null && in.isStoreCheck())==false) {
				continue;
			}
			if (orderNumber==null)
				orderNumber = ord==null? new OrderDetail().getOrderTicket().genSerialNumber(): ord.getOrderTicket().getNumber();
			if (sord.getStOrder()==0) {
				ord.getOrderTicket().setOrderType(new OrderTypeLogic().getBackType());
				ord.getOrderTicket().setNumber(orderNumber);
				createOrders.add(ord);
				purchase.setVoparam(ord);
			}
			ordList.add(ord);
		}
		viewData.setTicketDetails(ordList);
		viewData.setParam("CreateOrders", createOrders);
	}
	
	private void setInstorePurchase4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			list.add(detail);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setPurchaseProp4Service(ViewData<OrderDetail> viewData) {
		// property
	}
	
	private void setInstoreOrder4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList=new ArrayList<OrderDetail>();
		for (OrderDetail purchase: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail ord=purchase, sord=ord.getSnapShot();
			if (sord.getStOrder()==0)
				continue;
			ordList.add(ord);
		}
		viewData.setTicketDetails(ordList);
	}

	private void setSplitPurchaseCurSubtract4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		ReceiptTicketLogic rlogic = new ReceiptTicketLogic();
		for (OrderDetail curPurSubtract: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail curOrdSubtract = curPurSubtract;
			curPurSubtract.getVoParamMap().put("SourceAmount", curPurSubtract.getAmount());
			double subtractAmount = curPurSubtract.getLocationTicket().getLocAmount();
			double remainAmount = curPurSubtract.getAmount() - subtractAmount;
			curPurSubtract.getVoParamMap().put("NewRemainAmount", remainAmount);
			String subtractMonthnum = new OrderTicketLogic().getSplitMonthnum(curPurSubtract.getMonthnum());
			OrderDetail nwPurRemain = new PurchaseTicketLogic().genClonePurchase(curPurSubtract);
			nwPurRemain.setAmount(remainAmount);
			curPurSubtract.getVoParamMap().put("NewRemainPurchase", nwPurRemain);
			curPurSubtract.setAmount(curPurSubtract.getLocationTicket().getLocAmount());
			curPurSubtract.setMonthnum(subtractMonthnum);
			if (curOrdSubtract!=null) {
				OrderDetail nwOrdRemain = new OrderTicketLogic().genCloneOrder(curOrdSubtract);
				nwOrdRemain.setAmount(remainAmount);
				curPurSubtract.getVoParamMap().put("NewRemainOrder", nwOrdRemain);
				curOrdSubtract.setAmount(subtractAmount);
				curOrdSubtract.setMonthnum(subtractMonthnum);
				nwPurRemain.setVoparam(nwOrdRemain);
			}
			purList.add(curPurSubtract);
		}
		viewData.setTicketDetails(purList);
	}

	private void setSplitPurchaseNewRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail curPurSubtract: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail nwPurRemain = (OrderDetail)curPurSubtract.getVoParamMap().get("NewRemainPurchase");
			if (nwPurRemain!=null)
				purList.add(nwPurRemain);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setStoreEffect4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	}

	private LocationTicket getLocationTicket() {
		LocationTicket t = this.getAttr(LocationTicket.class);
		if (t==null) {
			t = new LocationTicket();
			this.setAttr(t);
		}
		return t;
	}
	
	private void setLocationTicketNumber() {
		this.getDomain().getLocationTicket().genSerialNumber();
	}
	
	private ChooseFormer getLocationChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PurchaseTicketLogic logic = new PurchaseTicketLogic();
		former.setViewBuilder(logic.getLocationChoosableLogic().getTicketBuilder());
		former.setSellerViewSetting(logic.getLocationChoosableLogic().getChooseSetting( logic.getLocationChoosableLogic().getTicketBuilder() ));
		return former;
	}
	
	public SelectTicketFormer4Sql<LocationTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k="SelectFormer4Purchase";
		SelectTicketFormer4Sql<LocationTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<LocationTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public SelectTicketFormer4Edit<LocationTicketForm, OrderDetail> getSelectEdit4Purchase() {
		String k = "SelectEdit4Purchase";
		SelectTicketFormer4Edit<LocationTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Edit<LocationTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Sql<LocationTicketForm, User> getSelectFormer4User() {
		String k="SelectFormer4User";
		SelectTicketFormer4Sql<LocationTicketForm, User> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<LocationTicketForm, User>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Sql<LocationTicketForm, Storehouse> getSelectFormer4Storehouse() {
		String k="SelectFormer4Storehouse";
		SelectTicketFormer4Sql<LocationTicketForm, Storehouse> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<LocationTicketForm, Storehouse>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private void setPurchaseState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStPurchase(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setOrderState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStOrder(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setOutsfState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setOutsfId(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	private void setInsfState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setInsfId(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setLocationUser(ViewData<OrderDetail> viewData) {
		StringBuffer suser = new StringBuffer();
		suser.append(genTicketUser().getUserDate()).append(",");
		suser.append("from").append(this.getDomain().getLocationTicket().getOut().getName()).append(",");
		suser.append("to").append(this.getDomain().getLocationTicket().getIn().getName());
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUlocation(suser.toString());
		}
	}
	
	private void addLocationUser(ViewData<OrderDetail> viewData) {
		TicketUser user = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUlocation(user.addUserDate(d.getUlocation()));
		}
	}
	
	private void setPurchaseSelect(List<OrderDetail> storeList0) {
		List<OrderDetail> storeList = new ArrayList<OrderDetail>(storeList0);
		storeList.removeAll(this.getDetailList());
		this.getDetailList().addAll(storeList);
	}
	
	private void setStorehouseSelect(Component fcomp) {
		if (StringUtils.equals("in.name", ((Field)fcomp.searchParentByClass(SimpleDialog.class).getFirer()).getFieldBuilder().getName())==true) {
			this.getDomain().getLocationTicket().setIn(this.getSelectFormer4Storehouse().getFirst());
		} else {
			this.getDomain().getLocationTicket().setOut(this.getSelectFormer4Storehouse().getFirst());
		}
		this.getDetailList().clear();
	}
	private void setUserSelect(Component fcomp) {
		if (StringUtils.equals("in.name", ((Field)fcomp.searchParentByClass(SimpleDialog.class).getFirer()).getFieldBuilder().getName())==true) {
			this.getDomain().getLocationTicket().setIn(this.getSelectFormer4User().getFirst());
		} else {
			this.getDomain().getLocationTicket().setOut(this.getSelectFormer4User().getFirst());
		}
		this.getDetailList().clear();
	}
	
	private void setVersionDomains() {
		this.getSelectFormer4Purchase().setIdDomains(this.getSelectEdit4Purchase().getSelectedList());
	}
	
	private void setTicketOutName() {
		this.getDetailList().clear();
	}
	
	private void setTicketOutHead() {
		if (new UserLogic().isInstallRole(this.getUser())) {
			throw new LogicException(2, "工程安装人员不能迁出总账库存");
		} else {
			this.getDomain().getLocationTicket().setOut(null);
			this.setTicketOutName();
		}
	}
	
	private void setTicketInHead() {
		if (StringUtils.isBlank(this.getDomain().getLocationTicket().getOut().getName()))
			throw new LogicException(2, "迁出迁入不能相同。");
		this.getDomain().getLocationTicket().setIn(null);
	}
	
	private HashMap<String, String> getParam4OutstoreTicket() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (true) {
			StringBuffer sb = new StringBuffer();
			if (new UserLogic().isInstallRole(this.getUser())) {
				sb.append("c.instore='").append(this.getUserName()).append("'");
			} else if (StringUtils.isBlank(this.getDomain().getLocationTicket().getOut().getName())) {
				sb.append("c.instore is null");
			} else {
				sb.append("c.instore='").append(this.getDomain().getLocationTicket().getOut().getName()).append("'");
			}
			map.put("OutLocation", sb.toString());
		}
		return map;
	}

	private HashMap<String, String> getParam4Outstore() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (true) {
			StringBuffer sb = new StringBuffer();
			if (new UserLogic().isInstallRole(this.getUser())==true)
				sb.append("and c.outstore='").append(this.getUserName()).append("'");
			map.put("OutLocation", sb.toString());
		}
		return map;
	}

	private HashMap<String, String> getParam4Instore() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (true) {
			StringBuffer sb = new StringBuffer();
			if (new UserLogic().isInstallRole(this.getUser())==true)
				sb.append("and c.instore='").append(this.getUserName()).append("'");
			map.put("InLocation", sb.toString());
		}
		return map;
	}
	
	private HashMap<String, String> getParam4Location() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("InstallRole", new StringBuffer("'").append(new UserLogic().getInstallRoleName()).append("'").toString());
		return map;
	}

	public List<OrderDetail> getDetailList() {
		String k="PurchaseDetailList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	public OrderDetail getDomain() {
		String k="PurchaseDetailDomain";
		OrderDetail d = this.getAttr(k);
		if (d==null) {
			d = new OrderDetail();
			this.setAttr(k, d);
		}
		return d;
	}

	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true) {
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new PurchaseTicketLogic().getLocationChoosableLogic().trunkViewBuilder(viewBuilder);
		}
	}
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private List<OrderDetail> getSelectedList() {
		String k="PurchaseSelectList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	private void setDetailSnapShot() {
		for (OrderDetail curPurSubtract: this.getSelectEdit4Purchase().getSelectedList()) {
			curPurSubtract.setSnapShot1();
		}
	}
	
	private void setOutstoreHole() {
		for (OrderDetail pur: this.getSelectEdit4Purchase().getSelectedList()) {
			pur.getLocationTicket().setLocAmount(pur.getAmount());
		}
	}
}

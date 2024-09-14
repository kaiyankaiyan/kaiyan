package com.haoyong.sales.sale.form;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.bean.DynaProperty;
import net.sf.mily.bean.ListDynaBean;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.server.EditViewer;
import net.sf.mily.support.form.SelectTicketFormer4Cross;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DateTimeType;
import net.sf.mily.types.DoubleType;
import net.sf.mily.types.LongType;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Menu;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.EditViewBuilder;
import net.sf.mily.webObject.EntityCrossBuilder;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ListViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.ColumnField;
import net.sf.mily.webObject.query.Fields;
import net.sf.mily.webObject.query.Fields.CondReg;
import net.sf.mily.webObject.query.SqlListBuilderSetting;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.util.JoinedIterator;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.SupplierT;
import com.haoyong.sales.base.domain.Yards;
import com.haoyong.sales.base.form.CommColForm;
import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.base.logic.CommColLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.base.logic.YardsLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.domain.StoreItem;
import com.haoyong.sales.sale.domain.StoreMonth;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.test.base.ClientTest;

public class StoreTicketForm extends AbstractForm<OrderDetail> implements FViewInitable {
	
	private void beforeInstoreExtra(IEditViewBuilder builder0) {
		new ClientTest().getModeList().addTest("storeForm.getExtraInstoreList", this.getExtraInstoreList());
		this.getDetailList().clear();
		OrderDetail first = null;
		for (OrderDetail item: this.getExtraInstoreList()) {
			if (first==null) {
				first = item;
				item.getOrderTicket().genSerialNumber();
				item.getPurchaseTicket().genSerialNumber();
				item.getReceiptTicket().genSerialNumber();
			} else {
				if (item.getOrderTicket().getNumber()==null)
					item.getOrderTicket().setNumber(first.getOrderTicket().getNumber());
				if (item.getPurchaseTicket().getNumber()==null)
					item.getPurchaseTicket().setNumber(first.getPurchaseTicket().getNumber());
				if (item.getReceiptTicket().getNumber()==null)
					item.getReceiptTicket().setNumber(first.getReceiptTicket().getNumber());
			}
			if (item.getMonthnum()==null)
				item.setMonthnum(new OrderTicketLogic().genMonthnum());
			Assert.assertTrue("待入库要有采购收货单", item.getPurchaseTicket().getNumber()!=null && item.getReceiptTicket().getNumber()!=null);
			OrderDetail pur = new PurchaseTicketLogic().genClonePurchase((OrderDetail)item);
			pur.getReceiptTicket().setStoreMoney(pur.getAmount() * pur.getReceiptTicket().getStorePrice());
			this.getDetailList().add(pur);
		}
		if ("现有Instore库存过滤".length()>0) {
			LinkedHashMap<String, LinkedHashSet<String>> colValues = new LinkedHashMap<String, LinkedHashSet<String>>();
			String[] k12List = new String[]{"purName","TPurchase.purName", "commName","TCommodity.commName"};
			for (OrderDetail item: this.getExtraInstoreList()) {
				for (Iterator<String> kIter=Arrays.asList(k12List).iterator(); kIter.hasNext();) {
					String col=kIter.next(), k2=kIter.next();
					String commName=(String)ReflectHelper.getPropertyValue(item, k2)+"";
					if (commName!=null) {
						LinkedHashSet<String> values=colValues.get(col);
						if (values==null) {
							values = new LinkedHashSet<String>();
							colValues.put(col, values);
						}
						values.add(commName);
					}
				}
			}
			LinkedHashMap<String, String> filterMap = new LinkedHashMap<String, String>();
			for (String col: colValues.keySet()) {
				LinkedHashSet<String> values = colValues.get(col);
				String snull = "null";
				if ("purName".equals(col) && values.contains(snull) && values.size()>1)
					Assert.fail("其中一个是采购名称null");
				else if ("commName".equals(col) && values.contains(snull) && values.size()>1)
					Assert.fail("其中一个是商品名称null");
				else {
					StringBuffer sb = new StringBuffer();
					for (String v: values) {
						sb.append("=").append(v).append(" ");
					}
					filterMap.put(col, sb.toString());
				}
			}
			SqlListBuilder sqlBuilder = (SqlListBuilder)((EditViewBuilder)builder0).getFieldBuildersDeep(SqlListBuilder.class).get(0);
			SqlListBuilderSetting setting = this.getSqlListBuilderSetting(sqlBuilder);
			for (Map.Entry<ColumnField, String> entry: setting.getColumnMap(sqlBuilder, setting.getFilters()).entrySet()) {
				filterMap.put(entry.getKey().getName(), entry.getValue());
			}
			if (this.getExtraInstoreList().isEmpty()) {
				filterMap.put("modifytime", ">".concat(new DateTimeType().format(new Date())));
			}
			if ("库存分账".length()>0) {
				if (new UserLogic().isInstallRole(this.getUser()))
					filterMap.put("instore", "=".concat(this.getUserName()));
			}
			List<String> filterList = new ArrayList<String>();
			for (Map.Entry<String, String> entry: filterMap.entrySet()) {
				filterList.add(entry.getKey());
				filterList.add(entry.getValue());
			}
			this.setFilters(sqlBuilder, filterList.toArray(new Object[0]));
		}
	}
	
	private void beforeOutstoreExtra(IEditViewBuilder builder0) {
		"".toCharArray();
		new ClientTest().getModeList().addTest("storeForm.getExtraOutstoreList", this.getExtraOutstoreList());
		this.getDetailList().clear();
		int rowi=0;
		for (OrderDetail source: this.getExtraOutstoreList()) {
			OrderDetail ord = new PurchaseTicketLogic().genClonePurchase((OrderDetail)source);
			ord.getReceiptTicket().setStoreMoney(ord.getAmount() * ord.getReceiptTicket().getStorePrice());
			ord.setVoparam(source);
			if ("记参与出库计算的order".length()>0)
				source.getVoParamMap().put("OutstoreOrder", ord);
			if ("记第几个order".length()>0 && ord.getClient().getId()==0)
				ord.getClient().setId(++rowi);
			this.getDetailList().add(ord);
		}
		if ("现有Outstore库存过滤".length()>0) {
			LinkedHashMap<String, LinkedHashSet<String>> colValues = new LinkedHashMap<String, LinkedHashSet<String>>();
			String[] k12List = new String[]{"commName","TCommodity.commName"};
			for (OrderDetail item: this.getExtraOutstoreList()) {
				for (Iterator<String> kIter=Arrays.asList(k12List).iterator(); kIter.hasNext();) {
					String col=kIter.next(), k2=kIter.next();
					String commName=(String)ReflectHelper.getPropertyValue(item, k2)+"";
					if (commName!=null) {
						LinkedHashSet<String> values=colValues.get(col);
						if (values==null) {
							values = new LinkedHashSet<String>();
							colValues.put(col, values);
						}
						values.add(commName);
					}
				}
			}
			LinkedHashMap<String, String> filterMap = new LinkedHashMap<String, String>();
			for (String col: colValues.keySet()) {
				LinkedHashSet<String> values = colValues.get(col);
				String snull = "null";
				if ("commName".equals(col) && values.contains(snull) && values.size()>1)
					Assert.fail("其中一个是商品名称null");
				else {
					StringBuffer sb = new StringBuffer();
					for (String v: values) {
						sb.append("=").append(v).append(" ");
					}
					filterMap.put(col, sb.toString());
				}
			}
			if (this.getExtraOutstoreList().isEmpty())
				filterMap.put("modifytime", ">".concat(new DateTimeType().format(new Date())));
			if ("库存分账".length()>0) {
				if (new UserLogic().isInstallRole(this.getUser()))
					filterMap.put("instore", "=".concat(this.getUserName()));
			}
			SqlListBuilder sqlBuilder = (SqlListBuilder)((EditViewBuilder)builder0).getFieldBuildersDeep(SqlListBuilder.class).get(0);
			SqlListBuilderSetting setting = this.getSqlListBuilderSetting(sqlBuilder);
			for (Map.Entry<ColumnField, String> entry: setting.getColumnMap(sqlBuilder, setting.getFilters()).entrySet()) {
				filterMap.put(entry.getKey().getName(), entry.getValue());
			}
			List<String> filterList = new ArrayList<String>();
			for (Map.Entry<String, String> entry: filterMap.entrySet()) {
				filterList.add(entry.getKey());
				filterList.add(entry.getValue());
			}
			this.setFilters(sqlBuilder, filterList.toArray(new Object[0]));
		}
	}
	
	private void beforeInstoreAgent(IEditViewBuilder builder0) {
		new ClientTest().getModeList().addTest("storeForm.getAgentInstoreList", this.getAgentInstoreList());
		new ClientTest().getModeList().addTest("storeForm.getAgent", this.getBomDetail());
		this.getAgentList().clear();
		for (BomDetail item: this.getAgentInstoreList()) {
			BomDetail source = TicketPropertyUtil.deepClone(item);
			item.setVoparam(source);
			this.getAgentList().add(item);
		}
	}
	
	private void beforeOutstoreAgent(IEditViewBuilder builder0) {
		new ClientTest().getModeList().addTest("storeForm.getAgentOutstoreList", this.getAgentOutstoreList());
		new ClientTest().getModeList().addTest("storeForm.getAgent", this.getBomDetail());
		this.getAgentList().clear();
		for (BomDetail source: this.getAgentOutstoreList()) {
			BomDetail ord = TicketPropertyUtil.deepClone(source);
			ord.setVoparam(source);
			source.getVoParamMap().put("OutstoreOrder", ord);
			this.getAgentList().add(ord);
		}
	}
	
	public void prepareRestore() {
		this.getDetailList().clear();
		this.getDetailList().addAll(getSelectFormer4Purchase().getSelectedList());
	}
	
	private void prepareTest() {
		List<Commodity> commList = (List<Commodity>)new ClientTest().getModeList().getTest("storeForm.getStoreCommodityList");
		if (commList!=null) {
			this.getStoreCommodityList().clear();
			this.getStoreCommodityList().addAll(commList);
		}
		List<OrderDetail> instoreList = (List<OrderDetail>)new ClientTest().getModeList().getTest("storeForm.getExtraInstoreList");
		if (instoreList!=null) {
			this.getExtraInstoreList().clear();
			this.getExtraInstoreList().addAll(instoreList);
		}
		List<OrderDetail> outstoreList = (List<OrderDetail>)new ClientTest().getModeList().getTest("storeForm.getExtraOutstoreList");
		if (outstoreList!=null) {
			this.getExtraOutstoreList().clear();
			this.getExtraOutstoreList().addAll(outstoreList);
		}
		BomDetail agent = (BomDetail)new ClientTest().getModeList().getTest("storeForm.getAgent");
		if (agent!=null)
			TicketPropertyUtil.copyFieldsSkip(agent, this.getBomDetail());
		List<BomDetail> inAgentList = (List<BomDetail>)new ClientTest().getModeList().getTest("storeForm.getAgentInstoreList");
		if (inAgentList!=null) {
			this.getAgentInstoreList().clear();
			this.getAgentInstoreList().addAll(inAgentList);
		}
		List<BomDetail> outAgentList = (List<BomDetail>)new ClientTest().getModeList().getTest("storeForm.getAgentOutstoreList");
		if (outAgentList!=null) {
			this.getAgentOutstoreList().clear();
			this.getAgentOutstoreList().addAll(outAgentList);
		}
	}
	
	public void viewinit(IEditViewBuilder vBuilder) {
		ViewBuilder viewBuilder = (ViewBuilder)vBuilder;
		if (true)
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		// reset StoreCross Count rows and cols
		if (viewBuilder.getName().equals("RestoreList")) {
			EntityCrossBuilder builder = (EntityCrossBuilder)viewBuilder.getFieldBuildersSelf(EntityCrossBuilder.class).get(0);
			builder.setParameters(builder.cloneParameters());
			StringBuffer rowFields=new StringBuffer(builder.getAttribute(ParameterName.Cfg, ParameterName.Row_Fields)), colFields=new StringBuffer();
			CommColLogic logic = new CommColLogic();
			for (FieldBuilder fieldb: logic.getRowBuilders()) {
				rowFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			for (FieldBuilder fieldb: logic.getColBuilders()) {
				colFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			builder.setAttribute(rowFields.length()==0? null: rowFields.deleteCharAt(rowFields.length()-1).toString(), ParameterName.Cfg, ParameterName.Row_Fields);
			builder.setAttribute(colFields.length()==0? null: colFields.deleteCharAt(colFields.length()-1).toString(), ParameterName.Cfg, ParameterName.Col_Fields);
			if (colFields.length()==0) {
				builder.setAttribute(null, ParameterName.Cfg, ParameterName.Cross);
				builder.setAttribute(null, ParameterName.Cfg, ParameterName.ColCount_Fields);
			}
		}
		if (Arrays.asList(new String[]{"MonthList", "EnoughList"}).contains(viewBuilder.getName())==true) {
			EntityCrossBuilder builder = (EntityCrossBuilder)viewBuilder.getFieldBuildersSelf(EntityCrossBuilder.class).get(0);
			builder.setParameters(builder.cloneParameters());
			StringBuffer rowFields=new StringBuffer(builder.getAttribute(ParameterName.Cfg, ParameterName.Row_Fields));
			CommColLogic logic = new CommColLogic();
			for (FieldBuilder fieldb: logic.getRowBuilders()) {
				rowFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			for (FieldBuilder fieldb: logic.getColBuilders()) {
				rowFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			builder.setAttribute(rowFields.deleteCharAt(rowFields.length()-1).toString(), ParameterName.Cfg, ParameterName.Row_Fields);
		}
		if (Arrays.asList(new String[]{"InExtraList", "OutExtraList"}).contains(viewBuilder.getName())==true) {
			ListViewBuilder builder = (ListViewBuilder)viewBuilder.getFieldBuilder("detailList");
			builder.setParameters(builder.cloneParameters());
			StringBuffer rowFields=new StringBuffer(builder.getAttribute(ParameterName.Cfg, ParameterName.Row_Fields));
			CommColLogic logic = new CommColLogic();
			for (FieldBuilder fieldb: logic.getRowBuilders()) {
				rowFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			for (FieldBuilder fieldb: logic.getColBuilders()) {
				rowFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			builder.setAttribute(rowFields.deleteCharAt(rowFields.length()-1).toString(), ParameterName.Cfg, ParameterName.Row_Fields);
		}
		if (Arrays.asList(new String[]{"InAgentList", "OutAgentList"}).contains(viewBuilder.getName())==true) {
			ListViewBuilder builder = (ListViewBuilder)viewBuilder.getFieldBuilder("agentList");
			builder.setParameters(builder.cloneParameters());
			StringBuffer rowFields=new StringBuffer(builder.getAttribute(ParameterName.Cfg, ParameterName.Row_Fields));
			CommColLogic logic = new CommColLogic();
			for (FieldBuilder fieldb: logic.getRowBuilders()) {
				rowFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			for (FieldBuilder fieldb: logic.getColBuilders()) {
				rowFields.append("commodity.").append(fieldb.getName()).append(";");
			}
			builder.setAttribute(rowFields.deleteCharAt(rowFields.length()-1).toString(), ParameterName.Cfg, ParameterName.Row_Fields);
		}
	}
	
	private String getColumnStore(LinkedHashMap<String, Object> params) {
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iter=params.keySet().iterator(); iter.hasNext();) {
			String yard = (String)params.get(iter.next());
			Double amount = Double.valueOf(params.get(iter.next()).toString());
			if (amount > 0) {
				sb.append(yard==null? "": yard).append("　").append(new DoubleType().format(amount));
			}
		}
		return sb.toString();
	}
	
	public List<Commodity> getStoreCommodityList() {
		String k = "StoreCommodityList";
		ArrayList<Commodity> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<Commodity>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<OrderDetail> getExtraInstoreList() {
		String k = "ExtraInstoreList";
		ArrayList<OrderDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<OrderDetail> getExtraOutstoreList() {
		String k = "ExtraOutstoreList";
		ArrayList<OrderDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private BomDetail getBomDetail() {
		String k = "AgentBomDetail";
		BomDetail d = this.getAttr(k);
		if (d==null) {
			d = new BomDetail();
			this.setAttr(k, d);
		}
		return d;
	}
	public void setBomDetail(BomDetail bom) {
		String k = "AgentBomDetail";
		this.setAttr(k, bom);
	}
	
	public List<BomDetail> getAgentInstoreList() {
		String k = "AgentInstoreList";
		ArrayList<BomDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<BomDetail> getAgentOutstoreList() {
		String k = "AgentOutstoreList";
		ArrayList<BomDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	/**
	 * 库存重算条件
	 */
	private HashMap<String, String> getParam4Restore(){
		HashMap<String, String> params = new HashMap<String, String>();
		if (true) {
			Date orderTime = new ClientTest().getModeList().getOrderTime();
			if (orderTime != null) {// Only TestCase Records
				params.put("fromTime", new StringBuffer().append("c.modifytime>='").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(orderTime)).append("' and ").toString());
			} else {
				params.put("fromTime", "");
			}
		}
		if (true) {
			String c="c.commName";
			Set<String> commnameList = new LinkedHashSet<String>();
			List<Commodity> commList = getStoreCommodityList();
			StringBuffer sb = new StringBuffer();
			if (commList.isEmpty()) {
				sb.append("1=1");
			} else {
				sb.append("(");
				for (Iterator<Commodity> iter=commList.iterator(); iter.hasNext();) {
					Commodity item = iter.next();
					String comm = new CommodityLogic().getPropertyChoosableLogic().toTrunk(item).getCommName();
					if (commnameList.add(comm))
						sb.append(c).append("='").append(comm).append("'").append(" or ");
				}
				sb.delete(sb.length()-4, sb.length()).append(")");
			}
			params.put("limitCommName", sb.toString());
		}
		return params;
	}
	
	/**
	 * 加、减备货库存条件
	 */
	public HashMap<String, String> getParam4Extra(){
		HashMap<String, String> params = new HashMap<String, String>();
		if (true) {
			if (new UserLogic().isInstallRole(this.getUser())) {
				StringBuffer sb = new StringBuffer();
				sb.append(" and c.instore='").append(this.getUserName()).append("'");
				params.put("PInstore", sb.toString());
			} else {
				params.put("PInstore", "");
			}
		}
		return params;
	}
	
	/**
	 * 加、减备货库存条件
	 */
	public HashMap<String, String> getParam4Agent(){
		Set<String> commnameList = new LinkedHashSet<String>();
		HashMap<String, String> params = new HashMap<String, String>();
		String k="limitCommName", c="c.commName";
		Iterator<BomDetail> iter = this.getAgentList().iterator();
		StringBuffer sb = new StringBuffer();
		if (iter.hasNext()==false) {
			sb.append("1=1");
		} else {
			sb.append("(");
			for (; iter.hasNext();) {
				Commodity item = iter.next().getCommodity();
				String comm = new CommodityLogic().getPropertyChoosableLogic().toTrunk(item).getCommName();
				if (commnameList.add(comm))
					sb.append(c).append("='").append(comm).append("'").append(" or ");
			}
			sb.delete(sb.length()-4, sb.length()).append(")");
		}
		params.put(k, sb.toString());
		SupplierT tsupplier = new SupplierLogic().getPropertyChoosableLogic().toTrunk(this.getBomDetail().getSupplier());
		params.put("agent", tsupplier.getSupplierName()==null? "空": tsupplier.getSupplierName());
		return params;
	}
	
	public Set<String> getParam4Store(Commodity... commodityList) {
		Set<String> commList = new LinkedHashSet<String>();
		List<FieldBuilder> colBuilders = new CommColLogic().getColBuilders();
		Commodity emptyCommodity = new Commodity();
		for (Commodity commodity0: commodityList) {
			Commodity commodity = TicketPropertyUtil.copyFieldsSkip(commodity0, new Commodity());
			for (FieldBuilder builder: colBuilders) {
				Object emptyValue = builder.getProperty().getDynaProperty().get(emptyCommodity);
				builder.getProperty().getDynaProperty().set(commodity, emptyValue);
			}
			String commName = new CommodityLogic().getPropertyChoosableLogic().toTrunk(commodity).getCommName();
			commList.add(commName);
		}
		return commList;
	}
	
	public HashMap<String, String> getParam4Month() {
		HashMap<String, String> params = new HashMap<String, String>();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date endDate = calendar.getTime();
		calendar.add(Calendar.MONTH, -1);
		Date startDate = calendar.getTime();
		this.setAttr("StartDate", startDate);
		calendar.add(Calendar.MONTH, -1);
		Date previous = calendar.getTime();
		params.put("startDate", new StringBuffer().append("'").append(new SimpleDateFormat("yyyy-MM-dd").format(startDate)).append("'").toString());
		params.put("endDate", new StringBuffer().append("'").append(new SimpleDateFormat("yyyy-MM-dd").format(endDate)).append("'").toString());
		params.put("previous", new StringBuffer().append("'").append(new SimpleDateFormat("yyyy-MM-dd").format(previous)).append("'").toString());
		return params;
	}
	
	public HashMap<String, String> getParam4MonthCurrent() {
		HashMap<String, String> params = new HashMap<String, String>();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1);
		Date endDate = calendar.getTime();
		calendar.add(Calendar.MONTH, -1);
		Date startDate = calendar.getTime();
		this.setAttr("StartDate", startDate);
		calendar.add(Calendar.MONTH, -1);
		Date previous = calendar.getTime();
		params.put("startDate", new StringBuffer().append("'").append(new SimpleDateFormat("yyyy-MM-dd").format(startDate)).append("'").toString());
		params.put("endDate", new StringBuffer().append("'").append(new SimpleDateFormat("yyyy-MM-dd").format(endDate)).append("'").toString());
		params.put("previous", new StringBuffer().append("'").append(new SimpleDateFormat("yyyy-MM-dd").format(previous)).append("'").toString());
		return params;
	}
	
	private OrderDetail getDetailById(long id) {
		for (OrderDetail d: this.getDetailList()) {
			if (d.getId()>0 && d.getId()==id) {
				return d;
			}
		}
		return null;
	}

	public OrderDetail getDomain() {
		return this.getDetailList().isEmpty()? null: this.getDetailList().get(0);
	}
	
	private CommColForm getCommColForm() {
		CommColForm form = this.getAttr(CommColForm.class);
		if (form == null) {
			form = new CommColForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private StoreTicketForm getSelfForm() {
		return this;
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
		this.getDetailList().clear();
		this.getDetailList().addAll(selected);
	}
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private void setReceiptState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setReceiptId(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
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
	
	private void setAgentState(State state, ViewData<BomDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (BomDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStBom(stateId);
				d.setStateName(stateName);
			}
		}
	}
	
	private void setReceiptUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreceipt(suser);
		}
	}
	
	public void setRestore4Service(ViewData<StoreItem> viewData) {
		viewData.setTicketDetails((List<StoreItem>)this.getAttr("StoreItemList"));
		viewData.setParam("DeleteList", (List<StoreItem>)this.getAttr("StoreItem4DeleteList"));
	}
	
	public void setInExtra4Service(List<OrderDetail> itemList, Object... filterList0) {
		StoreTicketForm form = this;
		form.getExtraInstoreList().clear();
		form.getExtraInstoreList().addAll(itemList);
		if (itemList.size()==0) {
			this.setAttr("InstorePurchaseList", new ArrayList<OrderDetail>(0));
			this.setAttr("InstorePurchase4UpdateList", new ArrayList<OrderDetail>(0));
			this.setAttr("InstorePurchase4CreateList", new ArrayList<OrderDetail>(0));
			this.setAttr("InstorePurchase4DeleteList", new ArrayList<OrderDetail>(0));
			return;
		}
		ViewBuilder vbuilder = EntityClass.loadViewBuilder(form.getClass(), "InExtraList");
		SqlListBuilder sbuilder = (SqlListBuilder)vbuilder.getFieldBuildersDeep(SqlListBuilder.class).get(0);
		form.getSearchSetting(sbuilder);
		this.setFilters(sbuilder, filterList0);
		Component vcomp = new EditViewer(vbuilder, form, 1024).createView().getComponent();
		new Window().add(vcomp);
		vcomp.getInnerComponentList(Menu.class).get(0).getEventListenerList().fireListener();
		form.getSearchSetting(sbuilder);
		this.setAttr(form);
	}
	
	public void setOutExtra4Service(List<OrderDetail> outList, Object... filterList0) {
		StoreTicketForm form = this;
		form.getExtraOutstoreList().clear();
		form.getExtraOutstoreList().addAll(outList);
		if (outList.size()==0) {
			this.setAttr("OutstorePurchaseList", new ArrayList<OrderDetail>(0));
			this.setAttr("OutstorePurchase4UpdateList", new ArrayList<OrderDetail>(0));
			this.setAttr("OutstorePurchase4CreateList", new ArrayList<OrderDetail>(0));
			this.setAttr("OutstorePurchase4DeleteList", new ArrayList<OrderDetail>(0));
			return;
		}
		"123".toCharArray();
		ViewBuilder vbuilder = EntityClass.loadViewBuilder(form.getClass(), "OutExtraList");
		SqlListBuilder sbuilder = (SqlListBuilder)vbuilder.getFieldBuildersDeep(SqlListBuilder.class).get(0);
		form.getSearchSetting(sbuilder);
		this.setFilters(sbuilder, filterList0);
		Component vcomp = new EditViewer(vbuilder, form, 1024).createView().getComponent();
		new Window().add(vcomp);
		vcomp.getInnerFormerList(ListView.class);
		vcomp.getInnerComponentList(Menu.class).get(0).getEventListenerList().fireListener();
		form.getSearchSetting(sbuilder);
		this.setAttr(form);
		StringBuffer sb = new StringBuffer();
		for (OrderDetail storeCreate: (List<OrderDetail>)this.getAttr("OutstorePurchase4CreateList")) {
			String comm = new CommodityLogic().getPropertyChoosableLogic().toTrunk(storeCreate.getCommodity()).getTrunk();
			sb.append("第").append(storeCreate.getClient().getId()).append("行").append(comm).append(storeCreate.getAmount()).append("，");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).append("不够库存出库。").toString());
		for (OrderDetail item: outList) {
			OrderDetail order=item.getVoparam("OutstoreOrder"), sorder=order.getVoparam(OrderDetail.class), fromStore=order.getVoparam("FromStore");
			LinkedHashSet<OrderDetail> orderSplits = sorder.getVoparam("OrderSplitList");
			StringBuffer purNumbers = new StringBuffer();
			double storeMoney=0, splitAmount=0;
			Assert.assertTrue("有使用库存拆分", orderSplits.size()>0 && item.getAmount()>0);
			for (Iterator<OrderDetail> spIter=orderSplits.iterator(); spIter.hasNext();) {
				OrderDetail split = spIter.next();
				purNumbers.append("采购库存").append(split.getPurchaseTicket().getNumber()).append("用").append(split.getAmount()).append("价").append(split.getReceiptTicket().getStorePrice()).append("，");
				storeMoney += split.getReceiptTicket().getStorePrice() * split.getAmount();
				splitAmount += split.getAmount();
				Assert.assertTrue("拆分的订单有采购收货", split.getAmount()>0 && split.getAmount()<=item.getAmount()
						&& split.getPurchaseTicket().getNumber()!=null && split.getReceiptTicket().getNumber()!=null && split.getStPurchase()>=30 && split.getReceiptId()>=30);
				if (spIter.hasNext()==false) {
					new PurchaseTicketLogic().setPurchaseTicket(split, item);
					item.getReceiptTicket().setStorePrice(storeMoney / item.getAmount());
					item.getReceiptTicket().setRemark(purNumbers.toString());
					item.setStPurchase(0);
					Assert.assertTrue("拆分后合数量未超出", splitAmount <= item.getAmount());
				}
			}
		}
	}
	
	private void setFilters(SqlListBuilder sqlBuilder, Object... filterList) {
		SqlListBuilderSetting setting = this.getSqlListBuilderSetting(sqlBuilder);
		HashMap<String, String> filters = new HashMap<String, String>();
		for (int i=0; i<filterList.length; i+=2) {
			String name=(String)filterList[i];
			Object input=filterList[i+1];
			List<Fields.CondItem> condItems = (input instanceof String)==false? new ArrayList<Fields.CondItem>(0): new CondReg().getCondItems((String)input);
			if (input instanceof Object[]) {
				StringBuffer fitems = new StringBuffer();
				for (Object f: (Object[])input) {
					fitems.append("=").append(f).append(" ");
				}
				filters.put(name, fitems.toString());
			} else if (condItems.size()>0 && (Boolean)ReflectHelper.invokeMethod(condItems.get(0), "hasSymbol", new Object[0])==true) {
				filters.put(name, (String)input);
			} else
				filters.put(name, "=".concat(input+""));
		}
		setting.addFilters(filters);
		List<String> nameList = new ArrayList<String>();
		for (int i=0; i<filterList.length; nameList.add((String)filterList[i]), i+=2);
		for (ColumnField col: setting.getColumnMap(sqlBuilder, setting.getFilters()).keySet()) {
			nameList.remove(col.getName());
		}
		if (nameList.size()>0)
			throw new LogicException(2, new StringBuffer("查询").append(sqlBuilder.getFullViewName()).append("没有配置列").append(nameList).toString());
	}
	
	public void setInExtra4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> createList=(List<OrderDetail>)this.getAttr("InstorePurchase4CreateList"), saveList=(List<OrderDetail>)this.getAttr("InstorePurchaseList"), deleteList=(List<OrderDetail>)this.getAttr("InstorePurchase4DeleteList"), updateList=(List<OrderDetail>)this.getAttr("InstorePurchase4UpdateList");
		viewData.setParam("CreateList", createList);
		viewData.setTicketDetails(createList);
		viewData.setParam("SaveList", saveList);
		viewData.setParam("UpdateList", updateList);
		viewData.setParam("DeleteList", deleteList);
	}
	public void setOutExtra4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> createList=(List<OrderDetail>)this.getAttr("OutstorePurchase4CreateList"), saveList=(List<OrderDetail>)this.getAttr("OutstorePurchaseList"), deleteList=(List<OrderDetail>)this.getAttr("OutstorePurchase4DeleteList"), updateList=(List<OrderDetail>)this.getAttr("OutstorePurchase4UpdateList");
		viewData.setParam("CreateList", createList);
		viewData.setTicketDetails(createList);
		viewData.setParam("SaveList", saveList);
		viewData.setParam("UpdateList", updateList);
		viewData.setParam("DeleteList", deleteList);
	}
	
	public void setExtraCount4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
	}
	
	public void setInAgent4Service(BomDetail agent, List<BomDetail> itemList) {
		StoreTicketForm form = this;
		form.getAgentInstoreList().clear();
		form.getAgentInstoreList().addAll(itemList);
		form.setBomDetail(agent);
		if (itemList.size()>0) {
			ViewBuilder vbuilder = EntityClass.loadViewBuilder(form.getClass(), "InAgentList");
			for (SqlListBuilder sbuilder: (List<SqlListBuilder>)vbuilder.getFieldBuildersDeep(SqlListBuilder.class)) {
				form.getSearchSetting(sbuilder);
			}
			Component vcomp = new EditViewer(vbuilder, form, 1024).createView().getComponent();
			new Window().add(vcomp);
			vcomp.getInnerComponentList(Menu.class).get(0).getEventListenerList().fireListener();
			this.setAttr(form);
		} else {
			this.setAttr("KeepBom4CreateList", null);
			this.setAttr("KeepBomList", null);
			this.setAttr("KeepBom4DeleteList", null);
		}
	}
	
	public void setOutAgent4Service(BomDetail agent, List<BomDetail> outList, Object... filterList0) {
		StoreTicketForm form = this;
		form.getAgentOutstoreList().clear();
		form.getAgentOutstoreList().addAll(outList);
		form.setBomDetail(agent);
		if (outList.size()==0) {
			this.setAttr("KeepBom4CreateList", null);
			this.setAttr("KeepBomList", null);
			this.setAttr("KeepBom4DeleteList", null);
			return;
		}
		if (outList.size()>0) {
			ViewBuilder vbuilder = EntityClass.loadViewBuilder(form.getClass(), "OutAgentList");
			SqlListBuilder sbuilder = (SqlListBuilder)vbuilder.getFieldBuildersDeep(SqlListBuilder.class).get(0);
			form.getSearchSetting(sbuilder);
			this.setFilters(sbuilder, filterList0);
			Component vcomp = new EditViewer(vbuilder, form, 1024).createView().getComponent();
			new Window().add(vcomp);
			vcomp.getInnerComponentList(Menu.class).get(0).getEventListenerList().fireListener();
			form.getSearchSetting(sbuilder);
			this.setAttr(form);
		}
		StringBuffer sb = new StringBuffer();
		for (BomDetail storeCreate: (List<BomDetail>)this.getAttr("KeepBom4CreateList")) {
			String comm = new CommodityLogic().getPropertyChoosableLogic().toTrunk(storeCreate.getCommodity()).getTrunk();
			sb.append("第").append(storeCreate.getClient().getId()).append("行").append(comm).append(storeCreate.getBomTicket().getKeepAmount()).append("，");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).append("不够库存出库。").toString());
		for (BomDetail item: outList) {
			BomDetail order=item.getVoparam("OutstoreOrder"), fromStore=order.getVoparam("FromStore");
			LinkedHashSet<BomDetail> orderSplits = item.getVoparam("OrderSplitList");
			StringBuffer purNumbers = new StringBuffer();
			double splitAmount=0;
			Assert.assertTrue("有使用库存拆分", orderSplits.size()>0 && item.getBomTicket().getKeepAmount()>0);
			for (Iterator<BomDetail> spIter=orderSplits.iterator(); spIter.hasNext();) {
				BomDetail split = spIter.next();
				purNumbers.append("采购库存").append(split.getPurchaseTicket().getNumber()).append("用").append(split.getBomTicket().getKeepAmount()).append("价").append(split.getReceiptTicket().getStorePrice()).append("，");
				splitAmount += split.getBomTicket().getKeepAmount();
				Assert.assertTrue("拆分的订单", split.getBomTicket().getKeepAmount()>0 && split.getBomTicket().getKeepAmount()<=item.getBomTicket().getKeepAmount());
				if (spIter.hasNext()==false) {
					item.getReceiptTicket().setRemark(purNumbers.toString());
					item.setStPurchase(0);
					Assert.assertTrue("拆分后合数量未超出", splitAmount <= item.getBomTicket().getKeepAmount());
				}
			}
		}
	}
	
	public void setAgentAdd4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> createList=(List<BomDetail>)this.getAttr("KeepBom4CreateList"), saveList=(List<BomDetail>)this.getAttr("KeepBomList"), deleteList=(List<BomDetail>)this.getAttr("KeepBom4DeleteList"), updateList=(List<BomDetail>)this.getAttr("KeepBom4UpdateList");
		viewData.setParam("CreateList", createList);
		viewData.setParam("SaveList", saveList);
		viewData.setTicketDetails(saveList);
		viewData.setParam("UpdateList", updateList);
		viewData.setParam("DeleteList", deleteList);
	}
	
	public void setAgentCount4Service(ViewData<BomDetail> viewData) {
		viewData.setTicketDetails(this.getAgentList());
	}
	
	public void setMonth4Service(ViewData<StoreMonth> viewData) {
		viewData.setTicketDetails((List<StoreMonth>)this.getAttr("StoreMonthList"));
		viewData.setParam("DeleteList", (List<StoreMonth>)this.getAttr("StoreMonth4DeleteList"));
	}
	
	public void setEnough4Service(ViewData<StoreEnough> viewData) {
		viewData.setTicketDetails((List<StoreEnough>)this.getAttr("StoreEnoughList"));
		viewData.setParam("DeleteList", (List<StoreEnough>)this.getAttr("StoreEnough4DeleteList"));
	}
	
	private void setRestoreCrossCount() throws Exception {
		if (this.getSelectFormer4StoreItem().getSelectedList().size()>0) {
			for (StoreItem store: this.getSelectFormer4StoreItem().getSelectedList()) {
				TicketPropertyUtil.copyFieldsSkip(new StoreItem(), store);
			}
		}
		Iterator<StoreItem> sourceIter = this.getSelectFormer4StoreItem().getSelectedList().iterator();
		SelectTicketFormer4Cross<StoreTicketForm> cross = getSelectCross4Store();
		List<StoreItem> storeAll = new ArrayList<StoreItem>();
		if (cross.getRowCounts().size()>0) {
			StringBuffer ids = new StringBuffer();
			for (Iterator<List<Object>> countIter=cross.getRowCounts().iterator(); countIter.hasNext();) {
				List<Object> counts=countIter.next();
				ids.append(counts.get(0)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new PurchaseTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				OrderDetail detail = detailMap.get(Long.valueOf(rowCounts.get(0).toString()));
				StoreItem store = sourceIter.hasNext()? sourceIter.next(): new StoreItem();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setAmount(new DoubleType().parse(rowCounts.get(1).toString()));
				store.setMoney(new DoubleType().parse(rowCounts.get(2).toString()));
				store.setBackAmount(new DoubleType().parse(rowCounts.get(3).toString()));
				storeAll.add(store);
			}
		}
		int comi=0;
		for (StoreItem item: storeAll) {
			item.getCommodity().setId(++comi);
		}
		getStoreCountList().clear();
		getStoreCountList().addAll(storeAll);
	}
	
	private void getRestoreFromCross() throws Exception {
		List<StoreItem> tostoreList = new ArrayList<StoreItem>();
		if (true) {
			int irow = 0;
			StringBuffer sb = new StringBuffer();
			for (Iterator<List<Object>> labelIter=this.getSelectCross4Store().getBodyRows().iterator(),rowIter=this.getSelectCross4Store().getRowCounts().iterator(),crossIter=this.getSelectCross4Store().getBodyCross().iterator(); rowIter.hasNext(); irow++) {
				List<Object> rowLabel=labelIter.next(), rowcountItem=rowIter.next(), rowcrossItem=crossIter.next();
				long id = Long.valueOf(rowcountItem.get(0).toString());
				StoreItem store = null;
				for (StoreItem d: this.getStoreCountList()) {
					if (d.getCommodity().getId() == id) {
						store = d;
						break;
					}
				}
				// clear commodity col values
				Commodity emptyCommodity = new Commodity();
				for (Iterator<FieldBuilder> fiter=new CommColLogic().getColBuilders().iterator(); fiter.hasNext();) {
					FieldBuilder fbuilder = fiter.next();
					DynaProperty property =  fbuilder.getProperty().getDynaProperty();
					Object value = property.get(emptyCommodity);
					property.set(store.getCommodity(), value);
				}
				// set from PurchaseDetail.monthNum list
				List<FieldBuilder> rowBuilders = new CommColLogic().getRowBuilders();
				StringBuffer nums = new StringBuffer();
				for (OrderDetail d: this.getSelectFormer4Purchase().getSelectedList()) {
					EqualsBuilder equals = new EqualsBuilder();
					for (Iterator<FieldBuilder> biter=rowBuilders.iterator(); biter.hasNext();) {
						FieldBuilder ritem = biter.next();
						DynaProperty prop = ritem.getProperty().getDynaProperty();
						equals.append(prop.get(store.getCommodity()), prop.get(d.getCommodity()));
						if (equals.isEquals()==false) {
							break;
						} else if (biter.hasNext()==false) {
							nums.append(d.getMonthnum()).append(",");
						}
					}
				}
				if (nums.length()>0)		store.setMonthnums(nums.deleteCharAt(nums.length()-1).toString());
				// count
				store.setAmount(Double.valueOf(rowcountItem.get(1).toString()));
				if (store.getAmount()==0)
					continue;
				store.setMoney(Double.valueOf(rowcountItem.get(2).toString()));
				store.setBackAmount(Double.valueOf(rowcountItem.get(3).toString()));
				store.setPrice(store.getMoney()/store.getAmount());
				// Yards,Amount
				//		set null
				for (int ci=12; ci>0; store.setColAmount(ci, null, 0), ci--);
				Yards yards = new YardsLogic().getYardsByCType(store.getCommodity().getCommType2());
				int coli=0, yardi=0;
				sb.append("\n").append(rowLabel);
				for (Iterator<List<Object>> coltitleIter=this.getSelectCross4Store().getHeadColumns().iterator(); coltitleIter.hasNext(); coli++) {
					List<Object> coltitle = coltitleIter.next();
					List<Object> crossList = ListDynaBean.asList(rowcrossItem.get(coli));
					double crossAmount = crossList.size()==0? 0: new DoubleType().parse(crossList.get(0).toString());
					if (crossAmount==0)
						continue;
					StringBuffer colName = new StringBuffer();
					for (Object t: coltitle) {
						if (t==null)
							continue;
						colName.append(t).append(",");
					}
					if (colName.length()>0)
						colName.deleteCharAt(colName.length()-1);
					if (yards!=null) {
						yardi = yards.getColList().indexOf(colName.toString());
						if (yardi==-1)		yardi=8+store.getColNamesF9().size();
						yardi++;
					} else {
						yardi++;
					}
					sb.append(",").append(colName).append("_").append(yardi).append(",").append(crossAmount).append("，");
					store.setColAmount(yardi, colName.toString(), crossAmount);
				}
				tostoreList.add(store);
			}
//			LogUtil.error(sb.insert(0, "库存分码数打横显示").toString());
		}
		List<StoreItem> deleteList = this.getSelectFormer4StoreItem().getSelectedList();
		deleteList.removeAll(tostoreList);
		this.setAttr("StoreItemList", tostoreList);
		this.setAttr("StoreItem4DeleteList", deleteList);
	}
	
	private void setInExtraDetailList() {
		int commodityi = 0;
		for (OrderDetail d: this.getSelectFormer4Purchase().getSelectedList()) {
			d.getCommodity().setId(++commodityi);
		}
		for (OrderDetail d: this.getDetailList()) {
			d.getCommodity().setId(++commodityi);
		}
		this.getDetailList().addAll(this.getSelectFormer4Purchase().getSelectedList());
	}
	
	private void getInExtraFromCross() throws Exception {
		List<OrderDetail> updateList=new ArrayList<OrderDetail>(), createList=new ArrayList<OrderDetail>(), deleteList=new ArrayList<OrderDetail>();
		SelectTicketFormer4Cross cross = this.getSelectCross4Store();
		for (Iterator<List<Object>> riter=cross.getBodyRows().iterator(),rciter=cross.getBodyCross().iterator(),rcntIter=cross.getRowCounts().iterator(); riter.hasNext();) {
			List<Object> row=riter.next();
			List<List<Object>> rcross = (List<List<Object>>)(Object)rciter.next();
			List<Object> rcount=rcntIter.next();
			int ci=0;
			List<OrderDetail> inList = new ArrayList<OrderDetail>();
			List<OrderDetail> storeList = new ArrayList<OrderDetail>();
			for (Iterator<List<Object>> citer=rcross.iterator(); citer.hasNext(); ci++) {
				List<Object> colCross = citer.next();
				if (colCross==null)
					continue;
				List<Long> colHead = (List<Long>)cross.getHeadColumns().get(ci);
				Long commodityi=colHead.get(0), storeId=colHead.get(1);
				for (OrderDetail d: this.getDetailList()) {
					if (d.getCommodity().getId()==commodityi) {
						if (storeId==0)
							inList.add(d);
						else
							storeList.add(d);
						break;
					}
				}
			}
			if ("一个商品整合".length()>0) {
				Long commId = new LongType().parse(rcount.get(0).toString());
				OrderDetail detail = null;
				for (OrderDetail d: this.getDetailList()) {
					if (d.getCommodity().getId()==commId) {
						detail = d;
						break;
					}
				}
				detail.setAmount(new DoubleType().parse(rcount.get(1).toString()));
				if (new DoubleType().parse(rcount.get(2).toString())>0 && detail.getAmount()>0)
					detail.getReceiptTicket().setStorePrice(new DoubleType().parse(rcount.get(2).toString()) / detail.getAmount());
				if (detail.getId()==0 && detail.getAmount()>0)
					createList.add(detail);
				else if (detail.getId()>0 && detail.getAmount()>0)
					updateList.add(detail);
				else if (detail.getId()>0 && detail.getAmount()==0)
					deleteList.add(detail);
				storeList.remove(detail);
				deleteList.addAll(storeList);
			}
		}
		getDomain().getReceiptTicket().genSerialNumber();
		for (OrderDetail d: createList) {
			if (d.getReceiptTicket().getReceiptDate()==null)
				d.getReceiptTicket().setReceiptDate(new Date());
		}
		for (OrderDetail d: deleteList) {
			d.setAmount(0);
			d.setStPurchase(0);
		}
		List<OrderDetail> saveList = new ArrayList<OrderDetail>(updateList);
		saveList.addAll(createList);
		saveList.addAll(deleteList);
		Assert.assertTrue("saveSize = updateSize + deleteSize + createSize", saveList.size() == updateList.size() + deleteList.size() + createList.size());
		this.setAttr("InstorePurchaseList", saveList);
		this.setAttr("InstorePurchase4UpdateList", updateList);
		this.setAttr("InstorePurchase4DeleteList", deleteList);
		this.setAttr("InstorePurchase4CreateList", createList);
	}

	private void setOutExtraDetailList() {
		int commodityi = 0;
		this.getDetailList().addAll(this.getSelectFormer4Purchase().getSelectedList());
		for (OrderDetail d: this.getDetailList()) {
			d.getCommodity().setId(++commodityi);
		}
	}
	
	private void getOutExtraFromCross() throws Exception {
		SelectTicketFormer4Cross cross = this.getSelectCross4Store();
		LinkedHashSet<OrderDetail> orderSplitList=new LinkedHashSet<OrderDetail>(), purSplitList=new LinkedHashSet<OrderDetail>();
		ArrayList<OrderDetail> createList=new ArrayList<OrderDetail>(), deleteList=new ArrayList<OrderDetail>(), updateList=new ArrayList<OrderDetail>();
		String kOrderSplit="OrderSplitList", kOrderYes="OrderYes", kOrderNo="OrderNo";
		for (Iterator<List<Object>> riter=cross.getBodyRows().iterator(),rciter=cross.getBodyCross().iterator(); riter.hasNext();) {
			List<Object> row=riter.next();
			List<List<Object>> rcross = (List<List<Object>>)(Object)rciter.next();
			int ci=0;
			List<OrderDetail> outList = new ArrayList<OrderDetail>();
			List<OrderDetail> storeList = new ArrayList<OrderDetail>();
			for (Iterator<List<Object>> citer=rcross.iterator(); citer.hasNext(); ci++) {
				List<Object> colCross = citer.next();
				if (colCross==null)
					continue;
				List<Long> colHead = (List<Long>)cross.getHeadColumns().get(ci);
				Long commodityi=colHead.get(0), storeId=colHead.get(1);
				for (OrderDetail d: this.getDetailList()) {
					if (d.getCommodity().getId()==commodityi) {
						if (storeId==0)
							outList.add(d);
						else
							storeList.add(d);
						break;
					}
				}
			}
			Iterator<OrderDetail> ordIter=outList.iterator(), purIter=storeList.iterator();
			for (; ordIter.hasNext() && purIter.hasNext();) {
				OrderDetail order=ordIter.next(), sorder=order.getVoparam(OrderDetail.class), purchase=purIter.next();
				orderSplitList.add(order);
				purSplitList.add(purchase);
				LinkedHashSet<OrderDetail> orderSplits = sorder.getVoparam(kOrderSplit);
				LinkedHashSet<Double> orderYes=null;
				if ("要出库的1订单分成了哪几个".length()>0) {
					if (orderSplits==null) {
						orderSplits = new LinkedHashSet<OrderDetail>();
						orderYes = new LinkedHashSet<Double>();
						sorder.getVoParamMap().put(kOrderSplit, orderSplits);
						sorder.getVoParamMap().put(kOrderYes, orderYes);
					} else
						orderYes = sorder.getVoparam(kOrderYes);
				}
				if (order.getAmount() > purchase.getAmount()) {
					String nwCurSplitMonthnum = new OrderTicketLogic().getSplitMonthnum(order.getMonthnum());
					OrderDetail selfRemainOrder = order;
					OrderDetail nwCurOrder = new OrderTicketLogic().genCloneOrder(order);
					nwCurOrder.setVoparam(sorder);
					selfRemainOrder.setAmount(order.getAmount() - purchase.getAmount());
					nwCurOrder.setMonthnum(nwCurSplitMonthnum);
					nwCurOrder.setAmount(purchase.getAmount());
					nwCurOrder.getVoParamMap().put("FromStore", purchase);
					orderYes.add(purchase.getAmount());
					orderSplits.add(nwCurOrder);
					new PurchaseTicketLogic().setPurchaseTicket(purchase, nwCurOrder);
					// 剩余无库存订单加入待处理队列
					List<OrderDetail> list = new ArrayList<OrderDetail>(1);
					list.add(selfRemainOrder);
					ordIter = new JoinedIterator(list.iterator(), ordIter);
					purchase.setAmount(0);
				} else if (order.getAmount() < purchase.getAmount()) {
					String nwCurSplitMonthnum = new OrderTicketLogic().getSplitMonthnum(purchase.getMonthnum());
					OrderDetail selfRemainPur = purchase;
					OrderDetail nwCurPur = new PurchaseTicketLogic().genClonePurchase(purchase);
					nwCurPur.setAmount(0);
					nwCurPur.setMonthnum(nwCurSplitMonthnum);
					order.getVoParamMap().put("FromStore", nwCurPur);
					orderYes.add(order.getAmount());
					orderSplits.add(order);
					new PurchaseTicketLogic().setPurchaseTicket(nwCurPur, order);
					// 剩余库存加入供用库存队列
					List<OrderDetail> list = new ArrayList<OrderDetail>(1);
					list.add(selfRemainPur);
					purIter = new JoinedIterator(list.iterator(), purIter);
					selfRemainPur.setAmount(purchase.getAmount()-order.getAmount());
					purSplitList.add(nwCurPur);
				} else {
					order.getVoParamMap().put("FromStore", purchase);
					orderYes.add(purchase.getAmount());
					orderSplits.add(order);
					new PurchaseTicketLogic().setPurchaseTicket(purchase, order);
					purchase.setAmount(0);
				}
			}
			for (; ordIter.hasNext(); createList.add(ordIter.next()));
			for (OrderDetail cur=null, next=null; purIter.hasNext(); cur=next, next=null) {
				next = purIter.next();
				purSplitList.add(next);
				if (cur==null || next==null)
					continue;
				if (cur.getId()!=next.getId())
					break;
			}
		}
		//不够出库的待出库明细
		for (OrderDetail c: createList) {
			c.setAmount(c.getAmount() * -1);
			new PurchaseTicketLogic().setPurchaseTicket(new OrderDetail(), c);
		}
		for (Iterator<OrderDetail> pIter=purSplitList.iterator(); pIter.hasNext();) {
			OrderDetail pur = pIter.next();
			if (pur.getAmount()==0 && pur.getId()==0)
				pIter.remove();
			else if (pur.getAmount()==0 && pur.getId()>0)
				deleteList.add(pur);
			else if (pur.getAmount()>0 && pur.getId()>0)
				updateList.add(pur);
		}
		Assert.assertTrue("saveSize = updateSize + deleteSize", purSplitList.size() == updateList.size() + deleteList.size());
		this.setAttr("OutstorePurchaseList", new ArrayList<OrderDetail>(purSplitList));
		this.setAttr("OutstorePurchase4UpdateList", updateList);
		this.setAttr("OutstorePurchase4DeleteList", deleteList);
		this.setAttr("OutstorePurchase4CreateList", createList);
		if ("Order原明细有库存数、无库存数".length()>0) {
			for (OrderDetail sorder: this.getExtraOutstoreList()) {
				LinkedHashSet<OrderDetail> orderSplit = sorder.getVoparam(kOrderSplit);
				LinkedHashSet<Double> orderYes = sorder.getVoparam(kOrderYes);
				if (orderSplit!=null) {
					double dyes=0d, dno=0d;
					for (Double d: orderYes) {
						dyes += d;
					}
					dno = sorder.getAmount() - dyes;
					sorder.getVoParamMap().put(kOrderYes, dyes);
					sorder.getVoParamMap().put(kOrderNo, dno);
					Assert.assertTrue("Order拆分，有库存的数量，无库存的数量", orderSplit.size()>=1 && orderYes!=null && dyes<=sorder.getAmount() && dyes+dno==sorder.getAmount());
				}
			}
		}
	}

	private void setInAgentList() {
		this.beforeInstoreAgent(null);
		int commodityi = 0;
		for (BomDetail d: this.getSelectFormer4Bom().getSelectedList()) {
			d.getCommodity().setId(++commodityi);
		}
		for (BomDetail d: this.getAgentList()) {
			d.getCommodity().setId(++commodityi);
		}
		this.getAgentList().addAll(this.getSelectFormer4Bom().getSelectedList());
	}
	
	private void getInAgentFromCross() throws Exception {
		List<BomDetail> updateList=new ArrayList<BomDetail>(), createList=new ArrayList<BomDetail>(), deleteList=new ArrayList<BomDetail>();
		SelectTicketFormer4Cross cross = this.getSelectCross4Agent();
		for (Iterator<List<Object>> riter=cross.getBodyRows().iterator(),rciter=cross.getBodyCross().iterator(),rcntIter=cross.getRowCounts().iterator(); riter.hasNext();) {
			List<Object> row=riter.next();
			List<List<Object>> rcross = (List<List<Object>>)(Object)rciter.next();
			List<Object> rcount=rcntIter.next();
			int ci=0;
			List<BomDetail> inList = new ArrayList<BomDetail>();
			List<BomDetail> storeList = new ArrayList<BomDetail>();
			for (Iterator<List<Object>> citer=rcross.iterator(); citer.hasNext(); ci++) {
				List<Object> colCross = citer.next();
				if (colCross==null)
					continue;
				List<Long> colHead = (List<Long>)cross.getHeadColumns().get(ci);
				Long commodityi=colHead.get(0), storeId=colHead.get(1);
				for (BomDetail d: this.getAgentList()) {
					if (d.getCommodity().getId()==commodityi) {
						if (storeId==0)
							inList.add(d);
						else
							storeList.add(d);
						break;
					}
				}
			}
			if ("一个商品整合".length()>0) {
				Long commId = new LongType().parse(rcount.get(0).toString());
				BomDetail detail = null;
				for (BomDetail d: this.getAgentList()) {
					if (d.getCommodity().getId()==commId) {
						detail = d;
						break;
					}
				}
				detail.getBomTicket().setKeepAmount(new DoubleType().parse(rcount.get(1).toString()));
				if (new DoubleType().parse(rcount.get(2).toString())>0 && detail.getBomTicket().getKeepAmount()>0)
					detail.getReceiptTicket().setStorePrice(new DoubleType().parse(rcount.get(2).toString()) / detail.getBomTicket().getKeepAmount());
				if (detail.getId()==0 && detail.getBomTicket().getKeepAmount()>0)
					createList.add(detail);
				else if (detail.getId()>0 && detail.getBomTicket().getKeepAmount()>0)
					updateList.add(detail);
				else if (detail.getId()>0 && detail.getBomTicket().getKeepAmount()==0) {
					deleteList.add(detail);
				}
				storeList.remove(detail);
				deleteList.addAll(storeList);
			}
		}
		for (BomDetail d: createList) {
			if (d.getReceiptTicket().getReceiptDate()==null)
				d.getReceiptTicket().setReceiptDate(new Date());
		}
		for (BomDetail d: deleteList) {
			d.getBomTicket().setKeepAmount(0);
		}
		List<BomDetail> saveList = new ArrayList<BomDetail>(updateList);
		saveList.addAll(createList);
		saveList.addAll(deleteList);
		Assert.assertTrue("saveSize = updateSize + deleteSize + createSize", saveList.size() == updateList.size() + deleteList.size() + createList.size());
		this.setAttr("KeepBomList", saveList);
		this.setAttr("KeepBom4UpdateList", updateList);
		this.setAttr("KeepBom4DeleteList", deleteList);
		this.setAttr("KeepBom4CreateList", createList);
	}

	private void setOutAgentList() {
		this.beforeOutstoreAgent(null);
		int commodityi = 0;
		this.getAgentList().addAll(this.getSelectFormer4Bom().getSelectedList());
		for (BomDetail d: this.getAgentList()) {
			d.getCommodity().setId(++commodityi);
		}
	}
	
	private void getOutAgentFromCross() throws Exception {
		SelectTicketFormer4Cross cross = this.getSelectCross4Agent();
		LinkedHashSet<BomDetail> orderSplitList=new LinkedHashSet<BomDetail>(), purSplitList=new LinkedHashSet<BomDetail>();
		ArrayList<BomDetail> createList=new ArrayList<BomDetail>(), deleteList=new ArrayList<BomDetail>(), updateList=new ArrayList<BomDetail>();
		String kOrderSplit="OrderSplitList", kOrderYes="OrderYes", kOrderNo="OrderNo";
		for (BomDetail sorder: this.getAgentOutstoreList()) {
			sorder.getVoParamMap().remove(kOrderSplit);
		}
		for (Iterator<List<Object>> riter=cross.getBodyRows().iterator(),rciter=cross.getBodyCross().iterator(); riter.hasNext();) {
			List<Object> row=riter.next();
			List<List<Object>> rcross = (List<List<Object>>)(Object)rciter.next();
			int ci=0;
			List<BomDetail> outList = new ArrayList<BomDetail>();
			List<BomDetail> storeList = new ArrayList<BomDetail>();
			for (Iterator<List<Object>> citer=rcross.iterator(); citer.hasNext(); ci++) {
				List<Object> colCross = citer.next();
				if (colCross==null)
					continue;
				List<Long> colHead = (List<Long>)cross.getHeadColumns().get(ci);
				Long commodityi=colHead.get(0), storeId=colHead.get(1);
				for (BomDetail d: this.getAgentList()) {
					if (d.getCommodity().getId()==commodityi) {
						if (storeId==0)
							outList.add(d);
						else
							storeList.add(d);
						break;
					}
				}
			}
			Iterator<BomDetail> ordIter=outList.iterator(), purIter=storeList.iterator();
			for (; ordIter.hasNext() && purIter.hasNext();) {
				BomDetail order=ordIter.next(), sorder=order.getVoparam(BomDetail.class), purchase=purIter.next();
				orderSplitList.add(order);
				purSplitList.add(purchase);
				LinkedHashSet<BomDetail> orderSplits = sorder.getVoparam(kOrderSplit);
				LinkedHashSet<Double> orderYes=null;
				if ("要出库的1订单分成了哪几个".length()>0) {
					if (orderSplits==null) {
						orderSplits = new LinkedHashSet<BomDetail>();
						orderYes = new LinkedHashSet<Double>();
						sorder.getVoParamMap().put(kOrderSplit, orderSplits);
						sorder.getVoParamMap().put(kOrderYes, orderYes);
					} else
						orderYes = sorder.getVoparam(kOrderYes);
				}
				if (order.getBomTicket().getKeepAmount() > purchase.getBomTicket().getKeepAmount()) {
					String nwCurSplitMonthnum = new OrderTicketLogic().getSplitMonthnum(order.getMonthnum());
					BomDetail selfRemainOrder = order;
					BomDetail nwCurOrder = TicketPropertyUtil.copyProperties(order, new BomDetail());
					nwCurOrder.setVoparam(sorder);
					selfRemainOrder.getBomTicket().setKeepAmount(order.getBomTicket().getKeepAmount() - purchase.getBomTicket().getKeepAmount());
					nwCurOrder.setMonthnum(nwCurSplitMonthnum);
					nwCurOrder.getBomTicket().setKeepAmount(purchase.getBomTicket().getKeepAmount());
					nwCurOrder.getVoParamMap().put("FromStore", purchase);
					orderYes.add(purchase.getBomTicket().getKeepAmount());
					orderSplits.add(nwCurOrder);
					// 剩余无库存订单加入待处理队列
					List<BomDetail> list = new ArrayList<BomDetail>(1);
					list.add(selfRemainOrder);
					ordIter = new JoinedIterator(list.iterator(), ordIter);
					purchase.getBomTicket().setKeepAmount(0);
				} else if (order.getBomTicket().getKeepAmount() < purchase.getBomTicket().getKeepAmount()) {
					String nwCurSplitMonthnum = new OrderTicketLogic().getSplitMonthnum(purchase.getMonthnum());
					BomDetail selfRemainPur = purchase;
					BomDetail nwCurPur = TicketPropertyUtil.copyProperties(purchase, new BomDetail());
					nwCurPur.getBomTicket().setKeepAmount(0);
					nwCurPur.setMonthnum(nwCurSplitMonthnum);
					order.getVoParamMap().put("FromStore", nwCurPur);
					orderYes.add(order.getBomTicket().getKeepAmount());
					orderSplits.add(order);
					// 剩余库存加入供用库存队列
					List<BomDetail> list = new ArrayList<BomDetail>(1);
					list.add(selfRemainPur);
					purIter = new JoinedIterator(list.iterator(), purIter);
					selfRemainPur.getBomTicket().setKeepAmount(purchase.getBomTicket().getKeepAmount()-order.getBomTicket().getKeepAmount());
					purSplitList.add(nwCurPur);
				} else {
					order.getVoParamMap().put("FromStore", purchase);
					orderYes.add(purchase.getBomTicket().getKeepAmount());
					orderSplits.add(order);
					purchase.getBomTicket().setKeepAmount(0);
				}
			}
			for (; ordIter.hasNext(); createList.add(ordIter.next()));
			for (BomDetail cur=null, next=null; purIter.hasNext(); cur=next, next=null) {
				next = purIter.next();
				purSplitList.add(next);
				if (cur==null || next==null)
					continue;
				if (cur.getId()!=next.getId())
					break;
			}
		}
		//不够出库的待出库明细
		for (BomDetail c: createList) {
			c.getBomTicket().setKeepAmount(c.getBomTicket().getKeepAmount() * -1);
		}
		for (Iterator<BomDetail> pIter=purSplitList.iterator(); pIter.hasNext();) {
			BomDetail pur = pIter.next();
			if (pur.getBomTicket().getKeepAmount()==0 && pur.getId()==0)
				pIter.remove();
			else if (pur.getBomTicket().getKeepAmount()==0 && pur.getId()>0)
				deleteList.add(pur);
			else if (pur.getBomTicket().getKeepAmount()>0 && pur.getId()>0)
				updateList.add(pur);
		}
		Assert.assertTrue("saveSize = updateSize + deleteSize", purSplitList.size() == updateList.size() + deleteList.size());
		this.setAttr("KeepBomList", new ArrayList<BomDetail>(purSplitList));
		this.setAttr("KeepBom4UpdateList", updateList);
		this.setAttr("KeepBom4DeleteList", deleteList);
		this.setAttr("KeepBom4CreateList", createList);
		if ("Order原明细有库存数、无库存数".length()>0) {
			for (BomDetail sorder: this.getAgentOutstoreList()) {
				LinkedHashSet<BomDetail> orderSplit = sorder.getVoparam(kOrderSplit);
				LinkedHashSet<Double> orderYes = sorder.getVoparam(kOrderYes);
				if (orderSplit!=null) {
					double dyes=0d, dno=0d;
					for (Double d: orderYes) {
						dyes += d;
					}
					dno = sorder.getBomTicket().getKeepAmount() - dyes;
					sorder.getVoParamMap().put(kOrderYes, dyes);
					sorder.getVoParamMap().put(kOrderNo, dno);
					Assert.assertTrue("Order拆分，有库存的数量，无库存的数量", orderSplit.size()>=1 && orderYes!=null && dyes<=sorder.getBomTicket().getKeepAmount() && dyes+dno==sorder.getBomTicket().getKeepAmount());
				}
			}
		}
	}
	
	private void setEnoughCounts() throws Exception {
		List<StoreEnough> storeAll = new ArrayList<StoreEnough>();
		if (true) {
			List<StoreEnough> sourceList = new ArrayList<StoreEnough>();
			for (StoreEnough source: this.getSelectFormer4Enough().getSelectedList()) {
				source.setInstore(null);
				source.setAmount(0.0);
				source.setStoreAmount(0.0);
				source.setOrderAmount(0.0);
				source.setRequestAmount(0.0);
				source.setOnroadAmount(0.0);
				source.setLockAmount(0.0);
				sourceList.add(source);
			}
			storeAll.addAll(sourceList);
		}
		SelectTicketFormer4Cross<StoreTicketForm> cross = getSelectCross4Store();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new PurchaseTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setStoreAmount(amount);
				store.setAmount(store.getStoreAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4KeepBom();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, BomDetail> detailMap = new HashMap<Long, BomDetail>();
			for (BomDetail detail: new BomTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				BomDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setStoreAmount(amount);
				store.setAmount(store.getStoreAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4Request();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new OrderTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setRequestAmount(amount);
				store.setAmount(store.getRequestAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4Onroad();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new PurchaseTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setOnroadAmount(amount);
				store.setAmount(store.getOnroadAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4Order();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new OrderTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setOrderAmount(amount);
				store.setAmount(0-store.getOrderAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4Bom();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, BomDetail> detailMap = new HashMap<Long, BomDetail>();
			for (BomDetail detail: new BomTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				BomDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setOrderAmount(amount);
				store.setAmount(0-store.getOrderAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4LockBom();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, BomDetail> detailMap = new HashMap<Long, BomDetail>();
			for (BomDetail detail: new BomTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				BomDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setLockAmount(amount);
				store.setOrderAmount(amount);
				store.setAmount(0-store.getLockAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4LockSend();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new PurchaseTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreEnough> storeList = new ArrayList<StoreEnough>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreEnough store = new StoreEnough();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setLockAmount(amount);
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		if (storeAll.size() > 0) {
			int si=0;
			for (StoreEnough store: storeAll) {
				store.getCommodity().setId(++si);
			}
		}
		getEnoughCountList().clear();
		getEnoughCountList().addAll(storeAll);
	}
	
	private void getStoreEnoughs() throws Exception {
		SelectTicketFormer4Cross<StoreTicketForm> former = getSelectCross4EnoughCount();
		List<StoreEnough> storeList = new ArrayList<StoreEnough>();
		for (Iterator<List<Object>> biter=former.getBodyRows().iterator(), citer=former.getRowCounts().iterator(); citer.hasNext();) {
			List<Object> rowLabels=biter.next(), rowCounts=citer.next();
			int ci = 0;
			Long sn = (Long)rowCounts.get(ci++);
			StoreEnough store = null;
			for (StoreEnough item: getEnoughCountList()) {
				if (item.getCommodity().getId() == sn) {
					store = item;
					break;
				}
			}
			String instore = (String)rowLabels.get(0);
			store.setStoreAmount((Double)rowCounts.get(ci++));
			store.setRequestAmount((Double)rowCounts.get(ci++));
			store.setOnroadAmount((Double)rowCounts.get(ci++));
			store.setOrderAmount((Double)rowCounts.get(ci++));
			store.setAmount((Double)rowCounts.get(ci++));
			store.setLockAmount((Double)rowCounts.get(ci++));
			store.setFreeAmount(store.getStoreAmount()-store.getLockAmount());
			if (store.getRequestAmount()+store.getOrderAmount()+Math.abs(store.getAmount())+Math.abs(store.getLockAmount())+Math.abs(store.getFreeAmount()) > 0)
				storeList.add(store);
		}
		this.setAttr("StoreEnoughList", storeList);
		List<StoreEnough> deleteList = new ArrayList<StoreEnough>(getSelectFormer4Enough().getSelectedList());
		deleteList.removeAll(storeList);
		this.setAttr("StoreEnough4DeleteList", deleteList);
	}
	
	private void setMonthCounts() throws Exception {
		List<StoreMonth> storeAll = new ArrayList<StoreMonth>();
		if (getSelectFormer4MonthStart().getSelectedList().size() > 0) {
			List<StoreMonth> sourceList = new ArrayList<StoreMonth>();
			for (StoreMonth store: getSelectFormer4MonthStart().getSelectedList()) {
				store.setInstore(null);
				store.setStartAmount(0.0);
				store.setStartMoney(0.0);
				store.setStartPrice(0.0);
				store.setInAmount(0.0);
				store.setInMoney(0.0);
				store.setInPrice(0.0);
				store.setOutAmount(0.0);
				store.setOutMoney(0.0);
				store.setOutPrice(0.0);
				store.setEndAmount(0.0);
				store.setEndMoney(0.0);
				store.setEndPrice(0.0);
				sourceList.add(store);
			}
			storeAll.addAll(sourceList);
		}
		if (getSelectFormer4MonthPrevious().getSelectedList().size() > 0) {
			List<StoreMonth> startList = new ArrayList<StoreMonth>();
			for (StoreMonth item: getSelectFormer4MonthPrevious().getSelectedList()) {
				StoreMonth store = new StoreMonth();
				store.setCommodity(item.getCommodity());
				store.setStartAmount(item.getEndAmount());
				store.setStartPrice(item.getEndPrice());
				store.setStartMoney(item.getEndMoney());
			}
			storeAll.addAll(startList);
		}
		SelectTicketFormer4Cross<StoreTicketForm> cross = getSelectCross4StoreEnd();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new PurchaseTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreMonth> storeList = new ArrayList<StoreMonth>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)) );
				StoreMonth store = new StoreMonth();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setEndAmount(amount);
				store.setEndMoney(detail.getPurchaseTicket().getPmoney());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4InStore();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new PurchaseTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreMonth> storeList = new ArrayList<StoreMonth>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)));
				StoreMonth store = new StoreMonth();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setInAmount(amount);
				store.setInMoney(detail.getPurchaseTicket().getPmoney());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4OutStore();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, OrderDetail> detailMap = new HashMap<Long, OrderDetail>();
			for (OrderDetail detail: new OrderTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreMonth> storeList = new ArrayList<StoreMonth>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				OrderDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)));
				StoreMonth store = new StoreMonth();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setOutAmount(amount);
				store.setOutMoney(detail.getPrice() * detail.getAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		cross = getSelectCross4OutBom();
		if (cross.getRowCounts().size() > 0) {
			StringBuffer ids = new StringBuffer();
			for (List<Object> row: cross.getRowCounts()) {
				ids.append(row.get(1)).append(",");
			}
			HashMap<Long, BomDetail> detailMap = new HashMap<Long, BomDetail>();
			for (BomDetail detail: new BomTicketLogic().getDetailInIDs(ids.deleteCharAt(ids.length()-1).toString())) {
				detailMap.put(detail.getId(), detail);
			}
			List<StoreMonth> storeList = new ArrayList<StoreMonth>();
			for (Iterator<List<Object>> biter=cross.getBodyRows().iterator(), citer=cross.getRowCounts().iterator(); citer.hasNext();) {
				List<Object> rowLabels=biter.next(), rowCounts=citer.next();
				double amount = Double.parseDouble(""+rowCounts.get(0));
				BomDetail detail = detailMap.get( Long.parseLong(""+rowCounts.get(1)));
				StoreMonth store = new StoreMonth();
				store.setInstore((String)rowLabels.get(0));
				store.setCommodity(detail.getCommodity());
				store.setOutAmount(amount);
				store.setOutMoney(detail.getPrice() * detail.getAmount());
				storeList.add(store);
			}
			storeAll.addAll(storeList);
		}
		if (storeAll.size() > 0) {
			int si=0;
			for (StoreMonth store: storeAll) {
				store.getCommodity().setId(++si);
			}
		}
		getMonthCountList().clear();
		getMonthCountList().addAll(storeAll);
	}
	
	private void setMonth4CurrentDraw(Component fcomp) {
		EditView editView = fcomp.searchFormerByClass(EditView.class);
		List<ListView> viewList = editView.getComponent().getInnerFormerList(ListView.class);
		for (ListView view: viewList) {
			ListViewBuilder builder = view.getListBuilder();
			String sparam = builder.getParameter(ParameterName.Cfg).getString(ParameterName.Parameter);
			if (sparam != null) {
				builder.setParameters(builder.cloneParameters());
				builder.setAttribute("getParam4MonthCurrent", ParameterName.Cfg, ParameterName.Parameter);
			}
		}
		editView.update();
	}
	
	private void setMonthCurrentFirer(Component fcomp) {
		EditViewBuilder builder = (EditViewBuilder)EntityClass.loadViewBuilder(this.getClass(), "MonthCountList");
		// clear filters
		for (SqlListBuilder sqlBuilder: builder.getFieldBuildersDeep(SqlListBuilder.class)) {
			this.getSearchSetting(sqlBuilder);
		}
		for (Menu item: fcomp.searchParentByClass(Window.class).getInnerComponentList(Menu.class)) {
			if (item.getText().equals("手动计算当月出入库数")) {
				item.getEventListenerList().fireListener();
				break;
			}
		}
	}
	
	private void getStoreMonths() throws Exception {
		SelectTicketFormer4Cross<StoreTicketForm> former = getSelectCross4MonthCount();
		List<StoreMonth> storeList = new ArrayList<StoreMonth>();
		DoubleType type = new DoubleType();
		for (List<Object> row: former.getRowCounts()) {
			int ci = 0;
			Long sn = (Long)row.get(ci++);
			StoreMonth store = null;
			for (StoreMonth item: getMonthCountList()) {
				if (item.getCommodity().getId() == sn) {
					store = item;
					break;
				}
			}
			store.setMonthi((Date)this.getAttr("StartDate"));
			store.setStartAmount((Double)row.get(ci++));
			store.setInAmount((Double)row.get(ci++));
			store.setOutAmount((Double)row.get(ci++));
			store.setEndAmount((Double)row.get(ci++));
			store.setStartMoney((Double)row.get(ci++));
			store.setInMoney((Double)row.get(ci++));
			store.setOutMoney((Double)row.get(ci++));
			store.setEndMoney((Double)row.get(ci++));
			if (store.getStartAmount()+store.getInAmount()+store.getOutAmount()+store.getEndAmount() == 0)
				continue;
			store.setStartPrice(store.getStartAmount()>0? type.parse(type.format(store.getStartMoney()/store.getStartAmount())): 0);
			store.setInPrice(store.getInAmount()>0? type.parse(type.format(store.getInMoney()/store.getInAmount())): 0);
			store.setOutPrice(store.getOutAmount()>0? type.parse(type.format(store.getOutMoney()/store.getOutAmount())): 0);
			store.setEndPrice(store.getEndAmount()>0? type.parse(type.format(store.getEndMoney()/store.getEndAmount())): 0);
			storeList.add(store);
		}
		this.setAttr("StoreMonthList", storeList);
		List<StoreMonth> deleteList = new ArrayList<StoreMonth>(getSelectFormer4MonthStart().getSelectedList());
		deleteList.removeAll(storeList);
		this.setAttr("StoreMonth4DeleteList", deleteList);
	}

	public SelectTicketFormer4Sql<StoreTicketForm, BomDetail> getSelectFormer4Bom() {
		String k = "SelectFormer4BomDetail";
		SelectTicketFormer4Sql<StoreTicketForm, BomDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<StoreTicketForm, BomDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	private SelectTicketFormer4Sql<StoreTicketForm, StoreItem> getSelectFormer4StoreItem() {
		String k = "SelectFormer4StoreItem";
		SelectTicketFormer4Sql<StoreTicketForm, StoreItem> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<StoreTicketForm, StoreItem>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public SelectTicketFormer4Sql<StoreTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k = "SelectFormer4Purchase";
		SelectTicketFormer4Sql<StoreTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<StoreTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public SelectTicketFormer4Sql<StoreTicketForm, StoreEnough> getSelectFormer4Enough() {
		String k = "SelectFormer4StoreEnough";
		SelectTicketFormer4Sql<StoreTicketForm, StoreEnough> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<StoreTicketForm, StoreEnough>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	private SelectTicketFormer4Sql<StoreTicketForm, StoreMonth> getSelectFormer4MonthPrevious() {
		String k = "SelectFormer4MonthPrevious";
		SelectTicketFormer4Sql<StoreTicketForm, StoreMonth> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<StoreTicketForm, StoreMonth>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	private SelectTicketFormer4Sql<StoreTicketForm, StoreMonth> getSelectFormer4MonthStart() {
		String k = "SelectFormer4MonthStart";
		SelectTicketFormer4Sql<StoreTicketForm, StoreMonth> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<StoreTicketForm, StoreMonth>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4Agent() {
		String k = "SelectCross4Agent";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4Store() {
		String k = "SelectCross4Store";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4Request() {
		String k = "SelectCross4Request";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4Onroad() {
		String k = "SelectCross4Onroad";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4Order() {
		String k = "SelectCross4Order";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4Bom() {
		String k = "SelectCross4Bom";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4KeepBom() {
		String k = "SelectCross4KeepBom";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4LockBom() {
		String k = "SelectCross4LockBom";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4LockSend() {
		String k = "SelectCross4LockSend";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4InStore() {
		String k = "SelectCross4InStore";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4OutStore() {
		String k = "SelectCross4OutStore";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4OutBom() {
		String k = "SelectCross4OutBom";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4StoreEnd() {
		String k = "SelectCross4StoreEnd";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4MonthCount() {
		String k = "SelectCross4MonthCount";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Cross<StoreTicketForm> getSelectCross4EnoughCount() {
		String k = "SelectCross4EnoughCount";
		SelectTicketFormer4Cross<StoreTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<StoreTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public List<OrderDetail> getDetailList() {
		String k="PurchaseList";
		List<OrderDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<BomDetail> getAgentList() {
		String k="AgentList";
		List<BomDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<StoreItem> getStoreCountList() {
		String k = "StoreCountList";
		List<StoreItem> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<StoreItem>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<StoreMonth> getMonthCountList() {
		String k="MonthCountList";
		List<StoreMonth> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<StoreMonth>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<StoreEnough> getEnoughCountList() {
		String k="EnoughCountList";
		List<StoreEnough> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<StoreEnough>();
			this.setAttr(k, list);
		}
		return list;
	}
}

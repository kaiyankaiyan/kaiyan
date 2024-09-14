package com.haoyong.sales.common.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.attributes.AttributeName;
import net.sf.mily.attributes.ClientEventName;
import net.sf.mily.attributes.StyleName;
import net.sf.mily.common.SessionProvider;
import net.sf.mily.http.Connection;
import net.sf.mily.http.HtmlElement;
import net.sf.mily.http.RenderingContext;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.renderer.RendererFactory;
import net.sf.mily.types.DateTimeType;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.HtmlText;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.Panel;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.ui.enumeration.ElementName;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ViewBuilder;

import com.haoyong.sales.base.domain.PrintViewSerial;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.PrintViewSerialLogic;
import com.haoyong.sales.sale.domain.PrintModel;
import com.haoyong.sales.sale.logic.PrintModelLogic;
import com.haoyong.sales.test.base.AbstractTest.TestMode;
import com.haoyong.sales.test.base.AbstractTest.TestModeList;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.test.base.GlobalSearchTest;
import com.haoyong.sales.test.base.QuestionTest;
import com.haoyong.sales.test.base.StorehouseTest;
import com.haoyong.sales.test.base.SubCompanyTest;
import com.haoyong.sales.test.base.SupplierTest;
import com.haoyong.sales.test.base.UserTest;
import com.haoyong.sales.test.sale.ArrangeTicketTest;
import com.haoyong.sales.test.sale.BillTicketTest;
import com.haoyong.sales.test.sale.InStoreTicketTest;
import com.haoyong.sales.test.sale.LocationTicketTest;
import com.haoyong.sales.test.sale.OrderReturnTest;
import com.haoyong.sales.test.sale.OrderTicketTest;
import com.haoyong.sales.test.sale.OutStoreTicketTest;
import com.haoyong.sales.test.sale.PReceiptTicketTest;
import com.haoyong.sales.test.sale.PurchaseReturnTest;
import com.haoyong.sales.test.sale.PurchaseTicketTest;
import com.haoyong.sales.test.sale.ReceiptTicketTest;
import com.haoyong.sales.test.sale.SendTicketTest;
import com.haoyong.sales.test.sale.StoreTicketTest;
import com.haoyong.sales.test.sale.WBillTicketTest;

public class TestCaseForm extends AbstractForm<PrintModel> implements FViewInitable {
	
	private void resetTest() {
		new SessionProvider().clear();
		if ("StartSeller".length()>0) {
			String key=null;
			if (this.getAttr(key="StartSeller")==null)
				this.setAttr(key, this.getSeller());
			if (this.getAttr(key="StartUser")==null)
				this.setAttr(key, this.getUser());
		}
		ClientTest t = new ClientTest();
		t.setTransSeller((Seller)this.getAttr("StartSeller"));
		t.setTransUser((User)this.getAttr("StartUser"));
	}
	
	private void setL(Hyperlink link, String l, TestCaseForm form) {
		Field field = (Field)link.getFormer();
		String type = field.getFieldBuilder().getParameter(ParameterName.Cfg).getString(ParameterName.Type);
		ClientTest t = new ClientTest();
		this.resetTest();
		TestModeList modeList = t.getModeList();
		if (1==1 && type!=null) {
			TestMode[] modes = modeList.getModesByName(type);
			modeList.setMode(modes);
		} else {
			modeList.setMode(TestMode.Purchase);
		}
		String label = new StringBuffer().append(modeList.getModeLabel()).append(field.getFieldBuilder().getLabel()).toString();
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		grid.addStyle(StyleName.WIDTH, "100%").addStyle(StyleName.BACKGROUND, "#5ED9FA");
		grid.append(new Text(label));
		this.setOnceCell(grid.append(null));
		if (type != null) {
			if (modeList.contain(TestMode.SubcompanyOrder) && modeList.contain(TestMode.SubCompany)==false)
				modeList.addMode(TestMode.SubCompany);
		}
		link.add(grid);
	}
	
	private void setT(Hyperlink link, String l, TestCaseForm form) {
		Field field = (Field)link.getFormer();
		String label = field.getFieldBuilder().getLabel();
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		grid.addStyle(StyleName.WIDTH, "100%").addStyle(StyleName.BACKGROUND, "#5ED9FA");
		grid.append(new Text(new StringBuffer(new DateTimeType().format(new Date())).append(label).toString()));
		Long total = this.getTotalTime();
		this.addTotalTime(0 - total);
		grid.append(new Text(PerformTrace.formatNano(total)));
		link.add(grid);
		new ClientTest().getModeList().setOrderTime(null);
		this.resetTest();
	}
	
	private void setP1(Hyperlink link, String p1, TestCaseForm form) {
		Field field = (Field)link.getFormer();
		String sproperty=field.getFieldBuilder().getAttribute(ParameterName.Cfg, ParameterName.Property), plist[]=sproperty.split("\\.");
		String sexclude=field.getFieldBuilder().getAttribute(ParameterName.Cfg, ParameterName.Exclude);
		if (sexclude!=null) {
			for (String item: sexclude.split("\\.")) {
				if (new ClientTest().getModeList().contain(TestMode.valueOf(item))==true)
					return;
			}
		}
		Object test = ReflectHelper.getPropertyValue(this, plist[0]);
		TraceItem traceItem = getTrace().onceStart("setP1");
		StringBuffer serror = new StringBuffer();
		try {
			this.resetTest();
			ReflectHelper.invokeMethod(test, plist[1], new Object[0]);
//			throw new RuntimeException("一个测试方法后强制出错");
		} catch(Exception e) {
			if (1==10)
				throw LogUtil.getRuntimeException(e);
			else {
				String perror = LogUtil.printStackTrace(e);
				int si=0;
				for (Iterator<String> iter=Arrays.asList(perror.split("\\n")).iterator(); iter.hasNext() && si<6; ) {
					String sitem = iter.next();
					for (int f=0, t=sitem.indexOf(".",f), tend=sitem.lastIndexOf("."); f<tend; f=t+1,t=sitem.indexOf(".",f)) {
						if (t==tend)
							sitem = sitem.substring(f);
					}
					serror.append(sitem).append("\n");
					if (sitem.contains(plist[1]))
						break;
					si++;
				}
			}
		}
		traceItem.onceEnd();
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		grid.addStyle(StyleName.WIDTH, "100%");
		grid.append(new Text(sproperty));
		grid.append(new Text(new StringBuffer().append(PerformTrace.formatNano(traceItem.getOnceTime())).toString()));
		this.addOneTime(traceItem.getOnceTime());
		if (serror.length()>0)
			grid.append(new Text(serror.toString()).addAttribute(AttributeName.Color, "red")).addStyle(StyleName.WhiteSpace, "normal");
		link.add(grid);
	}
	
	private void setP2(Hyperlink link, String p1, TestCaseForm form) {
		Field field = (Field)link.getFormer();
		String sproperty=field.getFieldBuilder().getAttribute(ParameterName.Cfg, ParameterName.Property), plist[]=sproperty.split("\\.");
		String sexclude=field.getFieldBuilder().getAttribute(ParameterName.Cfg, ParameterName.Exclude);
		if (sexclude!=null) {
			for (String item: sexclude.split("\\.")) {
				if (new ClientTest().getModeList().contain(TestMode.valueOf(item))==true)
					return;
			}
		}
		"".toCharArray();
		Object test = ReflectHelper.invokeConstructor(field.getFieldBuilder().getClassFinder().find(plist[0]), new Object[0]);
		TraceItem traceItem = getTrace().onceStart("setP2");
		StringBuffer serror = new StringBuffer();
		try {
			this.resetTest();
			ReflectHelper.invokeMethod(test, plist[1], new Object[0]);
//			throw new RuntimeException("一个测试方法后强制出错");
		} catch(Exception e) {
			if (1==10)
				throw LogUtil.getRuntimeException(e);
			else {
				String perror = LogUtil.printStackTrace(e);
				int si=0;
				for (Iterator<String> iter=Arrays.asList(perror.split("\\n")).iterator(); iter.hasNext() && si<6; ) {
					String sitem = iter.next();
					for (int f=0, t=sitem.indexOf(".",f), tend=sitem.lastIndexOf("."); f<tend; f=t+1,t=sitem.indexOf(".",f)) {
						if (t==tend)
							sitem = sitem.substring(f);
					}
					serror.append(sitem).append("\n");
					if (sitem.contains(plist[1]))
						break;
					si++;
				}
			}
		}
		traceItem.onceEnd();
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		grid.addStyle(StyleName.WIDTH, "100%");
		grid.append(new Text(sproperty));
		grid.append(new Text(new StringBuffer().append(PerformTrace.formatNano(traceItem.getOnceTime())).toString()));
		this.addOneTime(traceItem.getOnceTime());
		if (serror.length()>0)
			grid.append(new Text(serror.toString()).addAttribute(AttributeName.Color, "red")).addStyle(StyleName.WhiteSpace, "normal");
		link.add(grid);
	}
	
	private void setS1(Hyperlink link, String s1, TestCaseForm form) {
		Field field = (Field)link.getFormer();
		String sview = field.getFieldBuilder().getAttribute(ParameterName.Cfg, ParameterName.View_Name);
		ViewBuilder builder = EntityClass.loadViewBuilder(this.getClass(), sview);
		PrintModel model = new PrintModelLogic().getPrintModel(builder);
		if (model.getId()==0)
			return;
		Panel text = new Panel();
		text.getContent().append(model.getContent().toString());
		text.addAttribute(ClientEventName.InitScript0, "ajax.attributeNode(this);compList.doInit();");
		link.add(text);
	}
	
	private void getTestResult(Component fcomp) {
		this.resetTest();
		EditView view = fcomp.searchFormerByClass(EditView.class);
		Component tcomp = view.getComponent();
		Window win = new Window();
		win.renderInitialize(view.getComponent().getComponentIterator());
		HtmlElement telem = (HtmlElement)new RendererFactory().getRenderer(tcomp).render(tcomp, new RenderingContext(null), new HtmlElement(ElementName.CONTENT), new HtmlElement(ElementName.CONTENT));
		StringWriter writer = new StringWriter();
		telem.render(new PrintWriter(writer));
		writer.flush();
		PrintModel q = new PrintModelLogic().getPrintModel(view.getViewBuilder());
		q.getContent().delete(0, q.getContent().length());
		q.getContent().append(writer.toString().replaceAll("\\s{2,}", ""));
		this.setAttr(q);
	}
	
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		Connection conn = (Connection)WindowMonitor.getMonitor().getAttribute("conn");
		if (1==1 && viewBuilder.getName().startsWith("g")) {
			List<FieldBuilder> builderList = viewBuilder.getFieldBuilders();
			for (int ifrom=0,ito=0,bi=1; bi<builderList.size(); ifrom=bi,ito=bi,bi++) {
				FieldBuilder bstart=builderList.get(ifrom);
				bstart.setParameters(bstart.cloneParameters());
				String type=bstart.getParameter(ParameterName.Cfg).getString(ParameterName.Type);
				if (type == null)
					continue ;
				String[] typeList = type.split("\\,");
				bstart.setAttribute(typeList[0], ParameterName.Cfg, ParameterName.Type);
				int runMore = 10;
				if (conn!=null && conn.getParameterMap().get("times")!=null)
					runMore = Integer.parseInt(conn.getParameterMap().get("times").toString());
				List<FieldBuilder> newList = new ArrayList<FieldBuilder>();
				for (; ; bi++) {
					if (builderList.get(bi).getName().equals("Total") || builderList.get(bi).getName().equals("L")) {
						ito = bi;
						break;
					}
				}
				FieldBuilder blast=builderList.get(ito);
				for (int ti=1, tsize=typeList.length; ti<tsize && runMore-->1; ti++) {
					for (int fi=0; fi<ito-ifrom; fi++) {
						FieldBuilder builder = builderList.get(ifrom+fi).createClone();
						builder.setViewBuilder(viewBuilder);
						newList.add(builder);
						if (fi==0) {
							builder.setParameters(builder.cloneParameters());
							builder.setAttribute(typeList[ti], ParameterName.Cfg, ParameterName.Type);
						}
					}
				}
				if (true) {
					builderList.addAll(builderList.indexOf(blast), newList);
					bi = builderList.indexOf(blast)-1;
				}
			}
		}
	}
	
	private void setPrintModel4Service(ViewData<PrintModel> viewData) {
		viewData.setTicketDetails(this.getAttr(PrintModel.class));
	}

	public void setSelectedList(List<PrintModel> selected) {
	}
	
	private void setPrintView(Hyperlink link, StringBuffer s1, PrintViewSerial print) {
		Panel text = new Panel();
		text.getContent().append(print.getContent().toString());
		link.add(text);
	}
	private void onPrintOne(Component fcomp) {
		String winId=fcomp.searchParentByClass(Window.class).getIdentifier();
		StringBuffer openStr=new StringBuffer();
		String url="actionform.jsp?action=set.TestCaseForm&prepare=getPrintOne";
		openStr.append("window.open('").append(url).append("','a_blank');");
		fcomp.addAttribute(ClientEventName.InitScript0, openStr.toString());
	}
	private Component getPrintOne() {
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		ViewBuilder builder = EntityClass.loadViewBuilder(TestCaseForm.class, "PrintViewSerial");
		int cnt = 0;
		for (Iterator<PrintViewSerial> iter=new PrintViewSerialLogic().getViewList().iterator(); iter.hasNext();) {
			PrintViewSerial print = iter.next();
			grid.append(new Text(new StringBuffer().append(++cnt).append("，").append(print.getPath()).toString()));
			grid.append(new HtmlText(print.getContent().toString())).setStyleClass("Ticket");
//			LogUtil.error(print.getContent().toString());
		}
		return grid;
	}

	private String getS1() {
		return null;
	}
	
	private String getP1() {
		return null;
	}
	
	private String getL() {
		return null;
	}
	
	private String getTotal() {
		return null;
	}
	
	private Long getTotalTime() {
		String k = "TotalTime";
		Long t = (Long)this.getAttr(k);
		if (t == null) {
			t = new Long(0);
			this.setAttr(k, t);
		}
		return t;
	}
	
	private Long getOnceTime() {
		String k = "OnceTime";
		Long t = (Long)this.getAttr(k);
		if (t == null) {
			t = new Long(0);
			this.setAttr(k, t);
		}
		return t;
	}
	
	private void addOneTime(long once) {
		String k = "OnceTime";
		this.setAttr(k, new Long(this.getOnceTime().longValue() + once));
	}
	
	private void addTotalTime(long sum) {
		String k = "TotalTime";
		this.setAttr(k, new Long(this.getTotalTime().longValue() + sum));
	}
	
	private BlockCell getOnceCell() {
		String k = "OnceCell";
		BlockCell c = (BlockCell)this.getAttr(k);
		return c;
	}
	
	private void setOnceCell(BlockCell c) {
		if (getOnceCell()!=null) {
			getOnceCell().setComponent(new Text(PerformTrace.formatNano(getOnceTime())));
			this.addTotalTime(this.getOnceTime());
		}
		this.setAttr("OnceCell", c);
		this.setAttr("OnceTime", new Long(0));
	}
	
	private TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(this);
	}
	
	private PrintViewSerial getPrintViewSerial() {
		PrintViewSerial p = this.getAttr(PrintViewSerial.class);
		if (p == null) {
			p = new PrintViewSerial();
			this.setAttr(p);
		}
		return p;
	}
	
	private UserTest getUserTest() {
		String k = "UserTest";
		UserTest t = (UserTest)this.getAttr(k);
		if (t == null) {
			t = new UserTest();
			this.setAttr(k, t);
		}
		return t;
	}
	
	private ClientTest getClientTest() {
		String k = "ClientTest";
		ClientTest t = (ClientTest)this.getAttr(k);
		if (t == null) {
			t = new ClientTest();
			this.setAttr(k, t);
		}
		return t;
	}
	
	private SubCompanyTest getSubCompanyTest() {
		String k = "SubCompanyTest";
		SubCompanyTest t = (SubCompanyTest)this.getAttr(k);
		if (t == null) {
			t = new SubCompanyTest();
			this.setAttr(k, t);
		}
		return t;
	}
	
	private CommodityTest getCommodityTest() {
		String k = "CommodityTest";
		CommodityTest t = (CommodityTest)this.getAttr(k);
		if (t == null) {
			t = new CommodityTest();
			this.setAttr(k, t);
		}
		return t;
	}
	private StorehouseTest getStorehouseTest() {
		String k = "StorehouseTest";
		StorehouseTest t = (StorehouseTest)this.getAttr(k);
		if (t == null) {
			t = new StorehouseTest();
			this.setAttr(k, t);
		}
		return t;
	}
	
	private SupplierTest getSupplierTest() {
		String k = "SupplierTest";
		SupplierTest t = (SupplierTest)this.getAttr(k);
		if (t == null) {
			t = new SupplierTest();
			this.setAttr(k, t);
		}
		return t;
	}
	
	private QuestionTest getQuestionTest() {
		String k = "QuestionTest";
		QuestionTest t = (QuestionTest)this.getAttr(k);
		if (t == null) {
			t = new QuestionTest();
			this.setAttr(k, t);
		}
		return t;
	}
	
	private GlobalSearchTest getGlobalSearchTest() {
		return new GlobalSearchTest();
	}
	
	private OrderTicketTest getOrderTicketTest() {
		return new ClientTest().getModeList().getSelfOrderTest();
	}

	private ArrangeTicketTest getArrangeTicketTest() {
		return new ArrangeTicketTest();
	}
	
	private PurchaseTicketTest getPurchaseTicketTest() {
		return new ClientTest().getModeList().getSelfPurchaseTest();
	}
	
	private StoreTicketTest getStoreTicketTest() {
		return new StoreTicketTest();
	}

	private ReceiptTicketTest getReceiptTicketTest() {
		return new ClientTest().getModeList().getSelfReceiptTest();
	}

	private PReceiptTicketTest getPReceiptTicketTest() {
		return new PReceiptTicketTest();
	}

	private SendTicketTest getSendTicketTest() {
		return new SendTicketTest();
	}

	private InStoreTicketTest getInStoreTicketTest() {
		return new InStoreTicketTest();
	}
	
	private OutStoreTicketTest getOutStoreTicketTest() {
		return new OutStoreTicketTest();
	}
	
	private BillTicketTest getBillTicketTest() {
		return new BillTicketTest();
	}
	
	private WBillTicketTest getWBillTicketTest() {
		return new WBillTicketTest();
	}
	
	private LocationTicketTest getLocationTicketTest() {
		return new LocationTicketTest();
	}
	
	private OrderReturnTest getOrderReturnTest() {
		return new OrderReturnTest();
	}
	
	private PurchaseReturnTest getPurchaseReturnTest() {
		return new PurchaseReturnTest();
	}
}

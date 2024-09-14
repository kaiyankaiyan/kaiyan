package com.haoyong.sales.test.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.mily.bean.ListDynaBean;
import net.sf.mily.common.SessionProvider;
import net.sf.mily.http.HtmlElement;
import net.sf.mily.http.RenderingContext;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.renderer.RendererFactory;
import net.sf.mily.server.EditViewer;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.CheckBox;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Container;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.Menu;
import net.sf.mily.ui.RadioButton;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ElementName;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.ui.facable.OnchangeFormer;
import net.sf.mily.ui.facable.ValueGetable;
import net.sf.mily.ui.facable.ValueSetable;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.AddNoteListener;
import net.sf.mily.webObject.ButtonMeta;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.EditViewBuilder;
import net.sf.mily.webObject.EntityField;
import net.sf.mily.webObject.EntityField.FooterField;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.IEditListBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.RadioButtonGroupBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.View;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.event.FieldActionListener;
import net.sf.mily.webObject.query.ColumnField;
import net.sf.mily.webObject.query.Fields;
import net.sf.mily.webObject.query.Fields.CondReg;
import net.sf.mily.webObject.query.Identity;
import net.sf.mily.webObject.query.SqlListBuilderSetting;

import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;
import org.junit.Assert;

import com.haoyong.sales.base.domain.PrintViewSerial;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.PrintViewSerialLogic;
import com.haoyong.sales.base.logic.UserMenuLogic;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.listener.ActionServiceListener;
import com.haoyong.sales.sale.form.ArrangeTicketForm;
import com.haoyong.sales.sale.form.DOrderTicketForm;
import com.haoyong.sales.sale.form.OrderTicketForm;
import com.haoyong.sales.sale.form.PArrangeTicketForm;
import com.haoyong.sales.sale.form.PPurchaseTicketForm;
import com.haoyong.sales.sale.form.PurchaseTicketForm;
import com.haoyong.sales.test.sale.LOrderTicketTest;
import com.haoyong.sales.test.sale.OrderTicketTest;
import com.haoyong.sales.test.sale.POrderTicketTest;
import com.haoyong.sales.test.sale.PPurchaseTicketTest;
import com.haoyong.sales.test.sale.PReceiptTicketTest;
import com.haoyong.sales.test.sale.PurchaseTicketTest;
import com.haoyong.sales.test.sale.ReceiptTicketTest;

public abstract class AbstractTest<F extends AbstractForm> {
	
	private Window window;
	private ListView curListView;
	private F form;
	
	protected void loadView(String viewName, Object... filterList0) {
		this.loadFormView(this.getForm(), viewName, filterList0);
	}
	public void loadSql(String viewName, String sqlName, Object... filterList0) {
		this.loadSqlView(this.getForm(), viewName, sqlName, filterList0);
	}
	
	public EditView loadFormView(AbstractForm form, String viewName, Object... filterList0) {
		try {
			EditViewBuilder builder = (EditViewBuilder)EntityClass.loadViewBuilder(form.getClass(), viewName);
			for (EditViewBuilder once=builder; once!=null; once=null) {
				once.setParameters(once.cloneParameters());
				if ("beforeWaiting".equals(once.getAttribute(ParameterName.Cfg, ParameterName.Before)))
					once.setAttribute(null, ParameterName.Cfg, ParameterName.Before);
				once.setAttribute("false", ParameterName.Cfg, ParameterName.Freezable);
			}
			SqlListBuilder sqlBuilder = null;
			String sqlName = null;
			if (filterList0.length % 2==1) {
				sqlName = (String)filterList0[0];
				filterList0 = Arrays.copyOfRange(filterList0, 1, filterList0.length);
			}
			if (filterList0.length>0)
				sqlBuilder = this.setSqlFilters(form, builder, sqlName, filterList0);
			EditView view = new EditViewer(builder, form, form.getWidth()).setWindow(this.getWindow()).createView();
			if (sqlBuilder!=null) {
				this.getSqlListView(sqlBuilder.getFullName());
				form.getSearchSetting(sqlBuilder);
			} else
				this.curListView = null;
			return view;
		}catch(Exception e) {
			String se = new StringBuffer("加载界面失败").append(form.getClass().getSimpleName()).append(".").append(viewName).toString();
			LogUtil.error(se, e);
			Assert.fail(se);
		}
		return null;
	}
	
	public void loadSqlView(AbstractForm form, String viewName, String sqlName, Object... filterList0) {
		try {
			EditViewBuilder builder = (EditViewBuilder)EntityClass.loadViewBuilder(form.getClass(), viewName);
			for (EditViewBuilder once=builder; once!=null; once=null) {
				once.setParameters(once.cloneParameters());
				if ("beforeWaiting".equals(once.getAttribute(ParameterName.Cfg, ParameterName.Before)))
					once.setAttribute(null, ParameterName.Cfg, ParameterName.Before);
				once.setAttribute("false", ParameterName.Cfg, ParameterName.Freezable);
			}
			SqlListBuilder sqlBuilder = this.setSqlFilters(form, builder, sqlName, filterList0);
			ViewBuilder vsql = sqlBuilder.getViewBuilder();
			if (vsql.getMenuMetas().length>0 && vsql!=builder) {
				builder.setFieldBuilders(new ArrayList<FieldBuilder>());
				builder.getFieldBuilders().add(vsql);
			} else {
				builder.setFieldBuilders(new ArrayList<FieldBuilder>());
				builder.getFieldBuilders().add(sqlBuilder);
			}
			this.getWindow().getSubComponents().setComponent(builder.build(form).getComponent());
			this.getSqlListView(sqlBuilder.getFullName());
			form.getSearchSetting(sqlBuilder);
		}catch(Exception e) {
			LogUtil.error(new StringBuffer("加载界面失败").append(form.getClass().getSimpleName()).append(viewName).toString(), e);
			Assert.fail("加载界面失败".concat(viewName));
		}
	}
	
	protected void onRowButton(String menuname, int row) {
		EntityField entityField = this.getEditListView().getListBuilder().getEntityField((BlockGrid)this.getEditListView().getComponent(), row-1);
		for (Component bcomp: entityField.getInnerFormerComponentList(ButtonMeta.class)) {
			ButtonMeta button = (ButtonMeta)bcomp.getFormer();
			if (StringUtils.equals(button.getText(), menuname)) {
				bcomp.getEventListenerList().fireListener();
				return;
			}
		}
		Assert.fail("找不到按钮".concat(menuname));
	}
	
	protected void onButton(String menuname) {
		for (Component bcomp: window.getInnerFormerComponentList(ButtonMeta.class)) {
			ButtonMeta button = (ButtonMeta)bcomp.getFormer();
			if (StringUtils.equals(button.getText(), menuname)) {
				bcomp.getEventListenerList().fireListener();
				window.getSubComponents().clear();
				return;
			}
		}
		Assert.fail("找不到按钮".concat(menuname));
	}
	
	public void onMenu(String menuname) {
		this.curListView = null;
		Component theMenu = null;
		if ("点击按钮".length()>0) {
			for (Component bcomp: window.getInnerFormerComponentList(ButtonMeta.class)) {
				ButtonMeta button = (ButtonMeta)bcomp.getFormer();
				if (StringUtils.equals(button.getText(), menuname)) {
					theMenu = bcomp;
					break;
				}
			}
			if (theMenu==null)
				Assert.fail("找不到按钮".concat(menuname));
		}
		View theView = theMenu.searchFormerByClass(View.class);
		FieldBuilder theFirst = (FieldBuilder)theView.getViewBuilder().getFieldBuilders().get(0);
		if ("打印界面+按钮".length()>0 && this.getModeList().contain(TestMode.PrintView) && (theView instanceof EditView) && (theFirst instanceof SqlListBuilder)==false) {
			StringBuffer spath = new StringBuffer();
			List<EditView> viewList = theMenu.searchFormerLinkByClass(EditView.class);
			EditView view = viewList.get(viewList.size()-1);
			ViewBuilder vbuilder = view.getViewBuilder();
			User user = new UserMenuLogic().getMenu(vbuilder.getFullViewName());
			if (user!=null)
				spath.append(user.getUserName()).append("，");
			spath.append(menuname);
			if ("界面".length()>0 && vbuilder.getName().indexOf("Query")==-1)
				this.printView(view.getComponent(), spath.toString());
		}
		theMenu.getEventListenerList().fireListener();
		window.getSubComponents().clear();
	}
	
	private void printView(Component tcomp, String spath) {
		Window win = new Window();
		win.renderInitialize(tcomp.getComponentIterator());
		HtmlElement telem = (HtmlElement)new RendererFactory().getRenderer(tcomp).render(tcomp, new RenderingContext(null), new HtmlElement(ElementName.CONTENT), new HtmlElement(ElementName.CONTENT));
		StringWriter writer = new StringWriter();
		telem.render(new PrintWriter(writer));
		writer.flush();
		if ("只打印".length()<0) {
			LogUtil.error(new StringBuffer().append("\n").append(spath).append("\n").append(writer.toString()).toString());
		} else {
			Iterator<PrintViewSerial> iter = this.getModeList().getPrintViewIterator();
			PrintViewSerial print = iter.hasNext()? iter.next(): new PrintViewSerial();
			print.setPath(spath);
			print.getContent().delete(0, print.getContent().length());
			print.getContent().append(writer.toString().replaceAll("\\s{2,}", ""));
			new PrintViewSerialLogic().save(print);
		}
	}
	
	public void onMenuUntrans(String menuname) {
		this.curListView = null;
		Menu tmenu = null;
		for (Menu menu: window.getInnerComponentList(Menu.class)) {
			if (StringUtils.equals(menu.getText(), menuname)) {
				window.getSubComponents().clear();
				tmenu = menu;
				break;
			}
		}
		Assert.assertTrue("找不到按钮".concat(menuname), tmenu!=null);
		for (FieldActionListener listener: tmenu.searchFormerByClass(ButtonMeta.class).getListeners()) {
			if (listener.getClass()==ActionServiceListener.class) {
				listener.getParameter().setParameters(listener.getParameter().cloneParameters());
				"1".toCharArray();
				listener.getParameter().putString(ParameterName.Type, "UnTransaction");
			}
		}
		tmenu.getEventListenerList().fireListener();
	}
	
	protected HashMap<String, String> genHashMap(String... keyValues) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i=0; i<keyValues.length; i+=2) {
			String k=keyValues[i], v=keyValues[i+1];
			map.put(k, v);
		}
		return map;
	}
	
	protected Object[] genFiltersStart(Object[] filters0, Object...starts) {
		Object[] list = new Object[starts.length + filters0.length];
		int i=0;
		for (Object o: starts) {
			list[i++] = o;
		}
		for (Object o: filters0) {
			list[i++] = o;
		}
		return list;
	}
	
	public SqlListBuilder setFilters(Object... filterList) {
		ListView listview = this.getSqlListView();
		"12".toCharArray();
		String sqlListName = null;
		if (filterList.length%2 == 1) {
			sqlListName = (String)filterList[0];
			listview = this.getSqlListView(sqlListName);
			filterList = Arrays.copyOfRange(filterList, 1, filterList.length);
		}
		EditView editView = listview.getComponent().searchFormerByClass(EditView.class);
		AbstractForm cform = (AbstractForm)editView.getValue();
		SqlListBuilder sqlBuilder = this.setSqlFilters(cform, editView.getViewBuilder(), sqlListName, filterList);
		this.getSqlListView(sqlBuilder.getFullName()).update();
		this.form.getSearchSetting(sqlBuilder);
		return sqlBuilder;
	}
	private SqlListBuilder setSqlFilters(AbstractForm cform, ViewBuilder builder0, String sqlListName, Object... filterList) {
		"21".toCharArray();
		SqlListBuilder sqlBuilder = null;
		if (builder0.getClass()==EditViewBuilder.class) {
			EditViewBuilder editBuilder=(EditViewBuilder)builder0;
			if ("beforeWaiting".equals(editBuilder.getAttribute(ParameterName.Cfg, ParameterName.Before))) {
				editBuilder.setParameters(editBuilder.cloneParameters());
				editBuilder.setAttribute(null, ParameterName.Cfg, ParameterName.Before);
			}
			for (SqlListBuilder sbuilder: editBuilder.getFieldBuildersDeep(SqlListBuilder.class)) {
				if (sqlListName==null) {
					sqlBuilder = sbuilder;
					break;
				} else if (sbuilder.isViewOfName(sqlListName)) {
					sqlBuilder = sbuilder;
					break;
				} else if (StringUtils.equals(sqlListName, sbuilder.getLabel())) {
					sqlBuilder = sbuilder;
					break;
				}
			}
		} else if (builder0 instanceof SqlListBuilder) {
			sqlBuilder = (SqlListBuilder)builder0;
		}
		HashMap<String, String> filters = new HashMap<String, String>();
		StringBuffer snull = new StringBuffer();
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
			if (input==null)
				snull.append(name);
		}
		if (snull.length()>0)
			Assert.fail(snull.insert(0, "过滤条件为空").toString());
		SqlListBuilderSetting childSetting = cform.getSearchSetting(sqlBuilder);
		childSetting.addFilters(filters);
		List<String> nameList = new ArrayList<String>();
		for (int i=0; i<filterList.length; nameList.add((String)filterList[i]), i+=2);
		SqlListBuilderSetting setting = cform.getSqlListBuilderSetting(sqlBuilder);
		for (ColumnField col: setting.getColumnMap(sqlBuilder, setting.getFilters()).keySet()) {
			nameList.remove(col.getName());
		}
		if (nameList.size()>0)
			"".toCharArray();
		Assert.assertTrue(new StringBuffer("查询").append(sqlBuilder.getFullViewName()).append("没有配置列").append(nameList).toString(), nameList.size()==0);
		return sqlBuilder;
	}
	
	protected void setSqlListSelect(String listName, int... rowList) {
		for (ListView view: window.getInnerFormerList(ListView.class)) {
			if (view.getFieldBuilder().getFullName().endsWith(listName)) {
				this.setSqlListSelect(view, rowList);
				return;
			}
		}
		Assert.fail("没有找到此列表".concat(listName));
	}
	
	public void setSqlAllSelect(int detailCount) {
		Assert.assertTrue("要选择的明细数量不可为0", detailCount>0);
		ListView listview = this.getSqlListView();
		int[] rowList = new int[detailCount];
		for (int i=0; i<detailCount; i++, rowList[i-1]=i);
		this.setSqlListSelect(listview, rowList);
	}
	
	protected void setSqlListSelect(int... rowList) {
		ListView listview = this.getSqlListView();
		this.setSqlListSelect(listview, rowList);
	}
	
	private void setSqlListSelect(ListView listview, int... rowList) {
		List<BlockCell> list = listview.getComponent().getInnerComponentList("ListViewCellSelect");
		Arrays.sort(rowList);
		if (ListDynaBean.asList(listview.getValue()).size() < rowList[rowList.length-1])
			Assert.fail("查询列表记录行数不够选择");
		for (int row: rowList) {
			BlockCell cell = list.get(row-1);
			cell.getInnerComponentList(CheckBox.class).get(0).setSelected(true);
		}
		listview.getListBuilder().pickValue(listview.getComponent());
	}
	
	protected void setEditAllSelect(int detailCount) {
		Assert.assertTrue("要选择的明细数量不可为0", detailCount>0);
		int[] rowList = new int[detailCount];
		for (int i=0; i<detailCount; i++, rowList[i-1]=i);
		this.setEditListSelect(rowList);
	}
	
	protected void setEditListSelect(int... rowList) {
		ListView listview = this.getEditListView();
		List<BlockCell> list = listview.getComponent().getInnerComponentList("ListViewCellSelect");
		Arrays.sort(rowList);
		if (ListDynaBean.asList(listview.getValue()).size() < rowList[rowList.length-1])
			Assert.fail("编辑列表记录行数不够选择");
		for (int row: rowList) {
			BlockCell cell = list.get(row-1);
			cell.getInnerComponentList(CheckBox.class).get(0).setSelected(true);
		}
		if (listview.getListValue().get(0) instanceof Identity) {
			List<Identity> domainList = (List<Identity>)listview.getListValue();
			long preId = 0;
			for (Identity domain: domainList) {
				long id = domain.getId();
				if (id==0)
					continue;
				Assert.assertTrue("编辑明细不应为倒序！", preId<id);
				preId = id;
			}
		}
		listview.getListBuilder().pickValue(listview.getComponent());
	}
	
	public F getForm() {
		return form;
	}
	
	public <T extends AbstractTest> T setForm(F form) {
		this.form = form;
		this.getWindow();
		return (T)this;
	}
	
	private Window getWindow() {
		if (this.window==null) {
			this.window = new Window();
		} else {
			for (Iterator iter=this.window.getComponentIterator(); iter.hasNext();) {
				Component comp = (Component)iter.next();
				this.window.remove(comp);
			}
			this.window.clearCompEvents();
		}
		return this.window;
	}
	
	public void setTransSeller(Seller toSeller) {
		Seller fromSeller = (Seller)WindowMonitor.getMonitor().getAttribute("seller");
		WindowMonitor.getMonitor().addAttribute("seller", toSeller);
		if (fromSeller.getId()==toSeller.getId())
			return;
		if ("链接商家切换".length()>0) {
			TestMode[] fromList = this.getModeList().removeMode(TestMode.ClientOrder, TestMode.SubcompanyOrder, TestMode.LinkAsClient, TestMode.LinkAsSubcompany);
			List<TestMode> toList = new ArrayList<TestMode>();
			for (TestMode fromMode: fromList) {
				TestMode toMode = null;
				if (fromMode==TestMode.LinkAsClient) {
					toMode = TestMode.ClientOrder;
				} else if (fromMode==TestMode.LinkAsSubcompany) {
					toMode = TestMode.SubcompanyOrder;
					this.getModeList().addMode(TestMode.SubCompany);
				} else if (fromMode==TestMode.ClientOrder) {
					toMode = TestMode.LinkAsClient;
				} else if (fromMode==TestMode.SubcompanyOrder) {
					toMode = TestMode.LinkAsSubcompany;
					this.getModeList().removeMode(TestMode.SubCompany);
				}
				if (toMode!=null)
					toList.add(toMode);
			}
			if (toList.size()>0)
				this.getModeList().addMode(toList.toArray(new TestMode[0]));
		}
//		LogUtil.error(new StringBuffer("\nTestTrans from").append(fromSeller.getId()).append(" to ").append(((Seller)WindowMonitor.getMonitor().getAttribute("seller")).getId()).append(" modeList ").append(this.getModeList().modes).toString());
	}
	
	public User setTransUser(User toUser) {
		User fromUser = (User)WindowMonitor.getMonitor().getAttribute("user");
		WindowMonitor.getMonitor().addAttribute("user", toUser);
		if (fromUser.getId()==toUser.getId())
			return fromUser;
		return fromUser;
	}
	
	protected abstract void setQ清空();
	
	public TestModeList getModeList() {
		TestModeList modeList = (TestModeList)WindowMonitor.getMonitor().getAttribute("TestModeList");
		if (modeList==null) {
			modeList = new TestModeList();
			WindowMonitor.getMonitor().addAttribute("TestModeList", modeList);
		}
		return modeList;
	}
	
	protected void setTestStart() {
		new CommodityLogic().getViewSetting();
		this.getModeList().setOrderTime(new Date());
//		this.getModeList().removeMode(TestMode.PrintView);
		if (StringUtils.equals(this.getForm().getUserName(), "管理员")==false)
			this.setTransUser(new UserTest().getUser管理员());
		new SessionProvider().clear();
	}
	
	protected String getTimeDo() {
		this.getModeList().setOrderTime(new Date());
		return new SimpleDateFormat(">=yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
	}
	
	protected ListView getSqlListView(String... aname0) {
		String name = aname0.length==1? aname0[0]: null;
		if (name==null)
			for (ListView view: window.getInnerFormerList(ListView.class)) {
				if ((view.getViewBuilder() instanceof SqlListBuilder)==false)
					continue;
				if (view==this.curListView)
					return view;
			}
		for (ListView view: window.getInnerFormerList(ListView.class)) {
			if ((view.getViewBuilder() instanceof SqlListBuilder)==false)
				continue;
			if (name==null)
				return view;
			else if (name!=null && view.getViewBuilder().isViewOfName(name)) {
				this.curListView = view;
				return view;
			}
		}
		Assert.fail("没有找到查询列表");
		return null;
	}
	public ListView getEditListView(String... aname0) {
		String name = aname0.length==1? aname0[0]: null;
		if (name==null)
			for (ListView view: window.getInnerFormerList(ListView.class)) {
				if ((view.getViewBuilder() instanceof IEditListBuilder)==false)
					continue;
				if (view==this.curListView)
					return view;
			}
		for (ListView view: window.getInnerFormerList(ListView.class)) {
			if ((view.getViewBuilder() instanceof IEditListBuilder)==false)
				continue;
			if (name==null)
				return view;
			else if (name!=null && view.getViewBuilder().isViewOfName(name)) {
				this.curListView = view;
				return view;
			}
		}
		Assert.fail("没有找到编辑列表");
		return null;
	}
	
	public List<Object> getListViewValue() {
		for (ListView view: window.getInnerFormerList(ListView.class)) {
			if (curListView==view)
				return (List<Object>)view.getValue();
			else if (curListView==null)
				return (List<Object>)view.getValue();
		}
		Assert.fail("没有找到列表");
		return null;
	}
	
	protected LinkedHashMap<String, Integer> getListCountSum() {
		LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
		for (ListView view: window.getInnerFormerList(ListView.class)) {
			if (view.getViewBuilder().getClass()!=SqlListBuilder.class)
				continue;
			SqlListBuilder builder = (SqlListBuilder)view.getViewBuilder();
			map.put(builder.getName(), ((List)view.getValue()).size());
		}
		return map;
	}
	
	public List<Object> getListViewColumn(String colName) {
		ListView listview = this.getSqlListView();
		try {
			SqlListBuilder sqlBuilder = (SqlListBuilder)listview.getFieldBuilder();
			List<Object> list = new ArrayList<Object>();
			for (Object row: this.getListViewValue()) {
				for (Iterator fiter=sqlBuilder.getSqlQuery().getFieldIterator(), viter=sqlBuilder.getSqlQuery().getFieldValueIterator(row); fiter.hasNext();) {
					ColumnField f = (ColumnField)fiter.next();
					Object v = viter.next();
					if (f.getName().equals(colName)) {
						list.add(v);
						break;
					}
				}
			}
			if (list.size() == this.getListViewValue().size())
				return list;
		}catch(Exception e) {
		}
		Assert.fail("不能获取查询列表列值".concat(colName));
		return null;
	}
	
	protected List<Object> getListFootColumn(String... colList0) {
		List<String> colList=new ArrayList<String>();
		for (String col: colList0) {
			colList.add(col);
		}
		List<String> unfoundCols = new ArrayList<String>(colList);
		ListView listview = this.getSqlListView();
		List<FooterField> footerList = listview.getComponent().getInnerFormerList(FooterField.class);
		Assert.assertTrue("列表没有统计行", footerList.size()>0);
		List<List<Object>> rowList = new ArrayList<List<Object>>();
		
		for (FooterField footer: footerList) {
			List<Object> row = new ArrayList<Object>();
			for (String col: colList) {
				row.add(null);
			}
			for (Field f: footer.getFieldList()) {
				int idx = colList.indexOf(f.getFieldBuilder().getName());
				if (idx>-1) {
					row.set(idx, f.getValue());
					unfoundCols.remove(f.getFieldBuilder().getName());
				}
			}
			rowList.add(row);
			Assert.assertTrue(new StringBuffer("没有找到统计列").append(unfoundCols).toString(), unfoundCols.size()==0);
		}
		return rowList.size()==1? rowList.get(0): (List)rowList;
	}
	
	public List<Field> getFields(String... fieldNames) {
		List<String> nameList = new ArrayList<String>();
		List<Field> fieldList = new ArrayList<Field>();
		for (int ic=fieldNames.length, i=0; i<ic; nameList.add(fieldNames[i]),fieldList.add(null),i++);
		for (Iterator<Field> iter=window.getInnerFormerComponentList(EditView.class).get(0).getInnerFormerList(Field.class).iterator(); iter.hasNext() && nameList.size()>0;) {
			Object f = iter.next();
			if (f.getClass()!=Field.class)			continue;
			Field field = (Field)f;
			FieldBuilder builder = field.getFieldBuilder();
			for (String fname: new ArrayList<String>(nameList)) { 
				if (builder.getName().equals(fname) || builder.getFullName().endsWith(".".concat(fname))) {
					fieldList.set(nameList.indexOf(fname), field);
					break;
				}
			}
			if (builder instanceof ViewBuilder) {
				iter = new JoinedIterator(iter, ((ViewBuilder)builder).getFieldBuilderIterator());
			}
		}
		return fieldList;
	}
	
	protected String getFieldText(String fieldName) {
		for (EditView view: window.getInnerFormerList(EditView.class)) {
			EditViewBuilder builder = (EditViewBuilder)view.getFieldBuilder();
			List<Field> fieldList = builder.getFields(view.getComponent().getInnerFormerList(Field.class), builder);
			for (Field field: fieldList) {
				if (field.getFieldBuilder().getName().equals(fieldName)) {
				} else if (field.getFieldBuilder().getFullName().endsWith(".".concat(fieldName))) {
				} else
					continue;
				ValueGetable text = (ValueGetable)field.getComponent();
				Object v = text.getValueObject();
				return v==null? null: v.toString();
			}
		}
		Assert.fail(new StringBuffer("找不到界面值").append(fieldName).toString());
		return null;
	}
	
	public void setFieldText(String fieldName, Object value0) {
		String value = String.valueOf(value0);
		for (EditView view: window.getInnerFormerList(EditView.class)) {
			EditViewBuilder builder = (EditViewBuilder)view.getFieldBuilder();
			List<Field> fieldList = builder.getFields(view.getComponent().getInnerFormerList(Field.class), builder);
			for (Field field: fieldList) {
				if (field.getFieldBuilder().getName().equals(fieldName)) {
				} else if (field.getFieldBuilder().getFullName().endsWith(".".concat(fieldName))) {
				} else
					continue;
				if (field.getComponent() instanceof ValueSetable) {
					ValueSetable text = (ValueSetable)field.getComponent();
					text.setToValue(value);
					field.onComponentChange(value);
					((Component)text).getEventListenerList().fireListener();
					return ;
				}
			}
		}
		Assert.fail(new StringBuffer("找不到界面输入框").append(fieldName).toString());
	}
	protected void setRowFieldText(AbstractDomain rowDomain, String fieldName, Object value0) {
		String value = String.valueOf(value0);
		EntityField entityField = null;
		for (ListView view: window.getInnerFormerList(ListView.class)) {
			for (Iterator<EntityField> rowIter=view.getListBuilder().getEntityFieldIterator((BlockGrid)view.getComponent()); rowIter.hasNext();) {
				EntityField rowField = rowIter.next();
				if (rowField.getValue()==rowDomain) {
					entityField = rowField;
					break;
				}
			}
		}
		"".toCharArray();
		Assert.assertTrue("找不到此编辑列表行Domain", entityField!=null);
		for (Field field: entityField.getFieldList()) {
			if (field.getFieldBuilder().getName().equals(fieldName)) {
			} else if (field.getFieldBuilder().getFullName().endsWith(".".concat(fieldName))) {
			} else
				continue;
			if (field.getFieldBuilder() instanceof RadioButtonGroupBuilder) {
				Component groupComp = field.getComponent();
				List<String> nameList = new ArrayList<String>();
				String checkedName = String.valueOf(value0);
				nameList.add(checkedName);
				for (Iterator liter=groupComp.getInnerComponentList(Hyperlink.class).iterator(), citer=groupComp.getInnerComponentList(RadioButton.class).iterator(); liter.hasNext();) {
					Hyperlink link = (Hyperlink)liter.next();
					RadioButton check = (RadioButton)citer.next();
					boolean contain = false;
					for (String nm: nameList) {
						if (link.getText().indexOf(nm)>-1) {
							contain = true;
						}
					}
					if (contain==true) {
						check.setSelected(true);
						nameList.remove(checkedName);
					} else {
						check.setSelected(false);
					}
				}
				if (nameList.size()==0) {
					groupComp.searchFormerByClass(OnchangeFormer.class).onComponentChange(null);
					groupComp.getEventListenerList().fireListener();
					return;
				}
			} else if (field.getComponent() instanceof ValueSetable) {
				ValueSetable text = (ValueSetable)field.getComponent();
				text.setToValue(value);
				field.onComponentChange(value);
				((Component)text).getEventListenerList().fireListener();
				return ;
			}
		}
		Assert.fail(new StringBuffer("找不到界面输入框").append(fieldName).toString());
	}
	protected void setEntityFieldText(AbstractDomain entityDomain, String fieldName, Object value0) {
		String value = String.valueOf(value0);
		for (Field field: window.getInnerFormerList(Field.class)) {
			if (field.getEntityBean()!=null && field.getEntityBean().getBean()!=entityDomain)
				continue;
			if (field.getFieldBuilder().getName().equals(fieldName)) {
			} else if (field.getFieldBuilder().getFullName().endsWith(".".concat(fieldName))) {
			} else
				continue;
			if (field.getComponent() instanceof ValueSetable) {
				ValueSetable text = (ValueSetable)field.getComponent();
				text.setToValue(value);
				field.onComponentChange(value);
				((Component)text).getEventListenerList().fireListener();
				return ;
			}
		}
	}
		
	protected void setNoteText(String fieldName, Object value0) {
		String value = String.valueOf(value0);
		for (EditView view: window.getInnerFormerList(EditView.class)) {
			for (AddNoteListener note: view.getComponent().getInnerFormerList(AddNoteListener.class)) {
				FieldBuilder builder = note.getSourceBuilder();
				if ((builder.getName().equals(fieldName) || builder.getFullName().endsWith(".".concat(fieldName)))==false) {
				} else if ((note.getComponent() instanceof ValueSetable)==true) {
					ValueSetable text = (ValueSetable)note.getComponent();
					text.setToValue(value);
					note.onComponentChange(value);
					((Component)text).getEventListenerList().fireListener();
					return ;
				}
			}
		}
		Assert.fail(new StringBuffer("找不到输入框").append(fieldName).toString());
	}
	
	protected void setCheckGroup(String... checkedNames) {
		Component groupComp = null;
		for (Component c: window.getInnerComponentList(Container.class)) {
			int hsize=c.getInnerComponentList(Hyperlink.class).size(), csize=c.getInnerComponentList(CheckBox.class).size();
			if (hsize==csize && hsize>0) {
				groupComp = c;
				break;
			}
		}
		if (groupComp==null)
			Assert.fail("没有找到选择框组CheckGroup");
		List<String> nameList = new ArrayList<String>();
		for (int i=checkedNames.length; i-->0; nameList.add(checkedNames[i]));
		for (Iterator liter=groupComp.getInnerComponentList(Hyperlink.class).iterator(), citer=groupComp.getInnerComponentList(CheckBox.class).iterator(); liter.hasNext();) {
			Hyperlink link = (Hyperlink)liter.next();
			CheckBox check = (CheckBox)citer.next();
			if (nameList.contains(link.getText())) {
				check.setSelected(true);
				nameList.remove(link.getText());
			} else {
				check.setSelected(false);
			}
		}
		if (nameList.size()>0)
			Assert.fail("选择框组CheckGroup没有"+nameList);
		else {
			groupComp.searchFormerByClass(OnchangeFormer.class).onComponentChange(null);
			groupComp.getEventListenerList().fireListener();
		}
	}
	
	public void setCheckGroup(String fieldName, String[] checkedNames) {
		Component groupComp = null;
		"12".toCharArray();
		for (Component c: window.getInnerComponentList(Container.class)) {
			int hsize=c.getInnerComponentList(Hyperlink.class).size(), csize=c.getInnerComponentList(CheckBox.class).size();
			if (hsize==csize && hsize>0) {
				if (c.searchFormerByClass(OnchangeFormer.class).getPropertyName().equals(fieldName)) {
					groupComp = c.getInnerComponentList(Hyperlink.class).get(0).searchCompByFormerClass(OnchangeFormer.class);
					break;
				}
			}
		}
		if (groupComp==null)
			Assert.fail("没有找到选择框组CheckGroup");
		List<String> nameList = new ArrayList<String>();
		for (int i=checkedNames.length; i-->0; nameList.add(checkedNames[i]));
		for (Iterator liter=groupComp.getInnerComponentList(Hyperlink.class).iterator(), citer=groupComp.getInnerComponentList(CheckBox.class).iterator(); liter.hasNext();) {
			Hyperlink link = (Hyperlink)liter.next();
			CheckBox check = (CheckBox)citer.next();
			if (nameList.contains(link.getText())) {
				check.setSelected(true);
				nameList.remove(link.getText());
			} else {
				check.setSelected(false);
			}
		}
		if (nameList.size()>0)
			Assert.fail("选择框组CheckGroup没有"+nameList);
		else {
			groupComp.searchFormerByClass(OnchangeFormer.class).onComponentChange(null);
			groupComp.getEventListenerList().fireListener();
		}
	}
	
	protected void setRadioGroup(String checkedName) {
		LinkedHashSet<Component> groupCompList = new LinkedHashSet<Component>();
		for (Component c: window.getInnerComponentList(Container.class)) {
			int hsize=c.getInnerComponentList(Hyperlink.class).size(), csize=c.getInnerComponentList(RadioButton.class).size();
			if (hsize==csize && hsize>0)
				groupCompList.add(c.getInnerComponentList(Hyperlink.class).get(0).searchCompByFormerClass(OnchangeFormer.class));
		}
		if (groupCompList.size()==0)
			Assert.fail("没有找到选择框组RadioGroup");
		List<String> nameList = new ArrayList<String>();
		nameList.add(checkedName);
		for (Component groupComp: groupCompList) {
			for (Iterator liter=groupComp.getInnerComponentList(Hyperlink.class).iterator(), citer=groupComp.getInnerComponentList(RadioButton.class).iterator(); liter.hasNext();) {
				Hyperlink link = (Hyperlink)liter.next();
				RadioButton check = (RadioButton)citer.next();
				boolean contain = false;
				for (String nm: nameList) {
					if (link.getText().indexOf(nm)>-1) {
						contain = true;
					}
				}
				if (contain==true) {
					check.setSelected(true);
					nameList.remove(checkedName);
				} else {
					check.setSelected(false);
				}
			}
			if (nameList.size()==0) {
				groupComp.searchFormerByClass(OnchangeFormer.class).onComponentChange(null);
				groupComp.getEventListenerList().fireListener();
				break;
			}
		}
		if (nameList.size()>0)
			Assert.fail("选择框组RadioGroup没有"+nameList);
	}
	
	protected boolean hasBuilder(Class<? extends FieldBuilder>... builderNames) {
		List<Class> nameList = new ArrayList<Class>();
		for (int ic=builderNames.length, i=0; i<ic; nameList.add(builderNames[i]),i++);
		for (Field field: window.getInnerFormerList(Field.class)) {
			if (nameList.contains(field.getFieldBuilder().getClass())) {
				nameList.remove(field.getFieldBuilder().getClass());
			}
		}
		for (AddNoteListener note: window.getInnerFormerList(AddNoteListener.class)) {
			FieldBuilder noteBuilder = note.getSourceBuilder();
			if ( nameList.contains(noteBuilder.getClass())) {
				nameList.remove(noteBuilder.getClass());
			}
		}
		return nameList.size()==0;
	}
	
	protected boolean hasNoteRead(String... fieldNames) {
		List<String> nameList = new ArrayList<String>();
		for (int ic=fieldNames.length, i=0; i<ic; nameList.add(fieldNames[i]),i++);
		for (Component text: window.getInnerFormerList(EditView.class).get(0).getComponent().getInnerComponentList("textnote")) {
			if ((text.getClass()==Text.class && (text.getFormer() instanceof Field))==false)
				continue;
			Field field = (Field)text.getFormer();
			FieldBuilder builder = field.getFieldBuilder();
			for (String fname: new ArrayList<String>(nameList)) { 
				if ((builder.getName().equals(fname) || builder.getFullName().endsWith(".".concat(fname)))==false) {
				} else {
					nameList.remove(fname);
					break;
				}
			}
		}
		return nameList.size()==0;
	}
	
	protected boolean hasNoteWrite(String... fieldNames) {
		List<String> nameList = new ArrayList<String>();
		for (int ic=fieldNames.length, i=0; i<ic; nameList.add(fieldNames[i]),i++);
		for (AddNoteListener note: window.getInnerFormerList(EditView.class).get(0).getComponent().getInnerFormerList(AddNoteListener.class)) {
			FieldBuilder builder = note.getSourceBuilder();
			for (String fname: new ArrayList<String>(nameList)) { 
				if ((builder.getName().equals(fname) || builder.getFullName().endsWith(".".concat(fname)))==false) {
				} else if ((note.getComponent() instanceof ValueSetable)==true) {
					nameList.remove(fname);
					break;
				}
			}
		}
		return nameList.size()==0;
	}
	
	public boolean hasField(String... fieldNames) {
		List<String> nameList = new ArrayList<String>();
		for (int ic=fieldNames.length, i=0; i<ic; nameList.add(fieldNames[i]),i++);
		for (Iterator<Field> iter=window.getInnerFormerComponentList(EditView.class).get(0).getInnerFormerList(Field.class).iterator(); iter.hasNext() && nameList.size()>0;) {
			Object f = iter.next();
			if (f.getClass()!=Field.class)			continue;
			Field field = (Field)f;
			FieldBuilder builder = field.getFieldBuilder();
			for (String fname: new ArrayList<String>(nameList)) { 
				if (builder.getName().equals(fname) || builder.getFullName().endsWith(".".concat(fname))) {
					nameList.remove(fname);
					break;
				}
			}
			if (builder instanceof ViewBuilder) {
				iter = new JoinedIterator(iter, ((ViewBuilder)builder).getFieldBuilderIterator());
			}
		}
		return nameList.size()==0;
	}
	
	protected boolean hasMenu(String menuname) {
		for (Menu menu: window.getInnerComponentList(Menu.class)) {
			if (StringUtils.equals(menu.getText(), menuname)) {
				return true;
			}
		}
		return false;
	}
	
	public static class TestModeList {
		
		private List<TestMode> modes = new ArrayList<TestMode>();
		private TradeMode trade = TradeMode.Common;
		private Date orderTime;
		private ConcurrentHashMap<String, Object> testMap = new ConcurrentHashMap<String, Object>();
		
		public TradeMode getTradeMode() {
			return this.trade;
		}
		
		public void setMode(TestMode... modeList) {
			this.modes.clear();
			for (TestMode m: modeList) {
				this.modes.add(m);
			}
		}
		
		public void addMode(TestMode... modeList) {
			for (TestMode m: modeList) {
				if (this.modes.contains(m)==false)
					this.modes.add(m);
			}
		}
		
		public TestMode[] getMode(TestMode... TestMode1) {
			List<TestMode> gets = new ArrayList<TestMode>();
			for (TestMode m: TestMode1) {
				if (this.modes.indexOf(m)>-1)
					gets.add(m);
			}
			return gets.toArray(new TestMode[0]);
		}
		
		public TestMode[] removeMode(TestMode... modeList) {
			List<TestMode> removes = new ArrayList<TestMode>();
			for (TestMode m: modeList) {
				if (this.modes.remove(m))
					removes.add(m);
			}
			return removes.toArray(new TestMode[0]);
		}
		
		public TestMode[] getModesByName(String types) {
			String[] typeList = types.split("[\\|]");
			TestMode[] modeList = new TestMode[typeList.length];
			int ti = 0;
			for (String t: typeList) {
				TestMode m = TestMode.valueOf(t);
				modeList[ti++] = m;
			}
			return modeList;
		}
		
		public String getModeLabel() {
			StringBuffer sb = new StringBuffer();
			for (TestMode m: modes) {
				sb.append(m.label);
			}
			return sb.toString();
		}
		
		public TestMode[] getModeList() {
			return this.modes.toArray(new TestMode[0]);
		}
		
		public boolean contain(TestMode... TestMode1) {
			int cmatch = TestMode1.length;
			for (TestMode m: modes) {
				for (TestMode mode: TestMode1) {
					if (m == mode)
						cmatch--;
				}
			}
			return cmatch==0? true: false;
		}
		
		protected Iterator<PrintViewSerial> getPrintViewIterator() {
			String k = "Iterator<PrintViewSerial>";
			Iterator<PrintViewSerial> iter = (Iterator<PrintViewSerial>)this.getTest(k);
			if (iter==null) {
				iter = new PrintViewSerialLogic().getTableRows().iterator();
				this.addTest(k, iter);
			}
			return iter;
		}
		
		protected void setPrintViewEnd() {
			PrintViewSerialLogic logic = new PrintViewSerialLogic();
			for (Iterator<PrintViewSerial> iter=this.getPrintViewIterator(); iter.hasNext();) {
				PrintViewSerial print = iter.next();
				print.getContent().delete(0, print.getContent().length());
				logic.save(print);
			}
			String k = "Iterator<PrintViewSerial>";
			this.testMap.remove(k);
			this.removeMode(TestMode.PrintView);
		}
		
		public OrderTicketForm getSelfOrderForm() {
			if (this.contain(TestMode.SubCompany))
				return new DOrderTicketForm();
			return new OrderTicketForm();
		}
		
		public ArrangeTicketForm getSelfArrangeForm() {
			if (this.contain(TestMode.Purchase))
				return new ArrangeTicketForm();
			return new PArrangeTicketForm();
		}
		
		public PurchaseTicketForm getSelfPurchaseForm() {
			if (this.contain(TestMode.Purchase))
				return new PurchaseTicketForm();
			return new PPurchaseTicketForm();
		}
		
		public OrderTicketTest getSelfOrderTest() {
			if (this.getMode(TestMode.ClientOrder, TestMode.SubcompanyOrder).length>0)
				return new LOrderTicketTest();
			else if (this.contain(TestMode.Purchase))
				return new OrderTicketTest();
			return new POrderTicketTest();
		}
		
		public PurchaseTicketTest getSelfPurchaseTest() {
			if (this.contain(TestMode.Purchase))
				return new PurchaseTicketTest();
			return new PPurchaseTicketTest();
		}
		
		public ReceiptTicketTest getSelfReceiptTest() {
			if (this.contain(TestMode.Purchase))
				return new ReceiptTicketTest();
			return new PReceiptTicketTest();
		}

		public Date getOrderTime() {
			return orderTime;
		}

		public void setOrderTime(Date orderTime) {
			this.orderTime = orderTime;
		}
		
		public void addTest(String key, Object value) {
			this.testMap.put(key, value);
		}
		
		public Object getTest(String key) {
			return this.testMap.get(key);
		}
		
		public void clearTest() {
			this.testMap.clear();
			this.orderTime = null;
		}
	}
	
	public static enum TestMode {
		Purchase("采购"),
		Product("生产"),
		SubCompany("分公司"),
		LinkAsClient("我为南宁的客户"),
		LinkAsSubcompany("我为南宁的子公司"),
		ClientOrder("吉高客户"),
		SubcompanyOrder("吉高分公司"),
		
		PrintView("打印测试界面"),
		;
		
		private String label;
		
		private TestMode(String label) {
			this.label = label;
		}
	}
	
	public static enum TradeMode {
		AnFangGongChon("安防工程"),
		Common("常规"),
		;
		
		private String label;
		
		private TradeMode(String label) {
			this.label = label;
		}
	}
}

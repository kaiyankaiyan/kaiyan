package com.haoyong.sales.common.form;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.sf.mily.attributes.AttrFunctionName;
import net.sf.mily.attributes.AttributeName;
import net.sf.mily.attributes.ClientEventName;
import net.sf.mily.attributes.StyleName;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.mappings.PresentClass;
import net.sf.mily.server.ActionNavigator;
import net.sf.mily.server.ActionRealm;
import net.sf.mily.server.ActionRealmItem;
import net.sf.mily.server.EditViewer;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.IFrame;
import net.sf.mily.ui.Menu;
import net.sf.mily.ui.Panel;
import net.sf.mily.ui.SelectNodeList;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.Tree;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.Tree.Node;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.ui.enumeration.FileName;
import net.sf.mily.ui.event.ActionListener;
import net.sf.mily.ui.event.EventObject;
import net.sf.mily.ui.facable.Former;
import net.sf.mily.ui.facable.Nodable;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.base.logic.UserPrivilegeLogic;
import com.haoyong.sales.common.derby.User4d;
import com.haoyong.sales.common.derby.User4dLogic;
import com.haoyong.sales.sale.form.GlobalSearchForm;
import com.haoyong.sales.util.SSaleUtil;

public class MainForm extends AbstractForm implements Former {
	
	protected void beforeWindow(Window window) {
		super.beforeWindow(window);
		window.addCSS(FileName.TABLE_CSS);
		window.addCSS(FileName.TREE_CSS);
		window.addCSS(FileName.SELECTLIST_CSS);
	}
	
	public Component getComponent() {
		BlockGrid grid = new BlockGrid().createGrid(3, BlockGridMode.NotOccupySizable);
		grid.addCSS(FileName.TABLE_CSS);
		grid.addAttribute(ClientEventName.InitScript0, new StringBuffer("window.document.title='").append(SSaleUtil.getProperties().getString("project.name")).append("'").toString());
		// col0
		grid.append(this.getMenuContentList());
		// col1
		grid.append(genLeftMenu());
		// col2
		grid.append(genRightPanel()).setOccupysizable();
		grid.addAttribute(AttributeName.Height, "100%").setIdentifier("mainf");
		if (true) {
			long period = TimeUnit.MINUTES.toSeconds(5);
//			period = TimeUnit.SECONDS.toSeconds(30);
			Calendar cur = Calendar.getInstance();
			long initialDelay = period - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)%period) - cur.get(Calendar.SECOND);
			IFrame iframe = grid.getInnerComponentList(IFrame.class).get(0);
			iframe.getEventListenerList().addCyclingScript("true", new StringBuffer("compList.addEvent($('").append(iframe.getIdentifier()).append("'))").toString(), period, initialDelay);
			iframe.getEventListenerList().addActionListener(this.getRefreshListener());
			this.getRefreshListener().setWaitList();
			this.getRefreshListener().setTab1Tab2Show(StringUtils.isBlank(this.getRefreshListener().linkWaitCount.getText())==false);
		}
		grid.setFormer(this);
		return grid;
	}
	
	private BlockGrid getMenuContentList() {
		BlockGrid grid = new BlockGrid().createGrid(3, BlockGridMode.Independent);
		grid.addStyle(StyleName.WIDTH, "300px").addStyle(StyleName.HEIGHT, "100%");
		if ("菜单树页1".length()>0) {
			grid.append(genLeftTree()).setIdentifier("tab1_right").addStyle(StyleName.BACKGROUND, "#E6EEFA");
		}
		if ("待处理页2".length()>0) {
			BlockGrid g = new BlockGrid().createGrid(1, BlockGridMode.Independent);
			this.getRefreshListener().tdSearchList = (Panel)g.append(null).getPanel().setIdentifier("td_search");
			this.getRefreshListener().tdWaitList = (Panel)g.append(null).getPanel().setIdentifier("td_wait");
			g.addStyle(StyleName.WIDTH, "100%").addAttribute(ClientEventName.ONCLICK, "$('td_tree').fhide();");
			grid.append(g).setIdentifier("tab2_wait").addStyle(StyleName.Display, "none").addStyle(StyleName.BACKGROUND, "#E6EEFA");
		}
		if ("浩进销介绍".length()>0) {
			BlockGrid g = new BlockGrid().createGrid(1, BlockGridMode.Independent);
			g.append(new Hyperlink("http://www.haosale.top", "浩平台介绍").setTarget("right"));
			g.append(new Hyperlink("http://www.haosale.top/privacypolicy/HaoSale.html", "隐私政策").setTarget("right"));
			g.addStyle(StyleName.WIDTH, "100%").addAttribute(ClientEventName.ONCLICK, "$('td_tree').fhide();");
			grid.append(g).setIdentifier("tab2_haosale").addStyle(StyleName.Display, "none").addStyle(StyleName.BACKGROUND, "#E6EEFA");
		}
		BlockGrid prt = new BlockGrid().createGrid(2, BlockGridMode.Independent);
		if ("隐藏按钮".length()>0) {
			prt.setIdentifier("td_tree").addStyle(StyleName.HEIGHT, "100%").addStyle(StyleName.Position, "absolute").addStyle(StyleName.Z_Index, "20").addStyle(StyleName.BACKGROUND, "#E6EEFA");
			prt.addAttribute(AttrFunctionName.FHide, "$('td_tree').style.display='none';$('_tree').style.display='none';");
			prt.addAttribute(AttrFunctionName.FShow, "$('td_tree').style.display='';$('_tree').style.display='';");
			Hyperlink link = new Hyperlink(null, "＜");
			link.addStyle(StyleName.FONT_SIZE, "14px").addStyle(StyleName.BORDER, "1px solid #609FDB").addStyle(StyleName.Padding, "10px 0px").addStyle(StyleName.BACKGROUND, "#3B86E0").addStyle(StyleName.FONT_WEIGHT, "bolder").addStyle(StyleName.COLOR, "white");
			prt.append(grid);
			Component tdHide = prt.append(link).setIdentifier("td_hide").addStyle(StyleName.Padding, "10px 0px").addAttribute(ClientEventName.ONCLICK, "$('td_tree').fhide();");
			tdHide.addAttribute(ClientEventName.ONMOUSEDOWN, "try{event.preventDefault();}catch(e){event.returnValue=false;}");
		}
		return prt;
	}
	
	private Component genLeftMenu() {
		BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		grid.addAttribute(ClientEventName.ONCLICK, "$('td_tree').fshow();if ($('_tree').selectedNode!=null) {$('_ptree').scrollTop=G_Element.getPosition($('_tree').selectedNode)[2];}");
		grid.addStyle(StyleName.HEIGHT, "100%").addStyle(StyleName.CURSOR, "pointer");
		BlockGrid g = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		g.append(new Text("︻"));
		g.append(new Text("菜"));
		g.append(new Text("单"));
		g.append(new Text("︼"));
		grid.append(g).setIdentifier("mn_tree").addAttribute(ClientEventName.ONCLICK, "this.fshow();")
			.addAttribute(AttrFunctionName.FShow, "$('tab1_right').style.display='';$('tab2_wait').style.display='none';$('tab2_haosale').style.display='none';");
		for (Iterator<BlockCell> iter=g.getInnerComponentList(BlockCell.class).iterator(); iter.hasNext();) {
			BlockCell c = iter.next();
			c.addStyle(StyleName.FONT_SIZE, "14px").addStyle(StyleName.Padding_Left, "3px");
		}
		g = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		g.append(new Text("︻"));
		g.append(new Text("待"));
		g.append(new Text("处"));
		g.append(new Text("理"));
		Hyperlink linkWait = new Hyperlink(null, "");
		g.append(null).getPanel().add(linkWait);
		g.append(new Text("︼"));
		this.getRefreshListener().linkWaitCount = linkWait;
		grid.append(g).setIdentifier("mn_wait").addAttribute(ClientEventName.ONCLICK, "this.fshow();")
			.addAttribute(AttrFunctionName.FShow, "$('tab1_right').style.display='none';$('tab2_wait').style.display='';$('tab2_haosale').style.display='none';");
		for (Iterator<BlockCell> iter=g.getInnerComponentList(BlockCell.class).iterator(); iter.hasNext();) {
			BlockCell c = iter.next();
			c.addStyle(StyleName.FONT_SIZE, "14px").addStyle(StyleName.Padding_Left, "3px");
		}
		grid.append(new Text(" ")).addStyle(StyleName.HEIGHT, "80%");
		g = new BlockGrid().createGrid(1, BlockGridMode.Independent);
		g.append(new Text("H"));
		g.append(new Text("A"));
		g.append(new Text("O"));
		g.append(new Text(" "));
		g.append(new Text("S"));
		g.append(new Text("A"));
		g.append(new Text("L"));
		g.append(new Text("E"));
		g.addStyle(StyleName.HEIGHT, "100%");
		grid.append(g).setIdentifier("mn_haosale").addAttribute(ClientEventName.ONCLICK, "this.fshow();")
			.addAttribute(AttrFunctionName.FShow, "$('tab1_right').style.display='none';$('tab2_wait').style.display='none';$('tab2_haosale').style.display='';");
		for (Iterator<BlockCell> iter=g.getInnerComponentList(BlockCell.class).iterator(); iter.hasNext();) {
			BlockCell c = iter.next();
			c.addStyle(StyleName.FONT_SIZE, "14px").addStyle(StyleName.FONT_FAMILY, "times New Roman").addStyle(StyleName.Padding_Left, "3px");
		}
		return grid;
	}
	
	private Component genLeftTree() {
		Tree tree = new Tree();
		tree.setIdentifier("_tree");
		List<String> userRightList = new UserPrivilegeLogic().getRightsOfUser(this.getUser());
		for (Iterator<ActionRealm> realmIter = ActionNavigator.getInstance().getRealmList().iterator(); realmIter.hasNext();) {
			ActionRealm realm = realmIter.next();
			if (new UserPrivilegeLogic().isContainRight(userRightList, realm.getName())==false)
				continue;
			Nodable realmNode = null;
			if (realm.isVisible()==false) {
				realmNode = tree;
			} else {
				Component realmComp = new RightNode(realm.getLabel(), realm.getName()).getHyperlink();
				realmNode = tree.addNode(realmComp);
			}
			for (Iterator<ActionRealmItem> actionIter = realm.getItemList().iterator(); actionIter.hasNext();) {
				ActionRealmItem formAction = actionIter.next();
				PresentClass formClass = EntityClass.forName(formAction.getClss());
				if (formClass == null || !formAction.isVisible())
					continue;
				Component formComp = new RightNode(formClass.getLabel(), formClass.getClss().getName()).getHyperlink();
				Node formNode = null;
				List<Component> viewcompList = new ArrayList<Component>();
				for (Iterator<ViewBuilder> viewIter = formClass.getViewIterator(); viewIter.hasNext();) {
					ViewBuilder viewBuilder = viewIter.next();
					String viewName = viewBuilder.getViewName();
					if (new UserPrivilegeLogic().isContainRight(userRightList, viewBuilder.getFullViewName())==false)
						continue;
					if (viewBuilder.getLabel()==null)
						continue;
					StringBuffer url = new StringBuffer("actionform.jsp");
					url.append("?action=").append(realm.getName()).append(".").append(formClass.getSimpleName());
					url.append("&view=").append(viewName);
					Component viewComp = genViewHyperlink(viewBuilder, url.toString());
					viewcompList.add(viewComp);
				}
				if (viewcompList.size()==0) {
				} else if (viewcompList.size()==1) {
					realmNode.addNode(viewcompList.get(0));
				} else {
					formNode = realmNode.addNode(formComp);
					for (Component viewComp : viewcompList) {
						formNode.addNode(viewComp);
					}
				}
			}
		}
		List<Hyperlink> list = new ArrayList<Hyperlink>();
		StringBuffer sb = new StringBuffer("Initial RightList").append(SellerLogic.getSellerId()).append("\n");
		for (RightNode node: tree.getInnerFormerList(RightNode.class)) {
			if (node.getSqlBuilder()==null)
				continue;
			sb.append(node.getRight()).append("\n");
			list.add(node.getHyperlink());
		}
//		DomainChangeTracing.getLog().info(sb.toString());
		this.getRefreshListener().treenodeList = list;
		SelectNodeList selectList = new SelectNodeList(tree);
		if (tree.getRootNodes().size()>0)		selectList.setSelectedValue(tree.getRootNodes().get(0));
		selectList.addStyle(StyleName.WIDTH, "100%");
		Panel panel = new Panel();
		panel.add(selectList);
		panel.scrollY().addStyle(StyleName.WIDTH, "300px").setIdentifier("_ptree").addAttribute(ClientEventName.InitScript0, "this.style.height=$('td_hide').offsetHeight;");
		return panel;
	}
	
	private Component genRightPanel() {
		IFrame iframe = new IFrame();
		iframe.setUri("about:blank");
		iframe.addStyle(StyleName.HEIGHT, "100%").addStyle(StyleName.WIDTH, "100%");
		iframe.addAttribute(AttributeName.NAME, "right");
		return iframe;
	}
	
	private Hyperlink genViewHyperlink(ViewBuilder builder, String url) {
		RightNode node = new RightNode(builder, builder.getFullViewName());
		Hyperlink link = node.getHyperlink();
		link.setTarget("right");
		link.setHref(url);
		link.addAttribute(ClientEventName.ONCLICK, "$('td_tree').fhide();");
		return link;
	}
	
	public static class RightNode {
		
		private Hyperlink link;
		private String right;
		private ViewBuilder builder;
		private AbstractForm form;
		private int searchCount;
		
		public RightNode(String label, String right) {
			this.right = right;
			this.link = new Hyperlink("#",label);
			this.link.setFormer(this);
		}
		
		public RightNode(ViewBuilder builder, String right) {
			this.builder = builder;
			this.right = right;
			this.link = new Hyperlink("#", builder.getLabel());
			this.link.setFormer(this);
		}
		
		public ViewBuilder getViewBuilder() {
			return this.builder;
		}
		
		public SqlListBuilder getSqlBuilder() {
			if (this.builder!=null) {
				List<SqlListBuilder> sqlList = this.builder.getFieldBuildersDeep(SqlListBuilder.class);
				if (sqlList.size()>0)
					return sqlList.get(0);
			}
			return null;
		}
		
		public Hyperlink getHyperlink() {
			return this.link;
		}
		
		public String getPathText() {
			StringBuffer sb = new StringBuffer();
			for(Node node0=null,node1=link.searchParentByClass(Node.class); node1!=null; node0=node1,node1=node0.searchParentByClass(Node.class)) {
				Hyperlink link = (Hyperlink)node1.getComponent();
				sb.insert(0, link.getText().concat("-"));
			}
			return sb.deleteCharAt(sb.length()-1).toString();
		}
		
		public String getRight() {
			return this.right;
		}
		
		public int getSearchCount() {
			return this.searchCount;
		}
		
		public void setSearchCount(int count) {
			this.searchCount = count;
		}
		
		public AbstractForm getForm() {
			if (this.form!=null)
				return this.form;
			ViewBuilder viewBuilder = this.getViewBuilder();
			AbstractForm actionForm = null;
			try {
				this.form = (AbstractForm)ReflectHelper.classForName(viewBuilder.getPresentClass().getName()).newInstance();
			}catch(Exception e) {
				LogUtil.error("在统计功能点待处理记录数，加载form失败".concat(viewBuilder.getPresentClass().getName()), e);
			}
			return this.form;
		}
	}

	@Override
	public void setSelectedList(List selected) {
	}
	
	private RefreshLeftTreeListener getRefreshListener() {
		RefreshLeftTreeListener l = (RefreshLeftTreeListener)this.getAttr(RefreshLeftTreeListener.class);
		if (l==null) {
			l = new RefreshLeftTreeListener();
			l.user = (User)WindowMonitor.getMonitor().getAttribute("user");
			this.setAttr(l);
		}
		return l;
	}
	
	private static class RefreshLeftTreeListener implements ActionListener {
		
		private List<Hyperlink> treenodeList;
		private Panel tdWaitList, tdSearchList;
		private Hyperlink linkWaitCount;
		private User user;
		
		public void perform(EventObject event) {
			this.setSearchList();
			this.setWaitList();
		}
		
		private void setSearchList() {
			BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
			grid.addStyle(StyleName.WIDTH, "100%");
			List<RightNode> searchList = (List<RightNode>)WindowMonitor.getMonitor().getAttribute("SearchRightList");
			if (searchList!=null && searchList.size()>0) {
				GlobalSearchForm storeForm = new GlobalSearchForm();
				Component editComp = new EditViewer((EditViewBuilder)EntityClass.loadViewBuilder(storeForm.getClass(), "Search"), storeForm, 1024).createView().getComponent();
				Window window = new Window();
				window.add(editComp);
				Menu menu = editComp.getInnerComponentList(Menu.class).get(0);
				menu.getEventListenerList().fireListener();
				searchList = storeForm.getSearchRightList();
			} else {
				return;
			}
			BlockCell tdLabel = grid.append(null);
			int count = 0;
			StringBuffer sb = new StringBuffer("MainForm Search\n");
			for (Hyperlink hyperlink: treenodeList) {
				RightNode nview = (RightNode)hyperlink.getFormer();
				for (RightNode search: searchList) {
					if (StringUtils.equals(search.getRight(), nview.getRight())) {
						sb.append(nview.getHyperlink().getText()).append(",").append(nview.getRight()).append(search.getSearchCount()).append("\n");
						count++;
						Hyperlink link = TicketPropertyUtil.copyProperties(hyperlink, new Hyperlink());
						link.setText(new StringBuffer(nview.getPathText()).append(search.getSearchCount()).toString());
						link.setHref(link.getHref().concat("&global=1"));
						grid.append(link).setStyleClass("ListViewCell");
						break;
					}
				}
			}
			if (tdSearchList.getComponent()==null) {
				this.tdSearchList.add(grid);
			} else {
				this.tdSearchList.getComponent().fireComponentReplace(grid);
			}
			if (count>0) {
//				LogUtil.info(sb.toString());
				tdLabel.setComponent(new Text("全局搜索"));
			}
			this.user.getVoParamMap().put("SearchFoundString", sb.toString());
			this.setMenuShow(count>0);
		}
		
		private void setWaitList() {
			List<User4d> remindList1=new User4dLogic().getUserList_1(), remindList2=new User4dLogic().getUserList_2(this.user.getUserName());
			BlockGrid grid = new BlockGrid().createGrid(1, BlockGridMode.Independent);
			grid.addStyle(StyleName.WIDTH, "100%");
			int count = 0;
			BlockCell tdLabel = grid.append(null);
			StringBuffer sb = new StringBuffer("MainForm Wait").append(SellerLogic.getSellerId()).append(" "+remindList1.size()).append(" "+remindList2.size()).append("\n");
			List<String> installRights = new UserPrivilegeLogic().getRoleLinkRightNames(new UserLogic().getInstallRoleName());
			boolean installRole = new UserLogic().isInstallRole(user);
			for (Hyperlink hyperlink: treenodeList) {
				RightNode nview = (RightNode)hyperlink.getFormer();
				User4d remind=null;
				if (installRole && installRights.contains(nview.getRight())) {
					for (Iterator<User4d> iter2=remindList2.iterator(); iter2.hasNext();) {
						User4d ritem = iter2.next();
						if (nview.getRight().equals(ritem.getUserName())) {
							remind = ritem;
							break;
						}
					}
				} else {
					for (Iterator<User4d> iter1=remindList1.iterator(); iter1.hasNext();) {
						User4d ritem = iter1.next();
						if (nview.getRight().equals(ritem.getUserName())) {
							remind = ritem;
							break;
						}
					}
				}
				sb.append(nview.getRight()).append(remind==null? null: remind.getRcount()).append("\n");
				if (remind != null) {
					sb.append(nview.getHyperlink().getText()).append(",").append(nview.getRight()).append(remind.getRcount()).append("\n");
					count += remind.getRcount();
					Hyperlink link = TicketPropertyUtil.copyProperties(hyperlink, new Hyperlink());
					link.setText(new StringBuffer(nview.getPathText()).append(remind.getRcount()).toString());
					link.setHref(link.getHref().concat("&wait=1"));
					grid.append(link).setStyleClass("ListViewCell");
				}
			}
			this.user.getVoParamMap().put("WaitFoundString", sb.toString());
//			DomainChangeTracing.getLog().info(sb.toString());
			if (tdWaitList.getComponent()==null) {
				this.tdWaitList.add(grid);
			} else {
				this.tdWaitList.getComponent().fireComponentReplace(grid);
			}
			Hyperlink link = linkWaitCount;
			this.setMenuShow(count>0);
			if (count==0) {
				link.setText("");
			} else {
				tdLabel.setComponent(new Text("待处理"));
				link.setText(new StringBuffer().append(count).toString());
			}
		}
		
		private void setMenuShow(boolean show) {
			Hyperlink link = linkWaitCount;
			Component linkGrid = link.searchParentByClass(BlockGrid.class).searchParentByClass(BlockGrid.class).getInnerComponentList(Text.class).get(1);
			linkGrid.addAttribute(ClientEventName.InitScript0, new StringBuffer(show? "$('mn_wait').fshow();": "$('mn_tree').fshow();").toString());
		}
		
		private void setTab1Tab2Show(boolean tab2Wait) {
			Hyperlink link = linkWaitCount;
			Component linkGrid = link.searchParentByClass(BlockGrid.class).searchParentByClass(BlockGrid.class).getInnerComponentList(Text.class).get(2);
			if (tab2Wait==false)
				linkGrid.addAttribute(ClientEventName.InitScript1, "$('mn_tree').fshow();");
			else
				linkGrid.addAttribute(ClientEventName.InitScript1, "$('mn_wait').fshow();");
		}
	}
}

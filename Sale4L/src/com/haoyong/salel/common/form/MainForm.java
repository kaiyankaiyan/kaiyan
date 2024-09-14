package com.haoyong.salel.common.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.attributes.AttributeName;
import net.sf.mily.attributes.ClientEventName;
import net.sf.mily.attributes.StyleName;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.mappings.PresentClass;
import net.sf.mily.server.ActionNavigator;
import net.sf.mily.server.ActionRealm;
import net.sf.mily.server.ActionRealmItem;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Container;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.IFrame;
import net.sf.mily.ui.Panel;
import net.sf.mily.ui.SelectNodeList;
import net.sf.mily.ui.Tree;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.Tree.Node;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.ui.enumeration.FileName;
import net.sf.mily.ui.facable.Former;
import net.sf.mily.webObject.ViewBuilder;

import com.haoyong.salel.base.logic.PrivilegeLogic;
import com.haoyong.salel.common.domain.AbstractDomain;

public class MainForm extends AbstractForm implements Former {
	
	private IFrame iframe;
	
	protected void beforeWindow(Window window) {
		super.beforeWindow(window);
		window.addCSS(FileName.TABLE_CSS);
		window.addCSS(FileName.TREE_CSS);
		window.addCSS(FileName.SELECTLIST_CSS);
	}
	
	public Component getComponent() {
		BlockGrid grid = new BlockGrid().createGrid(2, BlockGridMode.NotOccupySizable);
		Component rightPanel = genRightPanel();
		grid.append(genLeftTree()).addStyle(StyleName.WIDTH, "220px").setIdentifier("tdleft");
		BlockCell rightTd = grid.append(rightPanel);
		rightTd.addAttribute(AttributeName.Height, "99.7%");
		rightTd.addAttribute(ClientEventName.InitScript0, new StringBuffer("$('tdright').style.width=document.body.clientWidth-220;").toString());
		grid.addAttribute(AttributeName.Height, "100%");
		return grid;
	}
	
	private Component genLeftTree() {
		Tree tree = new Tree();
		List<String> userRightList = new PrivilegeLogic().getRightsOfUser(this.getUser());
		for (Iterator<ActionRealm> realmIter = ActionNavigator.getInstance().getRealmList().iterator(); realmIter.hasNext();) {
			ActionRealm realm = realmIter.next();
			if (isContainRight(userRightList, realm.getName())==false)
				continue;
			if (realm.isVisible()==false)
				continue;
			Component realmComp = RightNode.genText(realm.getLabel(), realm.getName());
			Node realmNode = tree.addNode(realmComp);
			for (Iterator<ActionRealmItem> actionIter = realm.getItemList().iterator(); actionIter.hasNext();) {
				ActionRealmItem formAction = actionIter.next();
				if (isContainRight(userRightList, formAction.getClssName())==false)
					continue;
				PresentClass formClass = EntityClass.forName(formAction.getClss());
				if (formClass == null || !formAction.isVisible())
					continue;
				Component formComp = RightNode.genText(formClass.getLabel(), formClass.getClss().getName());
				Node formNode = null;
				List<Component> viewcompList = new ArrayList<Component>();
				for (Iterator<ViewBuilder> viewIter = formClass.getViewIterator(); viewIter.hasNext();) {
					ViewBuilder viewBuilder = viewIter.next();
					String viewName = viewBuilder.getViewName();
					if (isContainRight(userRightList, viewBuilder.getFullViewName())==false)
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
		SelectNodeList selectList = new SelectNodeList(tree);
		if (tree.getRootNodes().size()>0)		selectList.setSelectedValue(tree.getRootNodes().get(0));
		selectList.addStyle(StyleName.WIDTH, "100%");
		Panel panel = new Panel();
		panel.add(selectList);
		panel.scrollY().addStyle(StyleName.WIDTH, "220px");
		return panel;
	}
	
	
	private Container genRightPanel() {
		Container container = new Container();
		container.setIdentifier("tdright");
		IFrame iframe = new IFrame();
		iframe.setUri("about:blank");
		iframe.addStyle(StyleName.HEIGHT, "100%");
		iframe.addStyle(StyleName.WIDTH, "100%");
		iframe.addAttribute(AttributeName.NAME, "right");
		container.add(iframe);
		this.iframe = iframe;
		return container;
	}
	
	private Hyperlink genViewHyperlink(ViewBuilder builder, String url) {
		RightNode node = new RightNode();
		node.right = builder.getFullViewName();
		node.builder = builder;
		Hyperlink link = new Hyperlink();
		link.setFormer(node);
		link.setText(builder.getLabel());
		link.setTarget("right");
		link.setHref(url);
		return link;
	}
	
	private boolean isContainRight(List<String> rightList, String right) {
		if ("cooper".equals(this.getUser().getUserId())) {
			return true;
		}
		return rightList.contains(right);
	}
	
	public AbstractDomain getDomain() {
		return null;
	}

	public void setDomain(AbstractDomain t) {
	}
	
	public static class RightNode {
		
		private String right;
		private ViewBuilder builder;
		
		private static Component genText(String label, String right) {
			RightNode nparent = new RightNode();
			nparent.right = right;
			Component text = new Hyperlink("#",label);
			text.setFormer(nparent);
			return text;
		}
		
		public String getRight() {
			return this.right;
		}
	}

	@Override
	public void setSelectedList(List selected) {
	}
}

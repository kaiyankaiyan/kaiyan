package com.haoyong.sales.base.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.mily.mappings.EntityClass;
import net.sf.mily.mappings.PresentClass;
import net.sf.mily.server.ActionNavigator;
import net.sf.mily.server.ActionRealm;
import net.sf.mily.server.ActionRealmItem;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.DesUtil;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.CheckBox;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.Tree;
import net.sf.mily.ui.Tree.Node;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.ui.event.ChangeListener;
import net.sf.mily.ui.event.EventObject;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.base.logic.UserPrivilegeLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.util.TicketUtil;

public class UserForm extends AbstractForm<User> implements FViewInitable {
	
	private User domain;
	private List<User> selectedList;
	
	public void canResetUser(List<List<Object>> valiRows) {
		// userId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	/**
	 * 检查登陆的用户是否有权限作删除操作
	 */
	public void canDeleteUser(List<List<Object>> valiRows) {
		// userId
		StringBuffer sb=new StringBuffer(), sitem=null;
		if ("cooper".equals(getUser().getUserId())) {
			sb.append("您没有权限作删除此用户，");
		} else {
			for (List<Object> row: valiRows) {
				sitem = new StringBuffer();
				if ("cooper".equals(((String)row.get(0)).toLowerCase()) || "cooper".equals(((String)row.get(0)).toLowerCase()))
					sitem.append("不能删除此用户，");
				if (sitem.length()>0)
					sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void prepareCreate4DeptUser(){
		domain = new User();
		domain.setLinkType(0);
		this.getSelectFormer4User().setSelectedList(new ArrayList<User>());
	}
	
	private void prepareCreate4DeptSub(){
		domain = new User();
		domain.setLinkType(3);
	}
	
	private void prepareCreate4Role(){
		domain = new User();
		domain.getSnapShot();
		domain.setLinkType(1);
	}
	
	public void prepareChangePassword() {
		this.domain = new UserLogic().getUser(this.getUser().getId());
		try {
			domain.setPassword(new DesUtil().getDecrypt(domain.getPassword()));
		}catch(Exception e) {
		}
		this.domain.getSnapShot();
		
	}
	
	public void prepareImport() {
		this.setDomain(new User());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	private void prepareEdit() {
		this.domain = selectedList.get(0);
		if ("admin".equals(getUser().getUserId())) {
		} else {
			if (!getUserName().equals(domain.getUserName())) {
				throw new LogicException(2, "不能编辑其他用户,您没有权限");
			}
		}
	}
	
	private void prepareRolePrivilege() {
		this.domain = selectedList.get(0);
		getRightList().clear();
		getRightList().addAll(new UserPrivilegeLogic().getRoleLinkRightNames(this.domain.getDeptName()));
	}
	
	private void prepareRoleActor() {
		this.domain = selectedList.get(0);
		getActorList().clear();
		getActorList().addAll(new UserPrivilegeLogic().getRoleLinkActorNames(this.getDomain().getDeptName()));
		getRightList().clear();
		getRightList().addAll(new UserPrivilegeLogic().getRoleLinkRightNames(this.domain.getDeptName()));
	}
	
	private void validateDeptUser() throws Exception {
		StringBuffer sb = validateUser(this.getDomain());
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private void validateDeptSub() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.getDomain().getUserName()))
			sb.append("部门名称不能为空，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private void validatePassword() {
		StringBuffer sb = new StringBuffer();
		int len=this.getDomain().getPassword().trim().length();
		if (!(8<=len && len<=16))
			sb.append("密码必须有8~16位，");
		if (StringUtils.equals(this.getDomain().getPassword(), (String)this.getDomain().getVoParamMap().get("password"))==false)
			sb.append("重复密码要跟密码一致，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private StringBuffer validateUser(User entity) throws Exception {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(entity.getDeptName()))
			sb.append("部门名称不能为空，");
		if (StringUtils.isBlank(entity.getUserId()))
			sb.append("登陆名不能为空，");
		else if (!TicketUtil.isValid("bs_user", "userId", entity,  entity.getUserId()))
			sb.append("此登陆名").append(entity.getUserId()).append("已经存在，");
		if (StringUtils.isBlank(entity.getUserName()))
			sb.append("用户名不能为空，");
		else if (new UserLogic().isSingleUserName(entity)==false)
			sb.append("此用户").append(entity.getUserName()).append("已经存在，");
		if (entity.getId()>0 && StringUtils.isBlank(entity.getPassword()))
			sb.append("密码不能为空，");
		return sb;
	}
	
	public void validateRoleUser() {
		StringBuffer sb = new StringBuffer();
		if (new UserPrivilegeLogic().isSingleRoleUser(getDomain())==false)
			sb.append("已经存在此岗位的用户，");
		if (StringUtils.isBlank(this.getDomain().getUserName())==false && new UserLogic().isContainUserName(getDomain().getUserName())==false)
			sb.append("不存在此用户建档，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateToImport(Component fcomp) {
		this.getBaseImportForm().validateIndexes(fcomp);
		StringBuffer sb = new StringBuffer();
		StringBuffer input = new StringBuffer().append(getDomain().getVoParamMap().get("Remark"));
		if ("null".equals(input.toString()))
			sb.append("请粘贴入单元格内容，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else {
			// 保存列序号配置到Derby
			ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
			SellerViewInputs inputs = this.getBaseImportForm().getSellerViewInputs();
			this.getBaseImportForm().setSellerIndexes(listview, inputs);
			new SellerViewInputsLogic().saveOrUpdate(inputs);
		}
	}
	
	public void validateImport(Component fcomp) throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (getImportList().size()==0)
			sb.append("导入明细为空，");
		int ri=1;
		for (Iterator<User> iter=getImportList().iterator(); iter.hasNext(); ri++) {
			User d = iter.next();
			sitem = validateUser(d);
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateRolePrivilege() {
	}
	
	public void validateRoleActor() {
	}
	
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true)
			new UserLogic().getChoosableLogic().chooseViewBuilder(viewBuilder);
		if (viewBuilder.getName().equals("ImportUser"))
			this.getBaseImportForm().setImportBuilderInit(viewBuilder);
	}
	
	private void setUserA4Service(ViewData<User> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	
	private void setUserL4Service(ViewData<User> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4User().getSelectedList());
	}
	
	private void setImport4Service(ViewData<User> viewData) {
		viewData.setTicketDetails(this.getImportList());
	}
	
	public void setRolePrivilege4Service(ViewData<User> viewData) {
		List<User> sourceList = new UserPrivilegeLogic().getRoleLinkRights(getDomain().getDeptName());
		List<User> toList = new ArrayList<User>();
		Iterator<User> sourceIter = sourceList.iterator();
		for (String right: getRightList()) {
			User link = sourceIter.hasNext()? sourceIter.next(): new User();
			link.setLinkType(11);
			link.setDeptName(getDomain().getDeptName());
			link.setUserName(right);
			toList.add(link);
		}
		List<User> deleteList = new ArrayList<User>(sourceList);
		deleteList.removeAll(toList);
		viewData.setTicketDetails(toList);
		viewData.setParam("DeleteList", deleteList);
	}
	
	private void setRoleActor4Service(ViewData<User> viewData) {
		List<User> sourceList = new UserPrivilegeLogic().getRoleLinkActors(getDomain().getDeptName());
		List<User> toList = new ArrayList<User>();
		Iterator<User> sourceIter = sourceList.iterator();
		for (String actor: getActorList()) {
			User link = sourceIter.hasNext()? sourceIter.next(): new User();
			link.setLinkType(21);
			link.setDeptName(getDomain().getDeptName());
			link.setUserName(actor);
			toList.add(link);
		}
		List<User> deleteList = new ArrayList<User>(sourceList);
		deleteList.removeAll(toList);
		viewData.setTicketDetails(toList);
		viewData.setParam("DeleteList", deleteList);
	}
	
	// 岗位人员列表删除人员
	private void setRoleUserDelete4Service(ViewData<User> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4User().getSelectedList());
	}
	
	// 岗位人员列表添加人员
	private void setRoleUserAdd4Service(ViewData<User> viewData) {
		List<User> list = new ArrayList<User>();
		for (User u: this.getSelectFormer4User().getSelectedList()) {
			User link = new User();
			link.setUserName(u.getUserName());
			link.setLinkType(12);
			link.setDeptName(this.getDomain().getDeptName());
			list.add(link);
		}
		viewData.setTicketDetails(list);
	}

	public List<User> getSelectedList() {
		return this.selectedList;
	}

	@Override
	public void setSelectedList(List<User> selected) {
		this.selectedList = selected;
	}
	
	public List<User> getImportList() {
		String k = "ImportList";
		List<User> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<User>();
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
	
	public void setImportFormated(Component fcomp) throws Exception {
		List<User> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}
	
	private List<String> getRightList() {
		String k = "RightList";
		List<String> list = getAttr(k);
		if (list == null) {
			list = new ArrayList<String>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private Tree getRightTree() {
		String k="RightTree", kmap="RightNodeNameMap";
		Tree tree = this.getAttr(k);
		List<String> userRightList = new UserPrivilegeLogic().getRightsOfUser(this.getUser());
		if (tree == null) {
			tree = new Tree();
			LinkedHashMap<String, Object> nameMap = new LinkedHashMap<String, Object>();
			for (Iterator<ActionRealm> realmIter = ActionNavigator.getInstance().getRealmList().iterator(); realmIter.hasNext();) {
				ActionRealm realm = realmIter.next();
				if (new UserPrivilegeLogic().isContainRight(userRightList, realm.getName())==false)
					continue;
				BlockGrid realmComp = new BlockGrid().createGrid(2, BlockGridMode.Independent);
				realmComp.append(new CheckBox());
				realmComp.append(new Hyperlink("#",realm.getLabel()));
				realmComp.setFormer(realm);
				nameMap.put(realm.getName(), realm);
				Node realmNode = tree.addNode(realmComp);
				for (Iterator<ActionRealmItem> actionIter = realm.getItemList().iterator(); actionIter.hasNext();) {
					ActionRealmItem formAction = actionIter.next();
					PresentClass formClass = null;
					try {
						formClass = EntityClass.forName(formAction.getClss());
					} catch(Exception e) {
						continue;
					}
					if (formClass == null || !formAction.isVisible())
						continue;
					BlockGrid formComp = new BlockGrid().createGrid(2, BlockGridMode.Independent);
					formComp.append(new CheckBox());
					formComp.append(new Hyperlink(null, formClass.getLabel()));
					formComp.setFormer(formClass);
					nameMap.put(formClass.getBeanClass().getName(), formClass);
					Node formNode = null;
					List<Component> viewcompList = new ArrayList<Component>();
					for (Iterator<ViewBuilder> viewIter = formClass.getViewIterator(); viewIter.hasNext();) {
						ViewBuilder viewBuilder = viewIter.next();
						String viewName = viewBuilder.getViewName();
						if (viewBuilder.getLabel()==null)
							continue;
						if (new UserPrivilegeLogic().isContainRight(userRightList, viewBuilder.getFullViewName())==false)
							continue;
						BlockGrid viewComp = new BlockGrid().createGrid(2, BlockGridMode.Independent);
						viewComp.append(new CheckBox());
						viewComp.append(new Hyperlink(null, viewBuilder.getLabel()));
						viewComp.setFormer(viewBuilder);
						nameMap.put(viewBuilder.getFullViewName(), viewBuilder);
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
			this.setAttr(k, tree);
			this.setAttr(kmap, nameMap);
		}
		return tree;
	}
	
	private void setRolePrivilegeLink(Hyperlink link, List<String> rightList, UserForm form) {
		String kmap="RightNodeNameMap";
		Tree tree = this.getRightTree();
		LinkedHashMap<String, Object> nameMap = this.getAttr(kmap);
		OnNodeSelectListener listener = new OnNodeSelectListener();
		for (Node node: tree.getInnerComponentList(Node.class)) {
			if (node.getNodes().size()==0) {
			} else if (node.getComponent().getFormer() instanceof ActionRealm) {
				node.setOpened(true);
			} else {
				node.setOpened(false);
			}
		}
		for (CheckBox check: tree.getInnerComponentList(CheckBox.class)) {
			check.setDisable(false);
			check.setSelected(false);
			check.getEventListenerList().addChangeListener(listener);
		}
		for (String right: rightList) {
			tree.getInnerComponentByFormer(nameMap.get(right));
			BlockGrid grid = (BlockGrid)tree.getInnerComponentByFormer(nameMap.get(right));
			if (grid!=null)
				grid.getInnerComponentList(CheckBox.class).get(0).setSelected(true);
		}
		link.add(tree);
	}
	
	private void setRoleRightList() {
		String kmap="RightNodeNameMap";
		Tree tree = this.getRightTree();
		LinkedHashMap<String, Object> nameMap = this.getAttr(kmap);
		List<String> rightList = new ArrayList<String>();
		for (CheckBox check: tree.getInnerComponentList(CheckBox.class)) {
			if (check.isSelected()) {
				int idx = new ArrayList<Object>(nameMap.values()).indexOf(check.searchFormer());
				String right = new ArrayList<String>(nameMap.keySet()).get(idx);
				rightList.add(right);
			}
		}
		this.getRightList().clear();
		this.getRightList().addAll(rightList);
	}
	
	private void setRoleRightEmpty() {
		Tree tree = this.getRightTree();
		for (CheckBox check: tree.getInnerComponentList(CheckBox.class)) {
			if (check.isSelected())
				check.setSelected(false);
		}
	}
	
	private static class OnNodeSelectListener implements ChangeListener {
		
		public void perform(EventObject e) {
			CheckBox check = (CheckBox)e.getSource();
			Node node = e.getSource().searchParentByClass(Node.class);
			if (node.getParentNode()!=null)
				setParentNodeSelect(node, check.isSelected());
			if (node.getNodes().size()>0)
				setChildNodeSelect(node, check.isSelected());
		}
		
		private void setChildNodeSelect(Node current, boolean checked) {
			for (Node item: current.getNodes()) {
				item.getComponent().getInnerComponentList(CheckBox.class).get(0).setSelected(checked);
				if (item.getNodes().size()>0)
					setChildNodeSelect(item, checked);
			}
		}
		
		private void setParentNodeSelect(Node current, boolean checked) {
			if (checked) {
				for (Node cur=current,prt=cur.getParentNode(); prt!=null; cur=prt,prt=cur==null? null: cur.getParentNode()) {
					CheckBox pcheck = prt.getComponent().getInnerComponentList(CheckBox.class).get(0);
					if (pcheck.isSelected() ==false)
						pcheck.setSelected(true);
					else
						break;
				}
			} else {
				Node prtNode = current.getParentNode();
				for (Iterator<Node> iter=prtNode.getNodes().iterator(); iter.hasNext();) {
					Node item = iter.next();
					CheckBox cCheck = item.getComponent().getInnerComponentList(CheckBox.class).get(0);
					if (cCheck.isSelected() == true)
						break;
					if (iter.hasNext()==false) {
						CheckBox pCheck = prtNode.getComponent().getInnerComponentList(CheckBox.class).get(0);
						if (pCheck.isSelected()) {
							pCheck.setSelected(false);
							if (prtNode.getParentNode()!=null)
								setParentNodeSelect(prtNode, false);
						}
					}
				}
			}
		}
	}
	
	private void setRoleActorLink(Hyperlink link, List<String> actorList, UserForm form) {
		BlockGrid grid = new BlockGrid().createGrid(4, BlockGridMode.Independent);
		List<String> deptList = new UserPrivilegeLogic().getActorDepts();
		LinkedHashMap<String, BlockGrid> actorMap = new LinkedHashMap<String, BlockGrid>();
		for (String dept: deptList) {
			BlockGrid g = new BlockGrid().createGrid(2);
			g.append(new CheckBox());
			g.append(new Text(dept));
			actorMap.put(dept, g);
			grid.append(g);
		}
		link.add(grid);
		this.setAttr("RoleLinkActor_ActorMap", actorMap);
		for (String dept: actorList) {
			BlockGrid g = actorMap.get(dept);
			if (g==null)	continue;
			g.getInnerComponentList(CheckBox.class).get(0).setSelected(true);
		}
	}
	
	private void setRoleActor4RightLink(Hyperlink link, List<String> rightList, UserForm form) {
		String kmap="RightNodeNameMap";
		Tree tree = this.getRightTree();
		LinkedHashMap<String, Object> nameMap = this.getAttr(kmap);
		for (Node node: tree.getInnerComponentList(Node.class)) {
			if (node.getNodes().size()>0)
				node.setOpened(false);
		}
		for (CheckBox check: tree.getInnerComponentList(CheckBox.class)) {
			check.setDisable(true);
			check.setSelected(false);
		}
		for (String right: rightList) {
			BlockGrid grid = (BlockGrid)tree.getInnerComponentByFormer(nameMap.get(right));
			if (grid==null)
				continue;
			grid.getInnerComponentList(CheckBox.class).get(0).setSelected(true);
			Node node = grid.searchParentByClass(Node.class);
			node.setOpened(true);
		}
		link.add(tree);
	}
	
	private List<String> getActorList() {
		String k = "RoleLinkActor_Actors";
		List<String> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<String>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private void setRoleActorList() {
		String kmap="RoleLinkActor_ActorMap";
		LinkedHashMap<String, BlockGrid> actorMap = this.getAttr(kmap);
		List<String> actorList = new ArrayList<String>();
		for (Iterator<Map.Entry<String, BlockGrid>> enIter=new ArrayList(actorMap.entrySet()).iterator(); enIter.hasNext();) {
			Map.Entry<String, BlockGrid> entry = enIter.next();
			String actor = entry.getKey();
			BlockGrid g = entry.getValue();
			if (g.getInnerComponentList(CheckBox.class).get(0).isSelected()) {
				actorList.add(actor);
			}
		}
		this.getActorList().clear();
		this.getActorList().addAll(actorList);
	}
	
	private void setRoleActorEmpty() {
		String kmap="RoleLinkActor_ActorMap";
		LinkedHashMap<String, BlockGrid> actorMap = this.getAttr(kmap);
		for (BlockGrid g: actorMap.values()) {
			g.getInnerComponentList(CheckBox.class).get(0).setSelected(false);
		}
	}
	
	public UserForm getForm() {
		return this;
	}
	
	public User getDomain() {
		return domain;
	}

	public void setDomain(User t) {
		this.domain = t ;
	}
	
	private User getAUser() {
		String k = "FormUser";
		User d = this.getAttr(k);
		if (d == null) {
			d = new User();
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
	
	/**
	 * 重置用户密码
	 */
	private void setUserReset(){
		for (User user: this.getSelectFormer4User().getSelectedList()) {
			user.setPassword(new DesUtil().getEncrypt(user.getUserName().concat(user.getUserId())));
		}
	}
	private void setUserResetD(){
		this.domain.getSnapShot();
		this.domain.setPassword(this.domain.getUserName().concat(this.domain.getUserId()));
	}
	
	/**
	 * 用户密码加密
	 */
	private void setUserEncode(State state, ViewData<User> viewData){
		for (User user: viewData.getTicketDetails()) {
			User cur=user, src=user.getSnapShot();
			if (StringUtils.equals(cur.getPassword(), src.getPassword())==false) {
				String psw = user.getPassword();
				user.setPassword(new DesUtil().getEncrypt(psw));
			}
		}
	}
	
	private HashMap<String, String> getParam4RoleUser() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("RoleName", new StringBuffer().append("'").append(this.getDomain().getDeptName()).append("'").toString());
		return map;
	}
	private HashMap<String, String> getParam4DeptList() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("SellerId", this.getSellerId()+"");
		return map;
	}
	
	public SelectTicketFormer4Sql<UserForm, User> getSelectFormer4User() {
		String k = "SelectFormer4User";
		SelectTicketFormer4Sql<UserForm, User> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<UserForm, User>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.Choose12 logic = new UserLogic().getChoosableLogic();
		former.setViewBuilder(logic.getChooseBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getChooseBuilder()));
		return former;
	}
}

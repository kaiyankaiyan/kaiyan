package com.haoyong.salel.base.form;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.mappings.EntityClass;
import net.sf.mily.mappings.PresentClass;
import net.sf.mily.server.ActionNavigator;
import net.sf.mily.server.ActionRealm;
import net.sf.mily.server.ActionRealmItem;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.DesUtil;
import net.sf.mily.types.IntegerType;
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
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.base.logic.PrivilegeLogic;
import com.haoyong.salel.base.logic.UserLogic;
import com.haoyong.salel.common.form.AbstractForm;
import com.haoyong.salel.common.form.ViewData;
import com.haoyong.salel.util.TicketUtil;

public class UserForm extends AbstractForm<User> {
	
	private User domain;
	private List<User> selectedList;
	
	public void canResetUser(List<List<Object>> valiRows) {
		// userId
		StringBuffer sb = new StringBuffer();
		if (!"cooper".equals(getUser().getUserId())) {
			sb.append("只有admin才能使用重置功能，");
		}
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
	
	public void prepareCreate4Dept(){
		domain = new User();
		domain.setLinkType(0);
	}
	
	public void prepareCreate4Role(){
		domain = new User();
		domain.setLinkType(1);
	}
	
	public void prepareImport() {
		this.setDomain(new User());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	public void prepareEdit() {
		this.domain = selectedList.get(0);
		this.domain.setPassword(new DesUtil().getDecrypt(domain.getPassword()));
		if ("cooper".equals(getUser().getUserId())) {
		} else {
			if (!getUserName().equals(domain.getUserName())) {
				throw new LogicException(2, "不能编辑其他用户,您没有权限");
			}
		}
	}
	
	public void prepareRolePrivilege() {
		this.domain = selectedList.get(0);
		getRightList().clear();
		getRightList().addAll(new PrivilegeLogic().getRoleLinkRightNames(this.domain.getDeptName()));
	}
	
	public void prepareRoleActor() {
		this.domain = selectedList.get(0);
		getActorList().clear();
		getActorList().addAll(new PrivilegeLogic().getRoleLinkActorNames(this.getDomain().getDeptName()));
		getRightList().clear();
		getRightList().addAll(new PrivilegeLogic().getRoleLinkRightNames(this.domain.getDeptName()));
	}
	
	public void validateDeptUser() throws Exception {
		StringBuffer sb = validateUser(this.getDomain());
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
		if (StringUtils.isBlank(entity.getPassword()))
			sb.append("密码不能为空，");
		return sb;
	}
	
	public void validateRoleUser() {
		StringBuffer sb = new StringBuffer();
		if (new PrivilegeLogic().isSingleRoleUser(getDomain())==false)
			sb.append("已经存在此岗位的用户，");
		if (new UserLogic().isContainUserName(getDomain().getUserName())==false)
			sb.append("不存在此用户建档，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateToImport() throws Exception {
		StringBuffer sb = new StringBuffer();
		StringBuffer input = new StringBuffer().append(getDomain().getVoParamMap().get("Remark"));
		if ("null".equals(input.toString()))
			sb.append("请粘贴入单元格内容，");
		if (true) {
			IntegerType type = new IntegerType();
			Set<Integer> colList = new HashSet<Integer>();
			Map<TextFieldBuilder, Object> valueMap = getEntityPropertyValue(getDomain());
			StringBuffer sitem=new StringBuffer(), serror=new StringBuffer();
			int cok = 0;
			for (Iterator<TextFieldBuilder> iter=valueMap.keySet().iterator(); iter.hasNext();) {
				TextFieldBuilder builder = iter.next();
				Object ovalue = valueMap.get(builder);
				String svalue = ovalue+"";
				Integer ivalue = null;
				if (ovalue==null)
					continue;
				try {
					ivalue = type.parse(svalue);
					if (ivalue < 1) {
						sitem.append("列序号必须大于0,");
					} else if (colList.add(ivalue)==false) {
						sitem.append("存在相同的列序号").append(ivalue).append(",");
					} else {
						cok++;
					}
				} catch(Exception e) {
					serror.append(ovalue).append(",");
				}
			}
			if (sitem.length()>0)
				sb.append(sitem);
			if (serror.length()>0)
				sb.append("列序号格式错误").append(serror);
			if (sb.length()==0 && cok==0)
				sb.append("请输入列序号，");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateImport() throws Exception {
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
	
	public void setUserA4Service(ViewData<User> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	
	private void setUserL4Service(ViewData<User> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4User().getSelectedList());
	}
	
	public void setImport4Service(ViewData<User> viewData) {
		viewData.setTicketDetails(this.getImportList());
	}
	
	public void setRolePrivilege4Service(ViewData<User> viewData) {
		List<User> sourceList = new PrivilegeLogic().getRoleLinkRights(getDomain().getDeptName());
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
	
	public void setRoleActor4Service(ViewData<User> viewData) {
		List<User> sourceList = new PrivilegeLogic().getRoleLinkActors(getDomain().getDeptName());
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
	
	public Map<TextFieldBuilder, Object> getEntityPropertyValue(User entity) {
		LinkedHashMap<TextFieldBuilder, Object> map = new LinkedHashMap<TextFieldBuilder, Object>();
		ViewBuilder bomBuilder = this.getImportBuilder();
		List<TextFieldBuilder> list = bomBuilder.getFieldBuildersDeep(TextFieldBuilder.class);
		for (TextFieldBuilder b: list) {
			StringBuffer name = new StringBuffer();
			name.append(b.getName());
			for (ViewBuilder cur=b.getViewBuilder(),prt=cur.getViewBuilder(); cur.getPresentClass().getBeanClass()!=entity.getClass(); cur=prt,prt=cur==null? null: cur.getViewBuilder()) {
				name.insert(0, cur.getName().concat("."));
			}
			Object value = ReflectHelper.getPropertyValue(entity, name.toString());
			map.put(b, value);
		}
		return map;
	}
	
	public void setImportLabelIndex() throws ParseException {
		ViewBuilder bomBuilder = this.getImportBuilder();
		int ci = 0;
		for (TextFieldBuilder builder: new ArrayList<TextFieldBuilder>(bomBuilder.getFieldBuildersDeep(TextFieldBuilder.class))) {
			builder.setEntityPropertyValue(getDomain(), ++ci);
		}
	}
	
	public void setImportFormated() throws Exception {
		List<String> tList = new ArrayList<String>();
		StringBuffer input = new StringBuffer().append(getDomain().getVoParamMap().get("Remark"));
		tList.addAll(Arrays.asList(input.toString().split("[\t]")));
		for (int ti=tList.size(); ti-->0; ) {
			String t = tList.get(ti);
			if (t.startsWith("\"") && t.endsWith("\""))
				tList.set(ti, new StringBuffer(t).deleteCharAt(t.length()-1).deleteCharAt(0).toString());
			else if (t.indexOf("\n") > -1) {
				List<String> subs = new ArrayList<String>();
				subs.add(t.substring(0, t.indexOf("\n")));
				subs.add("\n");
				subs.add(t.substring(t.indexOf("\n")+1));
				tList.remove(ti);
				tList.addAll(ti, subs);
			}
		}
		ViewBuilder importBuilder = (ViewBuilder)this.getImportBuilder().getViewBuilder().getFieldBuilder("importList");
		Map<TextFieldBuilder, Object> valueMap = this.getEntityPropertyValue(this.getDomain());
		LinkedHashMap<Integer, FieldBuilder> fieldList = new LinkedHashMap<Integer, FieldBuilder>();
		IntegerType type = new IntegerType();
		for (TextFieldBuilder b: valueMap.keySet()) {
			if (valueMap.get(b)==null)
				continue;
			Integer col = type.parse(valueMap.get(b)+"");
			FieldBuilder colb = importBuilder.getFieldBuilder(b.getName());
			fieldList.put(col, colb);
		}
		for (int ti=0, tsize=tList.size(); ti<tsize; ti++) {
			User detail = new User();
			getImportList().add(detail);
			StringBuffer error = new StringBuffer();
			for (int ci=0, csize=fieldList.size(); ci<csize && ti<tsize; ci++, ti++) {
				String t=tList.get(ti);
				FieldBuilder builder = fieldList.get(ci+1);
				if (builder==null)
					continue;
				if ("\n".equals(t))
					break;
				try {
					Object v=builder.getFormatType().parse(t), v1=null;
					if (!(builder instanceof TextFieldBuilder)) {
						v1 = builder.pickValue(builder.build(v).getComponent());
						builder.setEntityPropertyValue(detail, v1);
						if (StringUtils.equals(v+"", v1+"")==false)
							throw new ParseException(new StringBuffer().append(v).append(v1).toString(), 0);
					} else {
						builder.setEntityPropertyValue(detail, v);
					}
				} catch(Exception e) {
					error.append(builder.getLabel()).append(t).append(",");
				}
				if (ci+1==csize) {
					for (ti++; ti<tsize && "\n".equals(tList.get(ti))==false; ti++);
					break;
				}
			}
			detail.getVoParamMap().put("error", error.toString());
		}
	}
	
	private ViewBuilder getImportBuilder() {
		String k = "ImportBuilder";
		ViewBuilder builder = this.getAttr(k);
		return builder;
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
		if (tree == null) {
			tree = new Tree();
			LinkedHashMap<String, Object> nameMap = new LinkedHashMap<String, Object>();
			for (Iterator<ActionRealm> realmIter = ActionNavigator.getInstance().getRealmList().iterator(); realmIter.hasNext();) {
				ActionRealm realm = realmIter.next();
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
			BlockGrid grid = (BlockGrid)tree.getInnerComponentByFormer(nameMap.get(right));
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
		List<String> deptList = new PrivilegeLogic().getActorDepts();
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
			actorMap.get(dept).getInnerComponentList(CheckBox.class).get(0).setSelected(true);
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
			grid.getInnerComponentList(CheckBox.class).get(0).setSelected(true);
			Node node = grid.searchParentByClass(Node.class);
			if (grid.getFormer() instanceof ActionRealm && node.getNodes().size()>0)
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
	
	private SelectTicketFormer4Sql<UserForm, User> getSelectFormer4User() {
		String k="SelectFormer4User";
		SelectTicketFormer4Sql<UserForm, User> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<UserForm, User>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	private UserForm getForm() {
		return this;
	}
	
	public User getDomain() {
		return domain;
	}

	public void setDomain(User t) {
		this.domain = t ;
	}
	
	/**
	 * 重置用户密码
	 */
	public void setUserReset(){
		this.domain=selectedList.get(0);
		this.domain.setPassword(new DesUtil().getEncrypt(this.domain.getUserName().concat(this.domain.getUserId())));
	}
	
	private String getForwardFromBuilder() {
		ViewBuilder builder = this.getAttr("forwardFromBuilder");
		return builder.getName();
	}
}

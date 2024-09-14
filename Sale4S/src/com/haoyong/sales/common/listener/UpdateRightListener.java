package com.haoyong.sales.common.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.Tree.Node;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.UserPrivilegeLogic;
import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.MainForm;
import com.haoyong.sales.common.form.MainForm.RightNode;

public class UpdateRightListener extends TransRunnableListener {
	
	@Override
	public void runTask() throws Exception {
		MainForm form = new MainForm();
		List<Hyperlink> linkList = form.getComponent().getInnerFormerComponentList(RightNode.class);
		List<User> preList=new UserPrivilegeLogic().getRightAll();
		List<User> changeList=new ArrayList<User>(), unchangeList=new ArrayList<User>();
		for (Iterator linkIter=linkList.iterator(), preIter=preList.iterator(); linkIter.hasNext();) {
			Hyperlink link = (Hyperlink)linkIter.next();
			User right = preIter.hasNext()? (User)preIter.next(): new User();
			User source = right.getId()>0? (User)right.clone(): new User();
			EqualsBuilder equalsb = new EqualsBuilder();
			right.setLinkType(2);
			equalsb.append(right.getLinkType(), source.getLinkType());
			StringBuffer fullPath = new StringBuffer();
			for (Node cur=link.searchParentByClass(Node.class),prt=(cur==null? null: cur.getParentNode()); cur!=null; cur=prt,prt=(cur==null? null: cur.getParentNode())) {
				Hyperlink curlink = cur.getInnerComponentList(Hyperlink.class).get(0);
				fullPath.insert(0, curlink.getText().concat(curlink==link? "": "-"));
			}
			right.setUserName(fullPath.toString());
			equalsb.append(right.getUserName(), source.getUserName());
			right.setDeptName(((RightNode)link.getFormer()).getRight());
			equalsb.append(right.getDeptName(), source.getDeptName());
			if (equalsb.isEquals())
				unchangeList.add(right);
			else
				changeList.add(right);
		}
		List<User> deleteList = new ArrayList<User>(preList);
		deleteList.removeAll(changeList);
		deleteList.removeAll(unchangeList);
		BaseDAO dao = new BaseDAO();
		for (User item: changeList) {
			dao.saveOrUpdate(item);
		}
		for (User item: deleteList) {
			dao.remove(item);
		}
	}
	
	protected void runBefore() {
		this.getWindowMonitor();
	}
	
	public void runAfter() {
		if (this.isCommit()==true)
			LogUtil.info(new StringBuffer().append(SellerLogic.getSellerId()).append("更新功能点完成。。。。。。").toString());
		new SessionProvider().clear();
		WindowMonitor.getMonitor().close();
	}
	
	public boolean isRunnable() {
		return true;
	}
}

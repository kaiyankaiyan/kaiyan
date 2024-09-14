package com.haoyong.sales.common.listener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.types.DateType;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.query.SqlListBuilderSetting;
import net.sf.mily.webObject.query.SqlQuery;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.base.logic.UserPrivilegeLogic;
import com.haoyong.sales.common.derby.DerbyDAO;
import com.haoyong.sales.common.derby.User4d;
import com.haoyong.sales.common.derby.User4dLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.MainForm;
import com.haoyong.sales.common.form.MainForm.RightNode;
import com.haoyong.sales.sale.logic.StoreEnoughLogic;


public class RemindInstallorListener extends AttrRunnableListener {

	private static List<RightNode> rightList;
	
	@Override
	public void runTask() {
		if ("没有新数据变更就不用更新提醒".length()>0 && new StoreEnoughLogic().hasNewStoreEnough(this.getFromTime())==false)
			return ;
		StringBuffer sbSql = new StringBuffer();
		Iterator<Object> cntIter = null;
		Iterator<User4d> derbyIter = new User4dLogic().getUserList_2().iterator();
		List<RightNode> rightList = this.getRightList();
		List<User> userList = new UserPrivilegeLogic().getUsersOfRole(new UserLogic().getInstallRoleName());
		if (rightList.isEmpty() && derbyIter.hasNext()==false) {
			return ;
		} else if (rightList.isEmpty() || userList.isEmpty()) {
			cntIter = new ArrayList<Object>(0).iterator();
		} else {
			sbSql.append("select ");
			for (User user: new UserPrivilegeLogic().getUsersOfRole(new UserLogic().getInstallRoleName())) {
				for (RightNode item: rightList) {
					sbSql.append(this.sqlCountSource(item, user));
				}
			}
			cntIter = SessionProvider.getQueryExecutor().getQueryResult(sbSql.deleteCharAt(sbSql.length()-1).toString()).get(0).iterator();
		}
		StringBuffer sb = new StringBuffer("RemindInstallor").append(SellerLogic.getSellerId()).append("\n");
		DerbyDAO dao = new DerbyDAO();
		for (User user: new UserPrivilegeLogic().getUsersOfRole(new UserLogic().getInstallRoleName())) {
			for (RightNode rightItem : rightList) {
				int count = Integer.valueOf(cntIter.next().toString());
				if (count==0)
					continue;
				User4d derby = derbyIter.hasNext()? derbyIter.next(): new User4d();
				sb.append(derby.getId()).append("_").append(user.getUserName()).append(".").append(rightItem.getPathText()).append(" ").append(count).append("\n");
				derby.setLinkType(2);
				derby.setDeptName(user.getUserName());
				derby.setUserName(rightItem.getRight());
				derby.setRcount(count);
				if (derby.isChanged())
					dao.saveOrUpdate(derby);
			}
		}
		for (; derbyIter.hasNext();) {
			User4d derby = derbyIter.next();
			dao.remove(derby);
			sb.append(derby.getId()).append(" ").append(derby.getSellerId()).append("\n");
		}
		dao.getSession().flush();
//		DomainChangeTracing.getLog().info(sb.toString());
	}
	
	protected void runBefore() {
		this.getWindowMonitor();
	}
	
	protected void runAfter() {
		new SessionProvider().clear();
		if (this.getSessionName()!=null)
			WindowMonitor.getMonitor().close();
		if (this.isCommit()==true)
			LogUtil.info(new StringBuffer().append(SellerLogic.getSellerId()).append("更新工程领班待处理记录数量完成。。。。。。").append(this.getRightList().size()).toString());
	}
	
	private List<RightNode> getRightList() {
		if (rightList!=null)
			return rightList;
		List<String> rightNames = new UserPrivilegeLogic().getRoleLinkRightNames(new UserLogic().getInstallRoleName());
		List<RightNode> list = new ArrayList<RightNode>();
		MainForm form = new MainForm();
		for (RightNode node: form.getComponent().getInnerFormerList(RightNode.class)) {
			if (rightNames.contains(node.getRight())==false)
				continue;
			if (node.getSqlBuilder()==null)
				continue;
			if (node.getSqlBuilder().isMetaEmpty(ParameterName.Remind))
				continue;
			list.add(node);
		}
		this.rightList = list;
		return rightList;
	}
	
	public String sqlCountSource(RightNode node, User user) {
		SqlListBuilder sqlBuilder = node.getSqlBuilder();
		AbstractForm actionForm = node.getForm();
		WindowMonitor.getMonitor().addAttribute("user", user);
		SqlQuery sqlQuery = sqlBuilder.getSqlQuery();
		HashMap<String, String> paramList = new HashMap<String, String>();
		String getter = sqlBuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Parameter);
		if (StringUtils.isNotEmpty(getter))
			paramList = (HashMap<String, String>)ReflectHelper.invokeMethod(actionForm, getter, new Object[0]);
		sqlQuery.setPageSize(sqlBuilder.getParameter(ParameterName.Cfg).getInteger(ParameterName.Page_Size,20));
		sqlQuery.getFields().setParamList(paramList);
		SqlListBuilderSetting setting = actionForm.getSearchSetting(sqlBuilder);
		Map<String, String> remindFilter = new HashMap<String, String>();
		remindFilter.put(sqlBuilder.getAttribute(ParameterName.Remind, ParameterName.ID), sqlBuilder.getAttribute(ParameterName.Remind, ParameterName.Value));
		setting.addFilters(remindFilter);
		sqlQuery.setSetting(setting);
		StringBuffer sb = new StringBuffer().append("\n(").append(sqlQuery.getFields().sqlCountSource()).append("),");
		actionForm.getSqlListBuilderSetting(sqlBuilder).removeFilters(remindFilter.keySet().toArray(new String[0]));
		return sb.toString();
	}

	@Override
	public boolean isRunnable() {
		// 08:00——20:00才处理
		Calendar date = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		try {
			DateType dtype = new DateType();
			calendar.setTime(dtype.parse(dtype.format(date.getTime())));
		}catch(Exception e) {
		}
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		if (date.before(calendar)) {
			return false;
		}
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		if (date.after(calendar)) {
			return false;
		}
		if ("Session过期没空处理".length()>0 && this.getSessionName()!=null && WindowMonitor.getMonitorBySessionId(this.getSessionName())==null)
			return false;
		return true;
	}
	
	private Date getFromTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -10);
		return calendar.getTime();
	}
}

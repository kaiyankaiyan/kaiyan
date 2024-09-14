package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.SqlListBuilderSetting;
import net.sf.mily.webObject.query.SqlQuery;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.UserForm;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.MainForm;
import com.haoyong.sales.common.form.MainForm.RightNode;
import com.haoyong.sales.common.listener.AttrRunnableListener;
import com.haoyong.sales.sale.domain.OrderDetail;

public class GlobalSearchForm extends AbstractForm<User> {
	
	private void doSearch() throws Exception {
		if ("保存搜索条件".length()>0) {
			SellerViewInputs inputs = this.getAttr(SellerViewInputs.class);
			inputs.getInputs().clear();
			String s = null;
			if (StringUtils.isBlank(s=this.getOrderDetail().getMonthnum())==false)
				inputs.getInputs().put("monthnum", s);
			if (StringUtils.isBlank(s=this.getOrderDetail().getOrderTicket().getNumber())==false)
				inputs.getInputs().put("number", s);
			new SellerViewInputsLogic().saveOrUpdate(inputs);
		}
		this.getSearchCountListener().run();
		List<RightNode> list = (List<RightNode>)this.getSearchCountListener().inputs.getVoParamMap().get("FoundList");
		this.getSearchRightList().clear();
		if (list!=null)
			this.getSearchRightList().addAll(list);
	}
	
	private void loadSearch(Component fcomp) {
		EditView view = fcomp.searchFormerByClass(EditView.class);
		String builderName = view.getViewBuilder().getFullViewName();
		SellerViewInputs inputs = new SellerViewInputsLogic().get(builderName, this.getUserName());
		if (inputs == null) {
			inputs = new SellerViewInputs();
			inputs.setBuilderName(builderName);
			inputs.setUserName(this.getUserName());
		}
		this.setAttr(inputs);
		TicketPropertyUtil.copyFieldsSkip(new OrderDetail(), this.getOrderDetail());
		this.getOrderDetail().setMonthnum(inputs.getInputs().get("monthnum"));
		this.getOrderDetail().getOrderTicket().setNumber(inputs.getInputs().get("number"));
	}
	
	public SellerViewInputs getDomain() {
		String builderName = "GlobalSearchForm.Search";
		SellerViewInputs inputs = new SellerViewInputsLogic().get(builderName, this.getUserName());
		if (inputs == null)
			inputs = new SellerViewInputs();
		return inputs;
	}
	
	public OrderDetail getOrderDetail() {
		String k = "FormOrderDetail";
		OrderDetail order = this.getAttr(k);
		if (order == null) {
			order = new OrderDetail();
			this.setAttr(k, order);
		}
		return order;
	}
	
	private SellerViewInputs getSellerViewInputs() {
		return this.getAttr(SellerViewInputs.class);
	}

	@Override
	public void setSelectedList(List<User> selected) {
	}
	
	private SearchCountListener getSearchCountListener() {
		SearchCountListener listener = this.getAttr(SearchCountListener.class);
		if (listener == null) {
			listener = new SearchCountListener(this.getSellerViewInputs());
			listener.setManual(true);
			WindowMonitor.getMonitor().addAttribute("seller", this.getSeller());
			User u = new User();
			u.setUserId("admin");
			u.setUserName("管理员");
			WindowMonitor.getMonitor().addAttribute("user", u);
			this.setAttr(listener);
		}
		listener.inputs = this.getSellerViewInputs();
		return listener;
	}
	
	public List<RightNode> getSearchRightList() {
		String k = "SearchRightList";
		List<RightNode> list = (List<RightNode>)this.getAttr(k);
		if (list == null) {
			list = new ArrayList<RightNode>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private UserForm getUserForm() {
		UserForm form = getAttr(UserForm.class);
		if (form == null) {
			form = new UserForm();
			setAttr(form);
		}
		form.prepareChangePassword();
		return form;
	}
	
	private void setGlobalShared() {
		WindowMonitor.getMonitor().addAttribute("SearchRightList", this.getSearchRightList());
	}
	
	private static class SearchCountListener extends AttrRunnableListener {

		private ConcurrentLinkedQueue<RightNode> rightList = new ConcurrentLinkedQueue<RightNode>();
		private SellerViewInputs inputs;

		private SearchCountListener(SellerViewInputs inputs) {
			this.inputs = inputs;
		}
		
		public void runTask() {
			StringBuffer sbSql = new StringBuffer("select ");
			for (RightNode item: this.getRightList())
				sbSql.append(this.sqlCountSource(item));
			sbSql.deleteCharAt(sbSql.length() - 1);
			Iterator<Object> cntIter = SessionProvider.getQueryExecutor().getQueryResult(sbSql.toString()).get(0).iterator();
			List<RightNode> countList = new ArrayList<RightNode>();
			StringBuffer sb = new StringBuffer();
			for (RightNode rightItem : rightList) {
				int count = Integer.valueOf(cntIter.next().toString());
				rightItem.setSearchCount(count);
				if (count>0) {
					countList.add(rightItem);
					sb.append(rightItem.getPathText()).append(",").append(rightItem.getRight()).append(count).append("\n");
				}
			}
//			if (sb.length()>0)			LogUtil.error(sb.insert(0, "全局搜索结果").toString());
			this.inputs.getVoParamMap().put("FoundList", countList);
			this.inputs.getVoParamMap().put("FoundString", sb.toString());
		}
		
		protected void runBefore() {
		}
		
		public void runAfter() {
			new SessionProvider().clear();
		}
		
		private ConcurrentLinkedQueue<RightNode> getRightList() {
			synchronized (rightList) {
				if (rightList.size()>0)
					return rightList;
				List<RightNode> list = new ArrayList<RightNode>();
				MainForm form = new MainForm();
				for (RightNode node: form.getComponent().getInnerFormerList(RightNode.class)) {
					if (node.getSqlBuilder()==null)
						continue;
					if (node.getSqlBuilder().isMetaEmpty(ParameterName.Select))
						continue;
					list.add(node);
				}
				rightList.addAll(list);
			}
			return rightList;
		}
		
		public String sqlCountSource(RightNode node) {
			ViewBuilder viewBuilder = node.getViewBuilder();
			if (StringUtils.equals(viewBuilder.getLabel(), "收货开单"))
				"".toCharArray();
			SqlListBuilder sqlBuilder = node.getSqlBuilder();
			AbstractForm actionForm = node.getForm();
			SqlQuery sqlQuery = sqlBuilder.getSqlQuery();
			HashMap<String, String> paramList = new HashMap<String, String>();
			String getter = sqlBuilder.getParameter(ParameterName.Cfg).getString(ParameterName.Parameter);
			if (StringUtils.isNotEmpty(getter)) {
				paramList = (HashMap<String, String>)ReflectHelper.invokeMethod(actionForm, getter, new Object[0]);
			}
			sqlQuery.setPageSize(sqlBuilder.getParameter(ParameterName.Cfg).getInteger(ParameterName.Page_Size,20));
			sqlQuery.getFields().setParamList(paramList);
			SqlListBuilderSetting setting = actionForm.getSearchSetting(sqlBuilder);
			Map<String, String> remindFilter = new HashMap<String, String>();
			for (Map.Entry<String, String> entry: this.inputs.getInputs().entrySet()) {
				String k=entry.getKey(), v=entry.getValue();
				if (StringUtils.isBlank(v)==false && sqlQuery.getFieldById(k)!=null)
					remindFilter.put(k, v);
			}
			if (remindFilter.keySet().size()==0)
				return "0,";
			String sid=sqlBuilder.getAttribute(ParameterName.Select, ParameterName.ID);
			String svalue=sqlBuilder.getAttribute(ParameterName.Select, ParameterName.Value);
			String mid=sqlBuilder.getAttribute(ParameterName.Remind, ParameterName.ID);
			String mvalue=sqlBuilder.getAttribute(ParameterName.Remind, ParameterName.Value);
			remindFilter.put(sid, svalue);
			if (StringUtils.equals(sid, "id") && StringUtils.equals(mid, "id")==false && mid!=null)
				remindFilter.put(mid, mvalue);
			setting.addFilters(remindFilter);
			sqlQuery.setSetting(setting);
			StringBuffer sb = new StringBuffer().append("\n(").append(sqlQuery.getFields().sqlCountSource()).append("),");
			actionForm.getSqlListBuilderSetting(sqlBuilder).removeFilters(remindFilter.keySet().toArray(new String[0]));
			return sb.toString();
		}

		@Override
		public boolean isRunnable() {
			return true;
		}
	}
}

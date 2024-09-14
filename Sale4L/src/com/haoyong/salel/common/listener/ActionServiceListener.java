package com.haoyong.salel.common.listener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.mily.bean.ListDynaBean;
import net.sf.mily.bus.service.ServiceChannel;
import net.sf.mily.mappings.MetaParameter;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.ui.event.Confirmable;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.ButtonMeta;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.FieldBuilderHelper;
import net.sf.mily.webObject.event.ActionChain;
import net.sf.mily.webObject.event.FieldActionEvent;
import net.sf.mily.webObject.event.FieldActionListener;

import org.apache.commons.lang.StringUtils;
import org.josql.Query;
import org.josql.QueryResults;
import org.junit.Assert;

import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.dao.DomainChangeTracing;
import com.haoyong.salel.common.dao.TransactionManager;
import com.haoyong.salel.common.domain.AbstractDomain;
import com.haoyong.salel.common.domain.State;
import com.haoyong.salel.common.form.AbstractForm;
import com.haoyong.salel.common.form.ActionEnum;
import com.haoyong.salel.common.form.ViewData;
import com.haoyong.salel.util.SLoginUtil;

/**
 * 业务服务类 Listener
 *
 */
public class ActionServiceListener extends TransRunnableListener implements FieldActionListener, Confirmable {
	
	protected MetaParameter param;
	private Component eventSource;
	private List<ViewData> afterList;
	private StringBuffer output;


	public void actionPerformed(FieldActionEvent event, ActionChain chain) {
		this.eventSource = event.getEvent().getSource();
		this.afterList = new ArrayList<ViewData>();
		if (FieldBuilderHelper.isExpressionOk(event.getField(), param)==Boolean.FALSE)
			return;
		this.setManual(true);
		this.run();
	}
	
	public void runTask() {
		EditView firer = eventSource.searchFormerByClass(EditView.class);
		Iterator<MetaParameter> iter=param.getParameterIterator(ParameterName.Action);
		BaseDAO dao = new BaseDAO();
		this.output=new StringBuffer();
		if (iter.hasNext()==false) {
			StringBuffer sitem=new StringBuffer(), smain=new StringBuffer();
			AbstractForm formable = getFormable(eventSource, param.getString(ParameterName.Former));
			ViewData viewData = new ViewData();
			viewData.setCurrentUser(((AbstractForm)formable).getUser());
			MetaParameter pitem = getParameter();
			runAction(pitem, formable, viewData);
			setState(getParameter(), firer, formable, viewData);
			setUser(getParameter(), formable, viewData);
			smain.append(viewData.getCurrentUser().getUserName()).append(new SimpleDateFormat("MM/ddHH:mm").format(new Date())).append("，").append(param.getString(ParameterName.Message, ""));
			sitem.append(dao.getDomainChangeTracing().print2clear("", smain.toString()));
			output.append(sitem);
		} else {
			for (; iter.hasNext();) {
				StringBuffer sitem=new StringBuffer(), smain=new StringBuffer();
				MetaParameter pitem = iter.next();
				if (FieldBuilderHelper.isExpressionOk(firer, pitem)==Boolean.FALSE)			continue;
				AbstractForm formable = getFormable(eventSource, pitem.getString(ParameterName.Former)==null? param.getString(ParameterName.Former): pitem.getString(ParameterName.Former));
				ViewData viewData = new ViewData();
				viewData.setCurrentUser(((AbstractForm)formable).getUser());
				runAction(pitem, formable,viewData);
				setState(pitem, firer, formable, viewData);
				setUser(pitem, formable, viewData);
				smain.append(viewData.getCurrentUser().getUserName()).append(new SimpleDateFormat("MM/ddHH:mm").format(new Date())).append("，").append(param.getString(ParameterName.Message, ""));
				sitem.append(new BaseDAO().getDomainChangeTracing().print2clear(pitem.getString(ParameterName.Message, ""), smain.toString()));
				output.append(sitem);
			}
		}
	}
	
	/**
	 * 跑服务Action
	 */
	private void runAction(MetaParameter param, Object form, ViewData<? extends AbstractDomain> viewData){
// Before
		for (String sBefore=param.getParameter(ParameterName.Before).getString(ParameterName.Method); sBefore!=null; sBefore=null)
			ReflectHelper.invokeMethod(form, sBefore, new Object[0]);
		int cnt=0;
		RuntimeException error = null;
// Property
		try {
			for (String sProperty=param.getString(ParameterName.Property); sProperty!=null && cnt==0; sProperty=null,cnt++)
				viewData.setTicketDetails(ListDynaBean.asList(ReflectHelper.getPropertyValue(form, sProperty)));
			for (String sProperty=param.getParameter(ParameterName.Property).getString(ParameterName.Property), sParam=param.getParameter(ParameterName.Property).getString(ParameterName.Parameter); sProperty!=null && cnt==0; sProperty=null,cnt++) {
				List<Object> list = new ArrayList<Object>();
				Assert.assertTrue("要指定property.parameter参数", sParam!=null);
				for (Object row: ListDynaBean.asList(ReflectHelper.getPropertyValue(form, sProperty))) {
					Object r = ReflectHelper.getPropertyValue(row, "voParamMap.".concat(sParam));
					if (r!=null)
						list.add(r);
				}
				viewData.setTicketDetails(ListDynaBean.asList(list));
			}
		} catch(RuntimeException e2) {
			error = e2;
		}
// Action
		String setter = param.getString(ParameterName.Setter);
		if (StringUtils.isNotEmpty(setter) && cnt++==0)
			ReflectHelper.invokeMethod(form, setter, viewData);
		if (cnt==0)
			Assert.fail("Action 没有配置 Setter!");
		String select = param.getString(ParameterName.Select);
		if (select!=null && "查符合条件的记录".length()>0 && viewData.getTicketDetails().size()>0) {
			ViewData<AbstractDomain> view1 = new ViewData<AbstractDomain>();
			view1.setTicketDetails(this.getSelectList(form, viewData.getTicketDetails(), select));
			viewData = view1;
		}
		try {
			String actionName=param.getString(ParameterName.Action);
			Assert.assertTrue("没有配置action", actionName!=null);
			for (String actionItem: actionName.split("\\,")) {
				ActionEnum action = null;
				try {
					action = ActionEnum.valueOf(actionItem);
				} catch(Exception e1) {
					throw new LogicException(2, new StringBuffer().append("找不到此Action").append(action).toString());
				}
				runServiceAction(action, viewData);
			}
		} catch(RuntimeException e2) {
			error = e2;
		}
// After
		String sAfter=param.getParameter(ParameterName.After).getString(ParameterName.Method);
		if (sAfter!=null)
			ReflectHelper.invokeMethod(form, sAfter, new Object[]{error==null});
		if (error!=null)
			throw error;
	}
	
	private void runServiceAction(ActionEnum action, ViewData viewData) {
		BaseDAO dao = new BaseDAO();
		dao.getDomainChangeTracing().getActonList().add(action);
		ServiceChannel channel = SLoginUtil.getBus().open(action);
		channel.run(new Object[]{viewData});
		if (channel.hasAfter()) {
			viewData.setParam(channel);
			afterList.add(viewData);
		} else {
			channel.close();
		}
	}
	
	protected void runAfter() {
		if (true) {
			List<EditView> editViews = eventSource.searchFormerLinkByClass(EditView.class);
			String menu = eventSource.searchFormerByClass(ButtonMeta.class).getText();
			String viewName = editViews.get(editViews.size()-1).getViewBuilder().getFullViewName();
			AbstractForm formable = getFormable(eventSource, param.getString(ParameterName.Former));
			output.insert(0, new StringBuffer().append(viewName).append(menu).append("，")
					.append(((AbstractForm)formable).getUser().getUserName()).append(TransactionManager.isRealCommit() && this.isCommit()? "提交": "回滚")
					.append("\n------").append(param.getString(ParameterName.Message, "")).toString());
			DomainChangeTracing.getLog().info(output.append("\n").toString());
		}
		if (this.isCommit()) {
			for (ViewData viewData: afterList) {
				ServiceChannel channel = (ServiceChannel)viewData.getParam(ServiceChannel.class);
				channel.runAftertrans(new Object[]{viewData});
				channel.close();
			}
		}
	}
	
	private void setState(MetaParameter param, Field firer, Object bean, ViewData<? extends AbstractDomain> viewData0) {
		for (Iterator<MetaParameter> iter=param.getParameterIterator(ParameterName.State); iter.hasNext();) {
			MetaParameter parameter = iter.next();
			int stateId = parameter.getInteger(ParameterName.ID, -1);
			if (Boolean.FALSE==FieldBuilderHelper.isExpressionOk(firer, parameter))
				continue;
			String stateName = parameter.getString(ParameterName.Name);
			State state = State.New();
			state.setId(stateId);
			state.setName(stateName);
			ViewData<? extends AbstractDomain> viewData = viewData0;
			String pname = parameter.getString(ParameterName.Property);
			if (pname != null) {
				List<AbstractDomain> ticketDetails = (List)ReflectHelper.getPropertyValue(viewData, "serviceParam.".concat(pname));
				if (ticketDetails==null)
					ticketDetails = (List)ReflectHelper.getPropertyValue(bean, "attrMap.".concat(pname));
				ViewData<AbstractDomain> view1 = new ViewData<AbstractDomain>();
				view1.setTicketDetails(ListDynaBean.asList(ticketDetails));
				viewData = view1;
			}
			String select = parameter.getString(ParameterName.Select);
			if (select!=null && "查符合条件的记录".length()>0 && viewData.getTicketDetails().size()>0) {
				ViewData<AbstractDomain> view1 = new ViewData<AbstractDomain>();
				view1.setTicketDetails(this.getSelectList(bean, viewData.getTicketDetails(), select));
				viewData = view1;
			}
			String method = parameter.getString(ParameterName.Method);
			if (method != null)
				ReflectHelper.invokeMethod(bean, method, new Object[]{state, viewData});
		}
	}
	
	private void setUser(MetaParameter param, Object bean, ViewData<? extends AbstractDomain> viewData0) {
		for (Iterator<MetaParameter> iter=param.getParameterIterator(ParameterName.User); iter.hasNext();) {
			MetaParameter parameter = iter.next();
			String select = parameter.getString(ParameterName.Select);
			ViewData<? extends AbstractDomain> viewData = viewData0;
			if (select!=null && "查符合条件的记录".length()>0 && viewData.getTicketDetails().size()>0) {
				ViewData<AbstractDomain> view1 = new ViewData<AbstractDomain>();
				view1.setTicketDetails(this.getSelectList(bean, viewData.getTicketDetails(), select));
				viewData = view1;
			}
			String method = parameter.getString(ParameterName.Method);
			ReflectHelper.invokeMethod(bean, method, new Object[]{viewData});
		}
	}
	
	private List getSelectList(Object form, List details, String expression) {
		Class entityBean = details.get(0).getClass();
		String tsql = null; 
		if ("处理参数".length()>0) {
			StringBuffer sb = new StringBuffer();
			sb.append("select * from ").append(entityBean.getName()).append(" where ").append(expression);
			String fsql = sb.toString();
			Matcher m = Pattern.compile("\\{\\$.+.\\}").matcher(fsql);
			StringBuffer paramSql = new StringBuffer();
			for (int msize=0, si=0, slen=fsql.length(); si<slen; si++) {
				if (m.find()) {
					msize++;
					String sitem = fsql.substring(m.start(), m.end());
					Object param = ReflectHelper.getPropertyValue(form, sitem.substring(2, sitem.length()-1));
					paramSql.append(fsql.substring(si, m.start()));
					if (param != null) {
						paramSql.append(TypeFactory.createType(param.getClass()).sqlFormat(param));
					} else {
						paramSql.append("=null");
					}
					si = m.end()-1;
				} else {
					paramSql.append(fsql.substring(si, fsql.length()));
					si = fsql.length()-1;
				}
			}
			tsql = paramSql.toString();
		}
		List list = null;
		try {
			Query q = new Query();
			q.parse(tsql);
			QueryResults queryResults = q.execute(details);
			list = queryResults.getResults();
			LogUtil.info(tsql);
			return list;
		} catch(Exception e) {
 			String error = new StringBuffer("the expression can not execute!\n").append(entityBean.getSimpleName()).append(".").append(expression).toString();
			throw new RuntimeException(error);
		}
	}
	
	private AbstractForm getFormable(Component fcomp, String sformer) {
		for (Iterator<EditView> iter=fcomp.searchFormerLinkByClass(EditView.class).iterator(); iter.hasNext();) {
			Object bean = iter.next().getValue();
			if (bean==null)		continue;
			if (sformer==null && bean instanceof AbstractForm)
				return (AbstractForm)bean;
			else if (sformer==null)
				continue;
			try {
				bean = ReflectHelper.getPropertyValue(bean, sformer);
				return (AbstractForm)bean;
			}catch(Exception e) {
			}
		}
		return null;
	}
	
	public String getConfirmMessage() {
		return param.getString(ParameterName.Message);
	}
	
	public MetaParameter getParameter() {
		return param;
	}

	public void setParameter(MetaParameter parameter) {
		this.param = parameter;
	}
	
	public boolean isRunnable() {
		return true;
	}
	
	public boolean isTransaction() {
		String stype = this.getParameter().getString(ParameterName.Type);
		if ("UnTransaction".equals(stype))
			return false;
		return true;
	}
}
package com.haoyong.salel.common.form;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.mily.common.AbstractAction;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.form.Formable;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.CheckBox;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Menu;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.enumeration.FileName;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.SqlListBuilderSetting;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.dao.TransactionManager;
import com.haoyong.salel.common.domain.AbstractDomain;
import com.haoyong.salel.common.logic.SqlListBuilderSettingLogic;

public abstract class AbstractForm<T extends AbstractDomain> extends AbstractAction implements Formable<T>{
	
	/**
	 * 登陆用户
	 */
	private User user;

	/**
	 * 警告信息
	 */
	protected String warnMsg;
	
	private Map<String, Object> attrMap = new HashMap<String, Object>();
	
	public String getActionMark() {
		try {
			return new StringBuffer().append(this.getUserName()).append(".").append(this.getClass().getSimpleName()).toString();
		}catch(Exception e) {
		}
		return null;
	}
	
	protected void beforeWindow(Window window) {
		window.addCSS(FileName.TABLE_CSS);
		window.addCSS(FileName.MENU_CSS);
	}
	
	protected void beforeBuild(IEditViewBuilder builder0) {
		ViewBuilder builder = (ViewBuilder)builder0;
		for (boolean one=builder.getParameter(ParameterName.Cfg).getString(ParameterName.Before)!=null; one; one=false) {
			String smethod = builder.getParameter(ParameterName.Cfg).getString(ParameterName.Before);
			ReflectHelper.invokeMethod(this, smethod, new Class[]{IEditViewBuilder.class}, new Object[]{builder});
		}
	}
	
	protected void beforeView(EditView view) {
		ViewBuilder builder = view.getViewBuilder();
		for (String menuname=builder.getParameter(ParameterName.Cfg).getString(ParameterName.Menu); menuname!=null; menuname=null) {
			for (Menu menu: view.getComponent().getInnerComponentList(Menu.class)) {
				if (StringUtils.equals(menu.getText(), menuname)) {
					menu.getEventListenerList().fireListener();
					return;
				}
			}
			Assert.fail("找不到按钮".concat(menuname));
		}
	}
	
	protected boolean getSqlListSearch(Window window, AbstractForm form, String viewName, int state_0_1, String... filterList) {
		ViewBuilder viewBuilder = EntityClass.loadViewBuilder(form.getClass(), viewName);
		SqlListBuilder sqlBuilder = (SqlListBuilder)viewBuilder.getFieldBuildersDeep(SqlListBuilder.class).get(0);
		SqlListBuilderSetting childSetting = form.getSearchSetting(sqlBuilder);
		HashMap<String, String> filters = new HashMap<String, String>();
		for (Iterator<String> iter=Arrays.asList(filterList).iterator(); iter.hasNext();)
			filters.put(iter.next(), iter.next());
		childSetting.addFilters(filters);
		Component viewComp = viewBuilder.build(form).getComponent();
		window.add(viewComp);
		ListView listview = viewComp.getInnerFormerList(ListView.class).get(0);
		List<List<Object>> rows = (List<List<Object>>)listview.getValue();
		boolean ok = true;
		RuntimeException error = null;
		if ((state_0_1 & 1)==1 && rows.size()==0) {// 找到0个
			form.getSearchSetting(sqlBuilder);
		} else if ((state_0_1 & 2)==2 && rows.size()==1) {// 找到1个
			List<BlockCell> cellList = listview.getComponent().getInnerComponentList("ListViewCellSelect");
			cellList.get(0).getInnerComponentList(CheckBox.class).get(0).setSelected(true);
			listview.getListBuilder().pickValue(listview.getComponent());
			form.getSearchSetting(sqlBuilder);
			try {
				viewComp.getInnerComponentList(Menu.class).get(0).getEventListenerList().fireListener();
			} catch(Exception e) {
				error = LogUtil.getRuntimeException(e);
			}
		} else {
			ok = false;
		}
		window.remove(viewComp);
		window.getSubComponents().clear();
		if (error != null)
			throw error;
		return ok;
	}
	
	public User getUser() {
		if (user == null) {
			user = (User)getAttribute("user");
		}
		return user;
	}
	
	/**用户过滤条件,即登录填写选项(分公司/生产部门/领班)*/
	@SuppressWarnings("unchecked")
	public Map<String,String> getUserFilters() {
		Map<String, String> userFilters=(Map<String, String>) getAttribute("UserFilters");
		if(userFilters==null){
			userFilters=new HashMap<String, String>();
		}
		return userFilters;
	}
	
	
	public String getUserName() {
		return getUser().getUserName();
	}
	
	public Date getDate() {
		return new Date();
	}

	public String getWarnMsg() {
		return warnMsg;
	}

	public void setWarnMsg(String warnMsg) {
		this.warnMsg = warnMsg;
	}
	
	protected <F extends Object> F getAttr(Class<F> key) {
		return (F)this.getAttr(key.getSimpleName());
	}
	
	protected <F extends Object> void setAttr(F value) {
		String key = value.getClass().getSimpleName();
		this.setAttr(key, value);
	}
	
	protected <F extends Object> F getAttr(String key) {
		return (F)this.attrMap.get(key);
	}
	
	protected <F extends Object> void setAttr(String key, F value) {
		this.attrMap.put(key, value);
	}
	
	public SqlListBuilderSetting getSqlListBuilderSetting(SqlListBuilder builder) {
		SqlListBuilderSetting st = super.getSqlListBuilderSetting(builder);
		//添加登录填写的过滤条件
		st.addFilters(getUserFilters());
		return st;
	}
	
	protected SqlListBuilderSetting getSearchSetting(SqlListBuilder builder) {
		SqlListBuilderSetting st = super.getSqlListBuilderSetting(builder);
		st.clearFilters();
		st.addFilters(getUserFilters());
		return st;
	}
	
	protected SqlListBuilderSetting genSqlListBuilderSetting() {
		SqlListBuilderSetting st = new SqlListBuilderSetting();
		st.setUserName(this.getUser().getUserId());
		return st;
	}
	
	public ConcurrentLinkedQueue<SqlListBuilderSetting> loadSqlListBuilderSettings() {
		return new SqlListBuilderSettingLogic().getSettingsByUser(getUser());
	}
	
	public void saveSqlListBuilderSetting(SqlListBuilderSetting st) {
		//移除登录填写的过滤条件
		Map<String, String> userFilters = getUserFilters();
		for (Entry<String, String> entry : userFilters.entrySet()) {
			st.getColumnSettings().remove(entry.getKey());//移除
			break;
		}
		//保存
		if (!st.isChanged())			return;
		st.savePrepare();
		TransactionManager.begin();
		String mode = null;
		if (st.getColumnSettings().size()>0 || StringUtils.isNotEmpty(st.getColumns())) {
			mode = "saveOrUpdate";
			BaseDAO.getSessionInThread().saveOrUpdate(st);
		} else if (st.getId()>0) {
			mode = "delete";
			BaseDAO.getSessionInThread().delete(st);
			st.setId(0);
		}
		if (StringUtils.isEmpty(mode)==false) {
			StringBuffer sb = new StringBuffer().append("=========").append(mode).append(" SqlListBuilderSetting ").append(st.getGson());
			LogUtil.info(sb.toString());
		}
		TransactionManager.commit();
		//重新添加回登录填写的过滤条件
		st.addFilters(userFilters);
	}
}

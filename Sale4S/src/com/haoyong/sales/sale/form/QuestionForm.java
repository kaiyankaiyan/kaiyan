package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.query.SqlListBuilderSetting;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.UserForm;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.SelectDomainListener;
import com.haoyong.sales.sale.domain.Question;
import com.haoyong.sales.sale.domain.TicketUser;

public class QuestionForm extends AbstractForm<Question> {

	private void beforeQuestionList(IEditViewBuilder buuilder0) {
		if (this.getQuestion().getStQuestion()==0)
			getQuestion().setStQuestion(15);
	}
	
	private void canEdit(List<List<Object>> valiRows) {
		// stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if ((Integer)row.get(0)>=20)
				sb.append("已解决的提问不能编辑，");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}

	private void canReply(List<List<Object>> valiRows) {
		// stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if ((Integer)row.get(0)>=20)
				sb.append("已解决的提问不用处理，");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}

	private void canClose(List<List<Object>> valiRows) {
		// stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if ((Integer)row.get(0)==30)
				sb.append("提问已经关闭，");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}

	private void canOpen(List<List<Object>> valiRows) {
		// stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if ((Integer)row.get(0)<20)
				sb.append("提问已经开启，");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	private void prepareQuestion(){
		setDomain(new Question());
		this.getDomain().setQuestioner(getUserName());
	}
	
	private void prepareView(){
		this.setDomain(this.getSelectFormer4Question().getFirst());
	}
	
	private void prepareEdit(){
		this.setDomain(this.getSelectFormer4Question().getFirst());
	}
	
	private void prepareReply(){
		this.setDomain(this.getSelectFormer4Question().getFirst());
	}
	
	private void validateQuestion() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.getDomain().getRightName()))
			sb.append("请填写功能名称，");
		if (StringUtils.isBlank(this.getDomain().getQuestioner()))
			sb.append("请填写提出人，");
		if (StringUtils.isBlank(this.getDomain().getTitle()))
			sb.append("请填写提问描述，");
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateReply() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.getDomain().getReply()))
			sb.append("请填写回复内容，");
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void setQuestionState(State state, ViewData<Question> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (Question d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStQuestion(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private void setQuestionUser(ViewData<Question> viewData) {
		String suser = genTicketUser().getUserDate();
		for (Question d: viewData.getTicketDetails()) {
			d.setQuestioner(suser);
		}
	}
	
	private void setReplyUser(ViewData<Question> viewData) {
		String suser = genTicketUser().getUserDate();
		for (Question d: viewData.getTicketDetails()) {
			d.setReplier(suser);
		}
	}
	
	private void setQuestion4Service(ViewData<Question> viewData) {
		viewData.setTicketDetails(this.getDomain());
	}
	
	private void setQuestionL4Service(ViewData<Question> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Question().getSelectedList());
	}
	
	private SelectTicketFormer4Sql<QuestionForm, Question> getSelectFormer4Question() {
		String k = "SelectFormer4Question";
		SelectTicketFormer4Sql<QuestionForm, Question> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<QuestionForm, Question>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Sql<QuestionForm, User> getSelectFormer4User() {
		String k = "SelectFormer4User";
		SelectTicketFormer4Sql<QuestionForm, User> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<QuestionForm, User>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public Question getDomain() {
		String k="Domain";
		Question d = this.getAttr(k);
		if (d == null) {
			d = new Question();
			this.setAttr(k, d);
		}
		return d;
	}

	private void setDomain(Question domain) {
		this.setAttr("Domain", domain);
	}
	
	private HashMap<String, String> getParam4Question() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("stateId", this.getQuestion().getStQuestion()+"");
		return map;
	}
	
	private List<Question> getSelectedList() {
		String k = "SelectedList";
		List<Question> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<Question>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<Question> getDetailList() {
		String k = "DetailList";
		List<Question> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<Question>();
			this.setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<Question> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	private Question getQuestion() {
		Question q = this.getAttr(Question.class);
		if (q==null) {
			q = new Question();
			this.setAttr(q);
		}
		return q;
	}
	
	private List<Map.Entry<Integer, String>> getStateOptions(Object form) {
		LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
		map.put(15, "提出");
		map.put(20, "提出&解决");
		map.put(30, "已关闭");
		return new ArrayList<Map.Entry<Integer,String>>(map.entrySet());
	}
	
	private UserForm getUserForm() {
		UserForm form = this.getAttr(UserForm.class);
		if (form == null) {
			form = new UserForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private List<User> getUserSearch(String name) {
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			SqlListBuilder sqlBuilder = (SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "UserQuery").getFieldBuildersDeep(SqlListBuilder.class).get(0);
			HashMap<String, String> filters = new HashMap<String, String>();
			filters.put("userName", name);
			SqlListBuilderSetting childSetting = getSearchSetting(sqlBuilder);
			childSetting.addFilters(filters);
			sqlBuilder.getSqlQuery().getFields().asSetting(childSetting);
			List<List<Object>> rows = sqlBuilder.getSqlQuery().loadResultAll(sqlBuilder.getSqlQuery().getFields().sqlSelectSource());
			if(rows.size()==1){//找到一条
				getSearchSetting(sqlBuilder);
				List<User> commList = new ArrayList<User>((List)new SelectDomainListener().toDomains(rows, User.class));
				return commList;
			}
		}
		this.setIsDialogOpen(true);
		return new ArrayList<User>(0);
	}
	
	private void setUserSelect4Questioner(List<User> userList) {
		if (userList.size()>0) {
			User user = userList.get(0);
			this.getDomain().setQuestioner(user.getUserName());
		} else {
			this.getDomain().setQuestioner(null);
		}
	}
	
	private void getRightSearchName(TextField input) {
		String name = input.getText();
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "RightQuery", 1|2, "userName", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	private void setRightSelect(List<User> userList) {
		if (userList.size()>0) {
			User user = userList.get(0);
			this.getDomain().setRightName(user.getUserName());
		} else {
			this.getDomain().setRightName(null);
		}
	}
	
	public boolean getIsDialogOpen() {
		String k = "IsDialogOpen";
		Boolean ok = this.getAttr(k);
		if (ok == null) {
			ok = Boolean.FALSE;
			this.setAttr(k, ok);
		}
		return ok;
	}
	
	private void setIsDialogOpen(boolean open) {
		String k = "IsDialogOpen";
		this.setAttr(k, open);
	}
}

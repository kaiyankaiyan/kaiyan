package com.haoyong.salel.base.service;

import java.util.List;

import net.sf.mily.bus.annotation.ActionService;
import net.sf.mily.support.tools.DesUtil;

import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.form.ActionEnum;
import com.haoyong.salel.common.form.MatchActions;
import com.haoyong.salel.common.form.ViewData;

/**
 * 业务服务类——用户
 *
 */
@ActionService
public class UserService {
	
	//待改进
	@MatchActions({ActionEnum.User_Save})
	public void save(ViewData<User> viewData) {
		BaseDAO dao = new BaseDAO();
		for (User domain: viewData.getTicketDetails()) {
			domain.setPassword(new DesUtil().getEncrypt(domain.getPassword()));
			dao.saveOrUpdate(domain);
		}
	}

	@MatchActions({ActionEnum.User_Delete})
	public void delete(ViewData<User> viewData) {
		BaseDAO dao = new BaseDAO();
		for (User domain: viewData.getTicketDetails()) {
			dao.remove(domain);
		}
	}

	@MatchActions({ActionEnum.User_RolePrivilege})
	public void rolePrivilege(ViewData<User> viewData) {
		BaseDAO dao = new BaseDAO();
		for (User link: viewData.getTicketDetails()) {
			dao.saveOrUpdate(link);
		}
		for (User link: (List<User>)viewData.getParam("DeleteList")) {
			dao.remove(link);
		}
	}

	@MatchActions({ActionEnum.User_RoleActor})
	public void roleActor(ViewData<User> viewData) {
		BaseDAO dao = new BaseDAO();
		for (User link: viewData.getTicketDetails()) {
			dao.saveOrUpdate(link);
		}
		for (User link: (List<User>)viewData.getParam("DeleteList")) {
			dao.remove(link);
		}
	}
}

package com.haoyong.sales.base.service;

import java.util.List;

import net.sf.mily.bus.annotation.ActionService;
import net.sf.mily.support.tools.DesUtil;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

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

	@MatchActions({ActionEnum.Seller_Create})
	public void sellerCreate(ViewData<Seller> viewData) {
		Seller seller = viewData.getFirst();
		BaseDAO dao = new BaseDAO();
		if ("管理员".length()>0) {
			User domain = new User();
			domain.setSellerId(seller.getId());
			domain.setUserId("admin");
			domain.setUserName("管理员");
			domain.setPassword(new DesUtil().getEncrypt("123"));
			dao.saveOrUpdate(domain);
		}
		if ("领班岗位".length()>0) {
			User role = new User();
			role.setLinkType(1);
			role.setDeptName("领班人员");
			role.setUserName("管理员");
			dao.saveOrUpdate(role);
		}
	}

	@MatchActions({ActionEnum.Seller_Delete})
	public void sellerDelete(ViewData<Seller> viewData) {
		StringBuffer sb = new StringBuffer("delete from bs_user where sellerid=?");
		new BaseDAO().getSQLQuery(sb.toString()).executeUpdate();
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

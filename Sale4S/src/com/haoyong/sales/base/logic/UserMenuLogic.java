package com.haoyong.sales.base.logic;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.common.dao.BaseDAO;

/**
 * 全局菜单功能点
 */
public class UserMenuLogic {

	public User getMenu(String fullViewName) {
		String sql = "select t.* from bs_User t where t.linkType=2 and t.deptName=? and t.sellerId=?";
		User user = new BaseDAO().nativeQueryFirstResult(sql, User.class, fullViewName);
		return user;
	}
}

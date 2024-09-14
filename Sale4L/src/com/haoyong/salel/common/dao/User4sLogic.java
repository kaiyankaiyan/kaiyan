package com.haoyong.salel.common.dao;

import com.haoyong.salel.base.domain.User;


public class User4sLogic {
	
	public User getUser(String userid) {
		LinkSellerDAO dao = new LinkSellerDAO();
		return dao.nativeQuerySingleResult("select t.* from bs_user t where t.linkType=0 and t.userId=? and sellerId=?", User.class, userid);
	}
}

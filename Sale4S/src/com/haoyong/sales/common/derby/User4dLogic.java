package com.haoyong.sales.common.derby;

import java.util.List;

import net.sf.mily.types.LongType;

public class User4dLogic {
	
	public List<User4d> getUserList_1() {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=1 and t.sellerId=? order by t.id");
		List<User4d> list = new DerbyDAO().nativeQuery(sql.toString(), User4d.class);
		for (User4d d: list) {
			d.setChanged(false);
		}
		return list;
	}
	
	public List<User4d> getUserList_2() {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=2 and t.sellerId=? order by t.id");
		List<User4d> list = new DerbyDAO().nativeQuery(sql.toString(), User4d.class);
		for (User4d d: list) {
			d.setChanged(false);
		}
		return list;
	}
	
	public List<User4d> getUserList_2(String userName) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=2 and t.deptName=? and t.sellerId=?");
		return new DerbyDAO().nativeQuery(sql.toString(), User4d.class, userName);
	}
	
	public List<User4d> getUserList_10() {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=10 and t.sellerId=?");
		List<User4d> linkList = new DerbyDAO().nativeQuery(sql.toString(), User4d.class);
		return linkList;
	}
	
	public List<User4d> getUserList_10(long commId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=10 and t.deptName=? and t.sellerId=?");
		List<User4d> linkList = new DerbyDAO().nativeQuery(sql.toString(), User4d.class, new LongType().format(commId));
		return linkList;
	}
}

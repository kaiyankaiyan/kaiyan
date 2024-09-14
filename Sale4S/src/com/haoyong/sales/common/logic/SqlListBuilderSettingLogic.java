package com.haoyong.sales.common.logic;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.mily.webObject.query.SqlListBuilderSetting;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.common.derby.DerbyDAO;

public class SqlListBuilderSettingLogic {

	public ConcurrentLinkedQueue<SqlListBuilderSetting> getSettingsByUser(User user) {
		List<SqlListBuilderSetting> list = new DerbyDAO().nativeQuery("select t.* from mi_SqlListBuilderSetting t where t.userName=? and t.sellerId=?", SqlListBuilderSetting.class, user.getUserId());
		for (SqlListBuilderSetting st: list) {
			st.fromJson(st.getJson());
		}
		return new ConcurrentLinkedQueue<SqlListBuilderSetting>(list);
	}
}

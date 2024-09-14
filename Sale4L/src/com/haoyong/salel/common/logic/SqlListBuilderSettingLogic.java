package com.haoyong.salel.common.logic;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.mily.webObject.query.SqlListBuilderSetting;

import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.common.dao.BaseDAO;

public class SqlListBuilderSettingLogic {

	public ConcurrentLinkedQueue<SqlListBuilderSetting> getSettingsByUser(User user) {
		List<SqlListBuilderSetting> list = new BaseDAO().nativeQuery("select t.* from mi_SqlListBuilderSetting t where t.userName=?", SqlListBuilderSetting.class, user.getUserName());
		for (SqlListBuilderSetting st: list) {
			st.fromJson(st.getJson());
		}
		return new ConcurrentLinkedQueue<SqlListBuilderSetting>(list);
	}
}

package com.haoyong.sales.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.domain.AbstractDomain;

/**
 * 业务单据服务
 */
public class TicketUtil {

	/**
	 * 校验Ticket属性值是否已经存在
	 */
	public static boolean isValid(String domainTable, String field, AbstractDomain domain, Object fieldValue) {
		return isValid(domainTable, field, domain, fieldValue, null);
	}
	public static boolean isValid(String domainTable, String field, AbstractDomain domain, Object fieldValue, String condition) {
		boolean isExist = false;
		BaseDAO dao = new BaseDAO();
		StringBuffer sql = new StringBuffer("select t.id from ").append(domainTable).append(" t where t.").append(field).append("=? and t.id<>? and t.sellerid=?");
		if (StringUtils.isNotEmpty(condition))		sql.append(condition);
		List list = dao.nativeSqlQuery(sql.toString(), fieldValue, domain.getId());
		if (list.size() > 0) {
			isExist = true;
		}
		return !isExist;
	}
}

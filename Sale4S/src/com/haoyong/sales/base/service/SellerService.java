package com.haoyong.sales.base.service;

import java.util.List;

import net.sf.mily.bus.annotation.ActionService;
import net.sf.mily.common.SessionProvider;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.util.LogUtil;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.dao.TransactionManager;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

/**
 * 业务服务类——商家
 *
 */
@ActionService
public class SellerService {

	@MatchActions({ActionEnum.Seller_DeleteTickets})
	public void deleteTickets(ViewData viewData) {
		List<Class> ticketList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		TransactionManager.begin();
		for (Class ticket: ticketList) {
			StringBuffer sql = new StringBuffer();
			sql.append("delete from ");
			try {
				SessionProvider.getHbmConfiguration().getClassMapping(ticket.getName());
			sql.append(EntityClass.forName(ticket).getHbmClass().getTable().getName()).append(" ");
			}catch(Exception e) {
				LogUtil.error(e);
			}
			sql.append("where sellerId=?");
			dao.getSQLQuery(sql.toString()).executeUpdate();
		}
		TransactionManager.commit();
	}
}

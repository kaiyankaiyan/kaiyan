package com.haoyong.sales.sale.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.BillDetail;

/**
 * 应收（正数）应付（负数）服务
 */
@ActionService
public class BillTicketService {

	@MatchActions({ActionEnum.BillTicket_Save})
	public void save(ViewData<BillDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		for (BillDetail detail: viewData.getTicketDetails()) {
			dao.saveOrUpdate(detail);
		}
	}

	@MatchActions({ActionEnum.BillTicket_Delete})
	public void delete(ViewData<BillDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		for (BillDetail detail: viewData.getTicketDetails()) {
			dao.remove(detail);
		}
	}
}

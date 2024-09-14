package com.haoyong.sales.sale.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.BomDetail;

/**
 * 订单Bom物料
 */
@ActionService
public class BomTicketService {

	@MatchActions({ActionEnum.BomTicket_Save})
	public void save(ViewData<BomDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		for (BomDetail d: viewData.getTicketDetails()) {
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.BomTicket_Delete})
	public void delete(ViewData<BomDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		for (BomDetail d: viewData.getTicketDetails()) {
			dao.remove(d);
		}
	}
}

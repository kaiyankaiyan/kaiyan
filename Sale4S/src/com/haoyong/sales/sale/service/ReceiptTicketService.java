package com.haoyong.sales.sale.service;

import java.util.Date;
import java.util.List;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.OrderDetail;

/**
 * 收货服务
 */
@ActionService
public class ReceiptTicketService {

	@MatchActions({ActionEnum.ReceiptTicket_Save})
	public void save(ViewData<OrderDetail> viewData) {
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail detail: detailList) {
			detail.getReceiptTicket().setReceiptDate(new Date());
			detail.getReceiptTicket().setStorePrice(detail.getPrice());
			dao.saveOrUpdate(detail);
		}
	}
}

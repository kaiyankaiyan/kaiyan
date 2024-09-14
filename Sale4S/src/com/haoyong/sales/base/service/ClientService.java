package com.haoyong.sales.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

/**
 * 业务服务类——客户
 *
 */
@ActionService
public class ClientService {

	@MatchActions({ActionEnum.Client_Save})
	public void save(ViewData<Client> viewData) {
		for (Client domain: viewData.getTicketDetails()) {
			new BaseDAO().saveOrUpdate(domain);
		}
	}

	@MatchActions({ActionEnum.Client_Delete})
	public void delete(ViewData<Client> viewData) {
		for (Client domain: viewData.getTicketDetails()) {
			new BaseDAO().remove(domain);
		}
	}
}

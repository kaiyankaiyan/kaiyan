package com.haoyong.sales.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

/**
 * 业务服务类——仓库
 *
 */
@ActionService
public class StorehouseService {

	@MatchActions({ActionEnum.Storehouse_Save})
	public void save(ViewData<Storehouse> viewData) {
		for (Storehouse domain: viewData.getTicketDetails()) {
			new BaseDAO().saveOrUpdate(domain);
		}
	}

	@MatchActions({ActionEnum.Storehouse_Delete})
	public void delete(ViewData<Storehouse> viewData) {
		for (Storehouse domain: viewData.getTicketDetails()) {
			new BaseDAO().remove(domain);
		}
	}
}

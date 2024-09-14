package com.haoyong.salel.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.salel.base.domain.City;
import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.form.ActionEnum;
import com.haoyong.salel.common.form.MatchActions;
import com.haoyong.salel.common.form.ViewData;

/**
 * 业务服务类——城市
 *
 */

@ActionService
public class CityService {

	@MatchActions({ActionEnum.City_Effect})
	public void create(ViewData<City> viewData) {
		City domain = viewData.getTicketFirst();
		new BaseDAO().saveOrUpdate(domain);
	}

	@MatchActions({ActionEnum.City_Delete})
	public void delete(ViewData<City> viewData) {
		City domain = viewData.getTicketFirst();
		new BaseDAO().remove(domain);
	}
}

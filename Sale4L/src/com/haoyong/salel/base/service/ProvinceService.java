package com.haoyong.salel.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.salel.base.domain.Province;
import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.form.ActionEnum;
import com.haoyong.salel.common.form.MatchActions;
import com.haoyong.salel.common.form.ViewData;

/**
 * 业务服务类——省份
 *
 */

@ActionService
public class ProvinceService {

	@MatchActions({ActionEnum.Province_Effect})
	public void create(ViewData<Province> viewData) {
		Province domain = viewData.getTicketFirst();
		new BaseDAO().saveOrUpdate(domain);
	}

	@MatchActions({ActionEnum.Province_Delete})
	public void delete(ViewData<Province> viewData) {
		Province domain = viewData.getTicketFirst();
		new BaseDAO().remove(domain);
	}
}

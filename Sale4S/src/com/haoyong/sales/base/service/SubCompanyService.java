package com.haoyong.sales.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

/**
 * 业务服务类——客户
 *
 */
@ActionService
public class SubCompanyService {

	@MatchActions({ActionEnum.SubCompany_Save})
	public void save(ViewData<SubCompany> viewData) {
		for (SubCompany domain: viewData.getTicketDetails()) {
			new BaseDAO().saveOrUpdate(domain);
		}
	}

	@MatchActions({ActionEnum.SubCompany_Delete})
	public void delete(ViewData<SubCompany> viewData) {
		for (SubCompany domain: viewData.getTicketDetails()) {
			new BaseDAO().remove(domain);
		}
	}
}

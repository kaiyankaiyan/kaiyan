package com.haoyong.sales.sale.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.PrintModel;

/**
 * 打印模板服务
 */
@ActionService
public class PrintModelService {

	@MatchActions({ActionEnum.PrintModel_Save})
	public void save(ViewData<PrintModel> viewData) {
		BaseDAO dao = new BaseDAO();
		for (PrintModel model: viewData.getTicketDetails()) {
			dao.saveOrUpdate(model);
		}
	}
}

package com.haoyong.sales.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

/**
 * 业务服务类——供应商
 *
 */
@ActionService
public class SupplierService {

	@MatchActions({ActionEnum.Supplier_Save})
	public void create(ViewData<Supplier> viewData) {
		for (Supplier domain: viewData.getTicketDetails()) {
			new SupplierLogic().getPropertyChoosableLogic().toTrunk(domain);
			new BaseDAO().saveOrUpdate(domain);
		}
	}

	@MatchActions({ActionEnum.Supplier_Delete})
	public void delete(ViewData<Supplier> viewData) {
		for (Supplier domain: viewData.getTicketDetails()) {
			new BaseDAO().remove(domain);
		}
	}
}

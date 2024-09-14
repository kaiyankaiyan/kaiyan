package com.haoyong.sales.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;

/**
 * 业务服务类——商品
 *
 */

@ActionService
public class CommodityService {

	@MatchActions({ActionEnum.Commodity_Save})
	public void create(ViewData<Commodity> viewData) {
		BaseDAO dao = new BaseDAO();
		for (Commodity d: viewData.getTicketDetails()) {
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.Commodity_Delete})
	public void delete(ViewData<Commodity> viewData) {
		BaseDAO dao = new BaseDAO();
		for(Commodity commodity: viewData.getTicketDetails()){
			dao.remove(commodity);
		}
	}
}

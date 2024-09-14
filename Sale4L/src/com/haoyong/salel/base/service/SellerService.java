package com.haoyong.salel.base.service;

import net.sf.mily.bus.annotation.ActionService;

import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.form.ActionEnum;
import com.haoyong.salel.common.form.MatchActions;
import com.haoyong.salel.common.form.ViewData;
import com.haoyong.salel.util.HttpSellerUtil;

/**
 * 业务服务类——商家
 */
@ActionService
public class SellerService {

	@MatchActions({ActionEnum.Seller_Create})
	public void create(ViewData<Seller> viewData){
		Seller domain = viewData.getTicketFirst();
		new BaseDAO().saveOrUpdate(domain);
		new HttpSellerUtil().action(ActionEnum.Seller_Create, domain, viewData.getCurrentUser());
	}
	
	@MatchActions({ActionEnum.Seller_Edit})
	public void edit(ViewData<Seller> viewData) {
		BaseDAO dao=new BaseDAO();
		for(Seller seller: viewData.getTicketDetails()){
			dao.saveOrUpdate(seller);
		}
	}
	
	@MatchActions({ActionEnum.Seller_Delete})
	public void delete(ViewData<Seller> viewData) {
		for (Seller domain: viewData.getTicketDetails()) {
			new BaseDAO().remove(domain);
			new HttpSellerUtil().action(ActionEnum.Seller_Delete, domain, viewData.getCurrentUser());
		}
	}
}

package com.haoyong.sales.sale.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.mily.bus.annotation.ActionService;
import net.sf.mily.common.NoteAccessorFormer;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;

/**
 * 采购单
 */
@ActionService
public class PurchaseTicketService {

	@MatchActions({ActionEnum.PurchaseTicket_Save})
	public void save(ViewData<OrderDetail> viewData) {
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: detailList) {
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.PurchaseTicket_Delete})
	public void delete(ViewData<OrderDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		for (OrderDetail pur: viewData.getTicketDetails()) {
			dao.remove(pur);
		}
	}

	@MatchActions({ActionEnum.PurchaseTicket_ChangeEffect})
	public void changeEffect(ViewData<OrderDetail> viewData) throws ParseException {
		Class changeType = (Class)viewData.getParam("ChangeType");
		NoteAccessorFormer<OrderDetail> accessor = viewData.getParam(NoteAccessorFormer.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail pur: detailList) {
			LinkedHashMap<String, String> mall=accessor.getVoNoteMap(pur), mpart=new PurchaseTicketLogic().getVoNoteMap(accessor, pur, changeType);
			if (changeType==ArrayList.class) {
				List<String> propList = viewData.getParam(ArrayList.class);
				mpart = accessor.getVoNoteMapIN(pur, propList.toArray(new String[0]));
			}
			accessor.setEntityChanges(pur, mpart);
			for (String k: mpart.keySet()) {
				mall.remove(k);
			}
			if (mall.isEmpty()) {
				pur.setChangeRemark(null);
				pur.setNotes(null);
			} else {
				accessor.isChangedNotesEX(pur);
			}
			dao.saveOrUpdate(pur);
		}
	}

	@MatchActions({ActionEnum.PurchaseTicket_ChangeClear})
	public void changeClear(ViewData<OrderDetail> viewData) {
		Class changeType = (Class)viewData.getParam("ChangeType");
		NoteAccessorFormer<OrderDetail> accessor = viewData.getParam(NoteAccessorFormer.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: detailList) {
			Map<String, String> mall=accessor.getVoNoteMap(d), mpart=new PurchaseTicketLogic().getVoNoteMap(accessor, d, changeType);
			for (String k: mpart.keySet()) {
				mall.remove(k);
			}
			if (mall.isEmpty()) {
				d.setChangeRemark(null);
				d.setNotes(null);
			} else {
				accessor.isChangedNotesEX(d);
			}
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.OrderTicket_AdjustEffect})
	public void adjustEffect4Order(ViewData<OrderDetail> viewData) throws ParseException {
		NoteAccessorFormer<OrderDetail> noteFormer = viewData.getParam(NoteAccessorFormer.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: detailList) {
			OrderDetail p = d.getVoparam(OrderDetail.class);
			if (p != null) {
				p.setCommodity(d.getCommodity());
				p.setAmount(d.getAmount());
				dao.saveOrUpdate(p);
			}
		}
	}

	@MatchActions({ActionEnum.PurchaseTicket_AdjustEffect})
	public void adjustEffect(ViewData<OrderDetail> viewData) {
		NoteAccessorFormer<OrderDetail> noteFormer = viewData.getParam(NoteAccessorFormer.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: detailList) {
			noteFormer.setEntityChanges(d);
			d.setChangeRemark(null);
			d.setNotes(null);
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.PurchaseTicket_AdjustDelete})
	public void adjustDelete(ViewData<OrderDetail> viewData) {
		NoteAccessorFormer<OrderDetail> noteFormer = viewData.getParam(NoteAccessorFormer.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: detailList) {
			noteFormer.getVoNoteMap(d).clear();
			d.setChangeRemark(null);
			d.setNotes(null);
			dao.saveOrUpdate(d);
		}
	}
}

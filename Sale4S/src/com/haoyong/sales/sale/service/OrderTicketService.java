package com.haoyong.sales.sale.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.sf.mily.bus.annotation.ActionService;
import net.sf.mily.common.NoteAccessorFormer;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.ArrangeTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.test.sale.OrderTicketTest;

/**
 * 订单服务
 */
@ActionService
public class OrderTicketService {

	@MatchActions({ActionEnum.OrderTicket_T})
	public void saveTicket(ViewData<OrderTicket> viewData) {
		BaseDAO dao = new BaseDAO();
		for (OrderTicket d: viewData.getTicketDetails()) {
			dao.saveOrUpdate(d);
		}
	}
	
	@MatchActions({ActionEnum.OrderTicket_Count})
	public void orderCount(ViewData<OrderDetail> viewData) {
		LinkedHashSet<String> numbers = new LinkedHashSet<String>();
		for (OrderDetail d: viewData.getTicketDetails()) {
			OrderDetail sd = d.getSnapShot();
			numbers.add(d.getOrderTicket().getNumber());
			numbers.add(sd.getOrderTicket().getNumber());
		}
		if (numbers.size()>0)
			new OrderTicketTest().check订单跟踪统计(numbers.toArray(new String[0]));
	}

	@MatchActions({ActionEnum.OrderTicket_Save})
	public void saveDetail(ViewData<OrderDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: viewData.getTicketDetails()) {
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.OrderTicket_Delete})
	public void delete(ViewData<OrderDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: viewData.getTicketDetails()) {
			dao.remove(d);
		}
	}

	@MatchActions({ActionEnum.OrderTicket_ChangeEffect})
	public void changeEffect(ViewData<OrderDetail> viewData) throws ParseException {
		Class changeType = (Class)viewData.getParam("ChangeType");
		NoteAccessorFormer<OrderDetail> accessor = viewData.getParam(NoteAccessorFormer.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: detailList) {
			LinkedHashMap<String, String> mall=accessor.getVoNoteMap(d), mpart=new OrderTicketLogic().getVoNoteMap(accessor, d, changeType);
			if (changeType==ArrayList.class) {
				List<String> propList = viewData.getParam(ArrayList.class);
				mpart = accessor.getVoNoteMapIN(d, propList.toArray(new String[0]));
			}
			accessor.setEntityChanges(d, mpart);
			for (String k: mpart.keySet()) {
				mall.remove(k);
			}
			if (mall.isEmpty()) {
				d.setChangeRemark(null);
				d.setNotes(null);
				d.getArrangeTicket().setCancelType(null);
			} else {
				accessor.isChangedNotesEX(d);
			}
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.OrderTicket_ChangeClear})
	public void changeClear(ViewData<OrderDetail> viewData) {
		NoteAccessorFormer<OrderDetail> accessor = viewData.getParam(NoteAccessorFormer.class);
		List<String> changeList = (List<String>)viewData.getParam("ChangeList");
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		Class noteType = null;
		if (changeList.contains("排单")) {
			noteType = ArrangeTicket.class;
		} else if (changeList.contains("订单")) {
			noteType = OrderTicket.class;
		}
		for (OrderDetail d: detailList) {
			Map<String,String> mall=accessor.getVoNoteMap(d), mpart=new OrderTicketLogic().getVoNoteMap(accessor, d, noteType);
			for (String k: mpart.keySet()) {
				mall.remove(k);
			}
			if (mall.isEmpty()) {
				d.setChangeRemark(null);
				d.setNotes(null);
				d.getArrangeTicket().setCancelType(null);
			} else {
				accessor.isChangedNotesEX(d);
			}
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.OrderTicket_AdjustEffect})
	public void adjustEffect(ViewData<OrderDetail> viewData) throws ParseException {
		NoteAccessorFormer<OrderDetail> accessor = viewData.getParam(NoteAccessorFormer.class);
		String adjustType = viewData.getParam(String.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		Class noteType = null;
		if ("排单".equals(adjustType)) {
			noteType = ArrangeTicket.class;
		} else if ("订单".equals(adjustType)) {
			noteType = OrderTicket.class;
		}
		for (OrderDetail d: detailList) {
			LinkedHashMap<String, String> mall=accessor.getVoNoteMap(d), mpart=new OrderTicketLogic().getVoNoteMap(accessor, d, noteType);
			accessor.setEntityChanges(d, mpart);
			for (String k: mpart.keySet()) {
				mall.remove(k);
			}
			if (mall.isEmpty()) {
				d.setNotes(null);
				d.setChangeRemark(null);
				d.getArrangeTicket().setCancelType(null);
			} else {
				accessor.isChangedNotesEX(d);
			}
			dao.saveOrUpdate(d);
		}
	}

	@MatchActions({ActionEnum.OrderTicket_AdjustDelete})
	public void adjustDelete(ViewData<OrderDetail> viewData) throws ParseException {
		Class adjustType = (Class)viewData.getParam("AdjustType");
		NoteAccessorFormer<OrderDetail> accessor = viewData.getParam(NoteAccessorFormer.class);
		List<OrderDetail> detailList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (OrderDetail d: detailList) {
			Map<String, String> mall=accessor.getVoNoteMap(d), mpart=new OrderTicketLogic().getVoNoteMap(accessor, d, adjustType);
			for (String k: mpart.keySet()) {
				mall.remove(k);
			}
			if (mall.isEmpty()) {
				d.setChangeRemark(null);
				d.setNotes(null);
				d.getArrangeTicket().setCancelType(null);
			} else {
				accessor.isChangedNotesEX(d);
			}
			dao.saveOrUpdate(d);
		}
	}
}

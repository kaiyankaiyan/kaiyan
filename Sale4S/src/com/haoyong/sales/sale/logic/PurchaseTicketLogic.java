package com.haoyong.sales.sale.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.sf.mily.common.NoteAccessorFormer;
import net.sf.mily.support.tools.TicketPropertyUtil;

import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ArrangeT;
import com.haoyong.sales.sale.domain.LocationT;
import com.haoyong.sales.sale.domain.LocationTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.PurchaseT;
import com.haoyong.sales.sale.domain.PurchaseTicket;
import com.haoyong.sales.sale.domain.ReceiptT;
import com.haoyong.sales.sale.domain.ReceiptTicket;
import com.haoyong.sales.sale.form.LocationTicketForm;
import com.haoyong.sales.sale.form.PPurchaseTicketForm;
import com.haoyong.sales.sale.form.PurchaseTicketForm;

public class PurchaseTicketLogic {
	
	public OrderDetail genClonePurchase(OrderDetail detail) {
		OrderDetail d = new OrderDetail();
		d.setCommodity(detail.getCommodity());
		TicketPropertyUtil.copyProperties(detail, d);
		d.setVoparam(detail.getVoparam(PurchaseT.class));
		d.setVoparam(detail.getVoparam(ReceiptT.class));
		Assert.assertTrue("入库价有复制", d.getReceiptTicket().getStorePrice()==detail.getReceiptTicket().getStorePrice()
				&& d.getStPurchase()==detail.getStPurchase() && d.getReceiptId()==detail.getReceiptId());
		return d;
	}
	
	public void setPurchaseTicket(OrderDetail purGet, OrderDetail ordSet) {
		ordSet.setArrangeTicket(purGet.getArrangeTicket());
		ordSet.setVoparam(purGet.getVoparam(ArrangeT.class));
		ordSet.setUarrange(purGet.getUarrange());
		ordSet.setArrangeId(purGet.getArrangeId());
		
		ordSet.setPurchaseTicket(purGet.getPurchaseTicket());
		ordSet.setVoparam(purGet.getVoparam(PurchaseT.class));
		ordSet.setUpurchase(purGet.getUpurchase());
		ordSet.setStPurchase(purGet.getStPurchase());
		
		ordSet.setReceiptTicket(purGet.getReceiptTicket());
		ordSet.getReceiptTicket().setReceiptAmount(0);
		ordSet.getReceiptTicket().setBadAmount(0);
		ordSet.setVoparam(purGet.getVoparam(ReceiptT.class));
		ordSet.setUreceipt(purGet.getUreceipt());
		ordSet.setReceiptId(purGet.getReceiptId());
		
		ordSet.getLocationTicket().setIn(purGet.getLocationTicket().getIn());
		ordSet.setVoparam(purGet.getVoparam(LocationT.class));
	}
	
	public List<OrderDetail> getDetails(List<String> monthnumList) {
		if (monthnumList.size()==0)
			return new ArrayList<OrderDetail>();
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from sa_OrderDetail t where (");
		for (String monthnum: monthnumList) {
			sql.append("t.monthnum='").append(monthnum).append("' or ");
		}
		List<OrderDetail> purList = new BaseDAO().nativeQuery(sql.delete(sql.length()-4, sql.length()).append(") and t.sellerId=?").toString(), OrderDetail.class);
		for (OrderDetail pur: purList) {
			pur.getSnapShot();
		}
		return purList;
	}
	
	public PropertyChoosableLogic.TicketDetail<PurchaseTicketForm, PurchaseTicket, PurchaseT> getTicketChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<PurchaseTicketForm, PurchaseTicket, PurchaseT>(new PurchaseTicketForm(), new PurchaseTicket(), new PurchaseT());
	}
	
	public PropertyChoosableLogic.TicketDetail<PurchaseTicketForm, PurchaseTicket, PurchaseT> getPTicketChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<PurchaseTicketForm, PurchaseTicket, PurchaseT>(new PPurchaseTicketForm(), new PurchaseTicket(), new PurchaseT());
	}
	
	public PropertyChoosableLogic.TicketDetail<LocationTicketForm, LocationTicket, LocationT> getLocationChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<LocationTicketForm, LocationTicket, LocationT>(new LocationTicketForm(), new LocationTicket(), new LocationT());
	}
	
	public LinkedHashMap<String, String> getVoNoteMap(NoteAccessorFormer<OrderDetail> accessor, OrderDetail detail, Class noteType) {
		if (noteType==PurchaseTicket.class) {
			return accessor.getVoNoteMapIN(detail, "PurchaseTicket");
		} else if (noteType==ReceiptTicket.class) {
			return accessor.getVoNoteMapIN(detail, "ReceiptTicket");
		} else if (noteType==OrderDetail.class) {
			LinkedHashMap<String, String> mall = accessor.getVoNoteMap(detail);
			return new LinkedHashMap<String, String>(mall);
		}
		return null;
	}
	
	/**
	 * 上级客户订单，取相应的下级作为本级的客户、分公司采购单
	 */
	public Map.Entry<List<String>, List<OrderDetail>> getDetails4Monthnum(List<String> monthnumList) {
		StringBuffer sql = new StringBuffer().append("select t.* from sa_OrderDetail t where (");
		LinkedHashSet<String> keys = new LinkedHashSet<String>();
		for (Iterator<String> iter=monthnumList.iterator(); iter.hasNext();) {
			String monthnum = iter.next();
			int idx = monthnum.indexOf("_");
			String key = null;
			if (idx == -1) {
				key = monthnum;
				sql.append("t.monthnum='").append(key).append("'");
			} else {
				key = monthnum.substring(0, idx);
				sql.append("t.monthnum like '").append(key).append("%'");
			}
			keys.add(key);
			if (iter.hasNext())
				sql.append(" or ");
		}
		sql.append(") and t.sellerId=?");
		BaseDAO dao = new BaseDAO();
		List<OrderDetail> list = dao.nativeQuery(sql.toString(), OrderDetail.class);
		List<String> monthnums = new ArrayList<String>();
		for (Iterator<OrderDetail> iter=list.iterator(); iter.hasNext(); monthnums.add(iter.next().getMonthnum()));
		HashMap<List<String>, List<OrderDetail>> map = new HashMap<List<String>, List<OrderDetail>>();
		map.put(monthnums, list);
		return map.entrySet().iterator().next();
	}
	
	public List<OrderDetail> getDetailInIDs(String inIDs) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from sa_OrderDetail t where t.id in (").append(inIDs).append(") and t.sellerId=?");
		BaseDAO dao = new BaseDAO();
		List<OrderDetail> list = dao.nativeQuery(sql.toString(), OrderDetail.class);
		return list;
	}
}
package com.haoyong.sales.sale.logic;

import java.util.LinkedHashMap;
import java.util.List;

import net.sf.mily.common.NoteAccessorFormer;
import net.sf.mily.support.tools.TicketPropertyUtil;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ArrangeTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderT;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.form.OrderTicketForm;

public class OrderTicketLogic {
	
	public PropertyChoosableLogic.TicketDetail<OrderTicketForm, OrderTicket, OrderT> getTicketChoosableLogic() {
		return new PropertyChoosableLogic.TicketDetail<OrderTicketForm, OrderTicket, OrderT>(new OrderTicketForm(), new OrderTicket(), new OrderT());
	}
	
	public OrderDetail genCloneOrder(OrderDetail detail) {
		OrderDetail d = new OrderDetail();
		d.setCommodity(detail.getCommodity());
		TicketPropertyUtil.copyProperties(detail, d);
		return d;
	}
	
	public void setOrderTicket(OrderDetail ordGet, OrderDetail purSet, String monthnum) {
		purSet.setMonthnum(monthnum);
		
		purSet.setOrderTicket(ordGet.getOrderTicket());
		purSet.setClient(ordGet.getClient());
		purSet.setSubCompany(ordGet.getSubCompany());
		purSet.setStOrder(ordGet.getStOrder());
		purSet.setUorder(ordGet.getUorder());
		
		purSet.setSendId(ordGet.getSendId());
	}
	
	public String genMonthnum() {
		return new SerialNumberFactory().serialLen3("", SerialNumberFactory.Month, 10).getNextSerial();
	}
	
	public String getSplitMonthnum(String monthnum) {
		if (monthnum==null)			return null;
		StringBuffer sb = new StringBuffer();
		int i1=monthnum.indexOf("-");
		if (i1>-1) {
			sb.append(monthnum.substring(0, i1));
		} else {
			sb.append(monthnum);
		}
		sb.append("-").append(new SerialNumberFactory().serialLen2(monthnum.substring(0, 1), null, 1).getNextIndex36());
		return sb.toString();
	}
	
	public String getPrtMonthnum(String childMonthnum) {
		int ci=childMonthnum.indexOf("-");
		return ci==-1? childMonthnum: childMonthnum.substring(0, ci);
	}
	
	public String getPrtBomMonthnum(String purchaseMonthnum0) {
		String bomMonthnum = purchaseMonthnum0;
		int ci=bomMonthnum.indexOf("-");
		bomMonthnum = (ci==-1? bomMonthnum: bomMonthnum.substring(0, ci));
		ci=bomMonthnum.indexOf(".");
		bomMonthnum = (ci==-1? bomMonthnum: bomMonthnum.substring(0, ci));
		return bomMonthnum;
	}
	
	public boolean isSplitMonthnum(String child, String parent) {
		int pi=parent.indexOf("-"), ci=child.indexOf("-");
		String mparent = pi==-1? parent: parent.substring(0, pi);
		if (ci > -1)
			return child.startsWith(mparent.concat("-"));
		return false;
	}
	
	public String getLinkMonthnum(String monthnum) {
		if (monthnum==null)			return null;
		StringBuffer sb = new StringBuffer();
		int i1=monthnum.indexOf("-"), i2=monthnum.indexOf(".");
		if (i1>-1) {
			sb.append(monthnum.substring(0, i1));
		} else if (i2>-1) {
			sb.append(monthnum.substring(0, i2));
		} else {
			sb.append(monthnum);
		}
		sb.append(".");
		sb.append(this.genMonthnum());
		return sb.toString();
	}
	
	public String getBomMonthnum(String monthnum) {
		if (monthnum==null)			return null;
		StringBuffer sb = new StringBuffer();
		int i1=monthnum.indexOf(".");
		if (i1>-1) {
			sb.append(monthnum.substring(0, i1));
		} else {
			sb.append(monthnum);
		}
		sb.append(".").append(new SerialNumberFactory().serialLen2(monthnum.substring(0, 1), null, 1).getNextIndex36());
		return sb.toString();
	}
	
	public LinkedHashMap<String, String> getVoNoteMap(NoteAccessorFormer<OrderDetail> accessor, OrderDetail detail, Class noteType) {
		if (noteType==ArrangeTicket.class) {
			return accessor.getVoNoteMapIN(detail, "ArrangeTicket");
		} else if (noteType==OrderTicket.class) {
			return accessor.getVoNoteMapIN(detail, "OrderTicket","commodity","client","subCompany","amount");
		}
		return null;
	}
	
	public List<OrderDetail> getDetailInIDs(String inIDs) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from sa_OrderDetail t where t.id in (").append(inIDs).append(") and t.sellerId=?");
		BaseDAO dao = new BaseDAO();
		List<OrderDetail> list = dao.nativeQuery(sql.toString(), OrderDetail.class);
		return list;
	}
	
	public OrderTicket getTicket(String number) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from sa_OrderTicket t where t.number=? and t.sellerId=?");
		return new BaseDAO().nativeQuerySingleResult(sql.toString(), OrderTicket.class, number);
	}
}
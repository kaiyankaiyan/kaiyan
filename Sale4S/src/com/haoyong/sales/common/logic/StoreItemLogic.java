package com.haoyong.sales.common.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.throwable.LogicException;

import org.hibernate.tool.hbm2x.StringUtils;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.sale.domain.StoreItem;

public class StoreItemLogic {
	
	public StoreItem getStoreItemByCommodityId(long commodityId) {
		BaseDAO dao = new BaseDAO();
		return dao.nativeQuerySingleResult("select * from sa_storeItem where commodityId=? and sellerId=?", StoreItem.class,
				commodityId);
	}
	
	public StoreItem getStoreItemByCommodityName(String name) {
		BaseDAO dao = new BaseDAO();
		return dao.nativeQuerySingleResult("select * from sa_storeItem where name=? and sellerId=?", StoreItem.class,
				name);
	}
	
	public List<StoreItem> getStoreItemList(){
		return new BaseDAO().nativeQuery("select * from sa_storeItem", StoreItem.class);
	}
	
	
	public StoreItem getStoreItemByCommodityNumber(String number){
		return new BaseDAO().nativeQuerySingleResult("select * from sa_storeItem where number=? and sellerId=?", StoreItem.class, number);
	}

	public List getStoreItemList(List<? extends AbstractCommodityItem> detailList) {
		if (detailList.size() == 0)
			return new ArrayList<StoreItem>();
		BaseDAO dao = new BaseDAO();
		StringBuilder materialIDs = new StringBuilder();
		for (Iterator<? extends AbstractCommodityItem> iter = detailList.iterator(); iter.hasNext();) {
			AbstractCommodityItem item = iter.next();
			materialIDs.append(getSQLCommodity(item.getCommodity(), "t"));
			if (iter.hasNext())
				materialIDs.append(" or ");
		}
		String sql = "select t.* from sa_storeItem t where " + materialIDs.toString();
		List<StoreItem> source = dao.nativeQuery(sql, StoreItem.class);
		return source;
	}

	public String getSQLCommodity(Commodity ticketCommodity, String tableAria) {
		boolean ok = false;
		StringBuilder sb = new StringBuilder();
		tableAria = " ".concat(tableAria);
		if (StringUtils.isNotEmpty(ticketCommodity.getNumber())) {
			ok = StringUtils.isNotEmpty(ticketCommodity.getNumber());
			sb.append("(").append(tableAria).append(".commNumber='").append(ticketCommodity.getNumber()).append("' ) ");
		} else {
			// 可能有问题
			sb.append("(").append(tableAria).append(".commodityId=0 and ");
			String name = ticketCommodity.getName();
			if (name != null) {
				ok = true;
				sb.append(tableAria).append(".name='").append(ticketCommodity.getName()).append("')");// 为什么多个and???
			} else {
				sb.append(tableAria).append(".name is null )");// 为什么多个and???
			}
		}
		if (!ok)
			throw new LogicException(2, "无效商品");
		return sb.toString();
	}
}
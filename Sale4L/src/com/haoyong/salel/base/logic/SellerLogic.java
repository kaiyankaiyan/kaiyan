package com.haoyong.salel.base.logic;

import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.database.DatabaseInfoReader;

import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.common.dao.BaseDAO;

/**
 *查询支持类————商家
 */
public class SellerLogic {
	
	public List<Seller> getSellerAll() {
		StringBuffer sb = new StringBuffer("select t.* from bs_Seller t where ");
		for (Iterator<String> diter=DatabaseInfoReader.getReader().getDatabaseInfo().getSellerList().iterator(); diter.hasNext();) {
			String dname = diter.next();
			sb.append("t.sqlName='").append(dname).append("'").append(diter.hasNext()? " or ": "");
		}
		return new BaseDAO().nativeQuery(sb.toString(), Seller.class);
	}
	
	public Seller getTestSeller() {
		return this.getSeller("南宁古城");
	}
	
	public Seller getSeller(String name){
		return new BaseDAO().nativeQuerySingleResult("select t.* from bs_Seller t where t.name=?", Seller.class, name);
	}
	
	public Seller getSellerById(long seller_id){
		return new BaseDAO().nativeQuerySingleResult("select t.* from bs_Seller t where t.id=?", Seller.class, seller_id);
	}
}

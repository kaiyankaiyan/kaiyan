package com.haoyong.sales.base.logic;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.common.dao.LinkLoginDAO;

public class Seller4lLogic {
	
	public Seller getSeller(String name){
		return new LinkLoginDAO().nativeQuerySingleResult("select t.* from bs_Seller t where t.name=?", Seller.class, name);
	}
	
	public Seller getSeller(String name, long notSellerId){
		return new LinkLoginDAO().nativeQuerySingleResult("select t.* from bs_Seller t where t.name=? and t.id!=?", Seller.class, name, notSellerId);
	}
	
	public Seller getSellerById(long seller_id){
		return new LinkLoginDAO().nativeQuerySingleResult("select t.* from bs_Seller t where t.id=?", Seller.class, seller_id);
	}
	
	public Seller get南宁古城() {
		return new LinkLoginDAO().nativeQuerySingleResult("select t.* from bs_seller t where t.name='南宁古城'", Seller.class);
	}
	
	public Seller get吉高电子() {
		return new LinkLoginDAO().nativeQuerySingleResult("select t.* from bs_seller t where t.name='吉高电子'", Seller.class);
	}
}

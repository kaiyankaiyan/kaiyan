package com.haoyong.sales.base.logic;

import net.sf.mily.ui.WindowMonitor;

import com.haoyong.sales.base.domain.Seller;

public class SellerLogic {

	public static Seller getSeller() {
		return (Seller)WindowMonitor.getMonitor().getAttribute("seller");
	}
	
	public static long getSellerId() {
		Seller seller = getSeller();
		return seller==null? 0: seller.getId();
	}
}

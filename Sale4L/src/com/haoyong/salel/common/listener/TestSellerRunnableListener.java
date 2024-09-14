package com.haoyong.salel.common.listener;

import net.sf.mily.common.SessionProvider;

import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.base.logic.SellerLogic;
import com.haoyong.salel.util.HttpSellerUtil;

public class TestSellerRunnableListener extends RunnableListener {
	
	private String listenerName;
	
	public TestSellerRunnableListener(String listenerName) {
		this.listenerName = listenerName;
	}

	protected void runTask() throws Exception {
		Seller seller = new SellerLogic().getTestSeller();
		new HttpSellerUtil().listener(this.listenerName, seller);
	}
	
	protected void runAfter() {
		new SessionProvider().clear();
	}

	protected boolean isRunnable() {
		return true;
	}
}

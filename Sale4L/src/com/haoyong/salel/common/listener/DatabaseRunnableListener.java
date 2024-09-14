package com.haoyong.salel.common.listener;

import java.util.HashSet;

import net.sf.mily.common.SessionProvider;

import com.haoyong.salel.base.domain.Seller;
import com.haoyong.salel.base.logic.SellerLogic;
import com.haoyong.salel.util.HttpSellerUtil;

public class DatabaseRunnableListener extends RunnableListener {
	
	private String listenerName;
	
	public DatabaseRunnableListener(String listenerName) {
		this.listenerName = listenerName;
	}

	protected void runTask() throws Exception {
		HashSet<String> names = new HashSet<String>();
		for (Seller seller: new SellerLogic().getSellerAll()) {
			if (names.add(seller.getSqlName())==true)
				new HttpSellerUtil().listener(this.listenerName, seller);
		}
	}
	
	public void runAfter() {
		new SessionProvider().clear();
	}

	protected boolean isRunnable() {
		return true;
	}
}

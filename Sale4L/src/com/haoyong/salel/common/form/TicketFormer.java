package com.haoyong.salel.common.form;

import java.util.List;

import com.haoyong.salel.common.domain.AbstractDomain;

public interface TicketFormer<D extends Object> {
	
	public D getDomain();

	public List<D> getDetailList();
	
	public static interface NextTicketFormer<D extends AbstractDomain> extends TicketFormer<D> {
	
		public void prepareNext();
	}
}

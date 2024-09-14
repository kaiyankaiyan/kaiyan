package com.haoyong.salel.common.domain;
/**
 * 
 * 用于日志跟踪
 * 
 * 订单、接单、采购、生产、订造、请购、生产收货、采购收货、订造收货、发料明细等会拆分的明细，
 * 日志跟踪要可追溯来源
 * 
 * @author laixd, 2015-09-29
 *
 */
public interface SplitFromDomain {
	/**
	 * 拆分的源domain id
	 * 
	 * @return
	 */
	public long getSplitFromId();
	public void setSplitFromId(long fromId);
}

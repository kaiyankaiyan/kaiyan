package com.haoyong.salel.common.domain;

/**
 * <pre>
 * Title: 具有序列号
 * Description:

 * </pre>
 */
public interface Serialy {

	/**
	 * 获取单号（序列号）
	 */
	public String getSerialNumber();

	/**
	 * 生成新单号
	 */
	public void genSerialNumber();
}

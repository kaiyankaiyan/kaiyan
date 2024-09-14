package com.haoyong.salel.base.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 流水号当前最大序号
 */
@Entity
@Table(name = "bs_serialnumberinfo")
public class SerialNumberInfo {

	private String prefix;
	
	private int sn;
	
	private long sellerId;
	
	private Date createDate;
	
	@Id
	protected String getPrefix() {
		return prefix;
	}

	protected void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	protected int getSn() {
		return sn;
	}

	protected void setSn(int sn) {
		this.sn = sn;
	}

	public long getSellerId() {
		return sellerId;
	}

	public void setSellerId(long sellerId) {
		this.sellerId = sellerId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	protected Date getCreateDate() {
		return createDate;
	}

	protected void setCreateDate(Date createtime) {
		this.createDate = createtime;
	}
}

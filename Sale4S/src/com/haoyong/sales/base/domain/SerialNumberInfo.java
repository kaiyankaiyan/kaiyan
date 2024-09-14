package com.haoyong.sales.base.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

/**
 * 流水号当前最大序号
 */
@Entity
@Table(name = "bs_serialnumberinfo")
public class SerialNumberInfo {
	
	private long id;

	private String prefix;
	
	private int sn;
	
	private long sellerId;
	
	private Date createDate;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	@Index(name="iSellerId")
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

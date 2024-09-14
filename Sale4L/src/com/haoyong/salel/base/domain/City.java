package com.haoyong.salel.base.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.haoyong.salel.common.domain.AbstractDomain;

/**
 * 城市
 */
@Entity
@Table(name = "bs_city")
public class City extends AbstractDomain{

	private static final long serialVersionUID = -3934490240176430976L;

	/** 城市编号 */
	private String number;

	/** 城市名字 */
	private String name;

	/** 所属的省份 */
	private long provinceId;

	@Column(length = 50)
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Column(length = 50)
	public String getName() { 
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(long provinceId) {
		this.provinceId = provinceId;
	}
}

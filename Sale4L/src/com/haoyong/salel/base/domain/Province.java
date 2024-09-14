package com.haoyong.salel.base.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.haoyong.salel.common.domain.AbstractDomain;

/**
 * 省份
 */
@Entity
@Table(name = "bs_province")
public class Province extends AbstractDomain {

	/** 省份编号 */
	private String number;

	/** 省份名称 */
	private String name;

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
}

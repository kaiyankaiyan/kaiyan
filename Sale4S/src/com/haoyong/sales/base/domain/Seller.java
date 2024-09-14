package com.haoyong.sales.base.domain;

import javax.persistence.Column;

import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.domain.LoginDomain;

/**
 *	商家
 */
public class Seller extends AbstractDomain implements LoginDomain {

	// 商家编号
	private String number;
	// 商家名称
	private String name;
	// 所使用的数据库名称
	private String sqlName;
	
	// 省份名称
	private String provinceName;
	// 城市名称
	private String cityName;
	// 商家联系电话，地址
	private String linkerCall, address;

	@Column(length = 50)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	@Column(length = 36)
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	@Column(length = 50)
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	
	@Column(length = 50)
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	@Column(length = 50)
	public String getLinkerCall() {
		return linkerCall;
	}
	public void setLinkerCall(String linkerCall) {
		this.linkerCall = linkerCall;
	}

	@Column(length = 50)
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getSqlName() {
		return sqlName;
	}

	@Column(length = 50)
	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}
}

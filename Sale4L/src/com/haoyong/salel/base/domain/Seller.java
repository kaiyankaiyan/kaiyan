package com.haoyong.salel.base.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.haoyong.salel.common.dao.SerialNumberFactory;
import com.haoyong.salel.common.domain.AbstractDomain;
import com.haoyong.salel.common.domain.Serialy;

/**
 *	商家
 */
@Entity
@Table(name = "bs_seller")
public class Seller extends AbstractDomain implements Serialy {

	// 商家编号
	private String number;
	// 商家名称
	private String name;
	// 所使用的数据库名称
	private String sqlName;
	
	// 省份编号，名称
	private String provinceNumber, provinceName;
	// 城市编号，名称
	private String cityNumber, cityName;
	// 商家联系电话，地址
	private String linkerCall, address;
	
	public void genSerialNumber() {
		if (this.number == null) {
			String serial = SerialNumberFactory.serialLen3(this.getCityNumber(), SerialNumberFactory.Year, 1).getNextSerial();
			this.number = serial;
		}
	}
	
	@Transient
	public String getSerialNumber() {
		return this.number;
	}
	
	@Column(length = 50)
	public String getProvinceNumber() {
		return provinceNumber;
	}

	public void setProvinceNumber(String provinceNumber) {
		this.provinceNumber = provinceNumber;
	}

	@Column(length = 36)
	public String getCityNumber() {
		return cityNumber;
	}

	public void setCityNumber(String cityNumber) {
		this.cityNumber = cityNumber;
	}
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

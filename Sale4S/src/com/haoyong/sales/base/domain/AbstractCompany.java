package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.haoyong.sales.common.domain.AbstractDomain;

/**
 * 供应商
 */
@Entity
@Table(name="bs_company")
@DiscriminatorColumn(discriminatorType=DiscriminatorType.INTEGER)
public abstract class AbstractCompany extends AbstractDomain {
	
	// 供应商编号
	private String number;
	// 地址
	private String address;
	// 供应商名称
	private String name;
	// 编辑日期
	private String uCreate;
	
	// 省份、城市、区域
	private String province, city, area;
	// 联系人
	private String linker;
	private String linkerQQ;
	private String linkerEmail;
	private String linkerCall;
	
	// 银行名称账号
	private String bankName;
	
	// 法人
	private String leader;
	// 备注
	private String remark;
	// 传真
	private String chuanzhen;

	@Column(length=50)
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Column(length=100)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(length=100)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length=500)
	public String getLinker() {
		return linker;
	}

	public void setLinker(String linker) {
		this.linker = linker;
	}

	public String getLinkerCall() {
		return linkerCall;
	}

	public void setLinkerCall(String linkerCall) {
		this.linkerCall = linkerCall;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(length=50)
	public String getChuanzhen() {
		return chuanzhen;
	}

	public void setChuanzhen(String chuanzhen) {
		this.chuanzhen = chuanzhen;
	}

	@Column(length=30)
	public String getLinkerQQ() {
		return linkerQQ;
	}

	public void setLinkerQQ(String linkerQQ) {
		this.linkerQQ = linkerQQ;
	}

	@Column(length=30)
	public String getLinkerEmail() {
		return linkerEmail;
	}

	public void setLinkerEmail(String linkerEmail) {
		this.linkerEmail = linkerEmail;
	}

	@Column(length=50)
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	@Column(length=30)
	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	@Column(length=30)
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	@Column(length=30)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(length=30)
	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Column(length=50)
	public String getUCreate() {
		return uCreate;
	}

	public void setUCreate(String create) {
		uCreate = create;
	}
}

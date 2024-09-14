package com.haoyong.sales.base.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.domain.PropertyChoosable;
import com.haoyong.sales.sale.domain.BomDetail;

/**
 * 商品
 */
@Entity
@Table(name = "bs_commodity")
public class Commodity extends AbstractDomain implements PropertyChoosable<Commodity> {

	// 产品编号，人工编号
	private String commNumber,number2;
	// 产品名称
	private String name;
	// 产品型号
	private String model;
	// 产品规格
	private String spec;
	// 款式图
	private String picture;
	// 产品颜色
	private String color;
	// 厂家/品牌
	private String factory;
	// 单位
	private String unit;
	// 供货方式
	private String supplyType;
	// 基准价格
	private String price;
	// 大类
	private String commType;
	// 小类
	private String commType2;
	// 安全库存量
	private double minInventory;
	// 最大库存量
	private double maxInventory;
	// Bom表材料明细
	private List<BomDetail> bomDetails;
	
	// 电阻值
	private String zuzhi;
	// 单位重量,重量
	private double aweight;
	// 防伪编码，条形码
	private String antiNum, barNum;
	// 贴标型号
	private String labelModel;
	
	
	private String remark;

	public void genSerialNumber() {
		if (this.commNumber == null) {
			String serial = new SerialNumberFactory().serialLen3("S", null, 1).getNextSerial();
			this.commNumber = serial;
		}
	}
	
	@Transient
	private Commodity getChoose() {
		return this;
	}
	
	@Transient
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"name"});
	}

	@Transient
	public String getNumber() {
		return commNumber;
	}

	@Column(length=100)
	public String getCommNumber() {
		return commNumber;
	}

	public void setCommNumber(String number) {
		this.commNumber = number;
	}

	@Column(length=100)
	public String getNumber2() {
		return number2;
	}

	public void setNumber2(String number2) {
		this.number2 = number2;
	}

	@Column(length=100)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length=50)
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Column(length=50)
	public String getSpec() {
		return spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
	}

	@Column(length=50)
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Column(length=100)
	public String getFactory() {
		return factory;
	}

	public void setFactory(String factory) {
		this.factory = factory;
	}

	@Column(length=50)
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getMinInventory() {
		return minInventory;
	}

	public void setMinInventory(double minInventory) {
		this.minInventory = minInventory;
	}

	public double getMaxInventory() {
		return maxInventory;
	}

	public void setMaxInventory(double maxInventory) {
		this.maxInventory = maxInventory;
	}

	@Column(length=10)
	public String getSupplyType() {
		return supplyType;
	}

	public void setSupplyType(String supplyType) {
		this.supplyType = supplyType;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	@Column(length=50)
	public String getCommType() {
		return commType;
	}

	public void setCommType(String ctype) {
		this.commType = ctype;
	}

	@Column(length=50)
	public String getCommType2() {
		return commType2;
	}

	public void setCommType2(String ctype2) {
		this.commType2 = ctype2;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(length=50)
	public String getZuzhi() {
		return zuzhi;
	}

	public void setZuzhi(String zuzhi) {
		this.zuzhi = zuzhi;
	}

	public double getAweight() {
		return aweight;
	}

	public void setAweight(double weight) {
		this.aweight = weight;
	}

	@Column(length=50)
	public String getAntiNum() {
		return antiNum;
	}

	public void setAntiNum(String antiNum) {
		this.antiNum = antiNum;
	}

	@Column(length=50)
	public String getBarNum() {
		return barNum;
	}

	public void setBarNum(String barNum) {
		this.barNum = barNum;
	}

	public String getLabelModel() {
		return labelModel;
	}

	public void setLabelModel(String labelModel) {
		this.labelModel = labelModel;
	}

	@Column(length=4000)
	@Deprecated
	public String getBDetails() {
		return new BomTicketLogic().getToJson(this.bomDetails);
	}

	public void setBDetails(String json) {
		this.bomDetails = new BomTicketLogic().getToMaterials(json);
	}
	
	@Transient
	public List<BomDetail> getBomDetails() {
		return this.bomDetails;
	}
	
	public void setBomDetails(List<BomDetail> list) {
		this.bomDetails = list;
	}
}

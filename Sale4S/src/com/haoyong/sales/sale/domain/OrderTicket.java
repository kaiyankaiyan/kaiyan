package com.haoyong.sales.sale.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.domain.PropertyChoosable;

@Entity
@Table(name="sa_orderticket")
public class OrderTicket extends AbstractDomain implements PropertyChoosable<OrderTicket> {
	
	// 订单号，人工编号
	private String number,number2;
	// 订单类型
	private String orderType;
	// 订单日期
	private Date orderDate=new Date();
	// 希望交期
	private Date hopeDate;
	// 下单人，客服人员
	private String createUser,supportMan;
	private String remark;
	// 客户，分公司
	private String clientName, subName;
	
	private LocationTicket locationTicket = new LocationTicket();
//********************************************************** 单头项目属性
	// 项目编号
	private String proNumber;
	// 项目名称
	private String proName;
	// 项目类型，多选
	private String proType;
	// 省份、城市、区域
	private String province, city, area;
	// 业务人员
	private String saleMan;
	// 方案人员
	private String designMan;
	// 工程内容
	private String proOptions;
	// 约有网点数
	private String siteAmount;
	// 开票单位
	private String billUnit;
	
//********************************************************** 统计属性
	// 订单，安排了的，可发，发货了的，付款了的
	private String OrderT;
	private String Arrange;
	private String Receipt,Send,Paid;
	
//********************************************************** 明细属性
	// 码号数量
	private int XS,S,M,L,XL,X2L,X3L;
	
	
	// 一条明细数量的合重量
	private double weight;
	// 售价，总价格
	private double cprice, cmoney;
	// 其它金额(加入cmoney)，已付款金额
	private double otherMoney, paidedMoney;
	// 顺序号
	private double rowi;
	// 明细选项
	private String doption;
	// 下单商品要求
	private String spnote;
	// 税率, 不含税价格
	private double taxRate, untaxPrice;
	
//********************************************************** 处理属性
	@Transient
	private OrderTicket getTicket() {
		return this;
	}
	@Transient
	private OrderTicket getProject() {
		return this;
	}
	@Transient
	private OrderTicket getDetail() {
		return this;
	}
	@Transient
	private OrderTicket getHandle() {
		return this;
	}
	@Transient
	public LocationTicket getLocationTicket() {
		return this.locationTicket;
	}
	@Transient
	public List<String> getTrunkDefault() {
		return new ArrayList<String>(0);
	}
	
	public String genSerialNumber() {
		if (this.number == null) {
			String serial = new SerialNumberFactory().serialLen2("DD", SerialNumberFactory.Date, 5).getNextSerial();
			this.number = serial;
		}
		return this.number;
	}
	public String genSerialNumber4Sale() {
		if (this.number == null) {
			String serial = new SerialNumberFactory().serialLen2("XS", SerialNumberFactory.Date, 5).getNextSerial();
			this.number = serial;
		}
		return this.number;
	}
	public String genSerialNumber4Project() {
		if (this.proNumber == null) {
			String serial = new SerialNumberFactory().serialLen2("XM", SerialNumberFactory.Date, 5).getNextSerial();
			this.proNumber = serial;
		}
		return this.proNumber;
	}
	
	@Column(length=50)
	@Index(name="ioNumber")
	public String getNumber() {
		return number;
	}

	@Column(length=50)
	public void setNumber(String number) {
		this.number = number;
	}
	public String getNumber2() {
		return number2;
	}

	@Column(length=50)
	public void setNumber2(String number2) {
		this.number2 = number2;
	}

	@Temporal(TemporalType.DATE)
	public Date getHopeDate() {
		return hopeDate;
	}
	public void setHopeDate(Date hopeDate) {
		this.hopeDate = hopeDate;
	}

	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getSubName() {
		return subName;
	}
	public void setSubName(String subName) {
		this.subName = subName;
	}
	
	@Temporal(TemporalType.DATE)
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	@Column(length=10)
	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@Transient
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Transient
	public double getTaxRate() {
		return taxRate;
	}
	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}
	
	@Transient
	public String getDoption() {
		return doption;
	}
	public void setDoption(String doption) {
		this.doption = doption;
	}
	
	public String getProType() {
		return this.proType;
	}
	public void setProType(String type) {
		this.proType = type;
	}
	
	@Transient
	public double getRowi() {
		return rowi;
	}
	public void setRowi(double rowi) {
		this.rowi = rowi;
	}
	
	@Transient
	public String getSpnote() {
		return spnote;
	}
	public void setSpnote(String spnote) {
		this.spnote = spnote;
	}
	
	@Transient
	public double getUntaxPrice() {
		return untaxPrice;
	}
	public void setUntaxPrice(double untaxPrice) {
		this.untaxPrice = untaxPrice;
	}
	
	@Transient
	public double getCprice() {
		return cprice;
	}
	public void setCprice(double cprice) {
		this.cprice = cprice;
	}
	
	@Transient
	public double getCmoney() {
		return cmoney;
	}
	public void setCmoney(double cmoney) {
		this.cmoney = cmoney;
	}
	
	@Column(length=50)
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	@Column(length=50)
	public String getSupportMan() {
		return supportMan;
	}
	public void setSupportMan(String supportMan) {
		this.supportMan = supportMan;
	}

	@Column(length=50)
	public String getProNumber() {
		return proNumber;
	}
	public void setProNumber(String proNumber) {
		this.proNumber = proNumber;
	}
	
	public String getProName() {
		return proName;
	}
	public void setProName(String proName) {
		this.proName = proName;
	}
	
	@Column(length=50)
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	
	@Column(length=50)
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	
	public String getSaleMan() {
		return saleMan;
	}
	public void setSaleMan(String salesMan) {
		this.saleMan = salesMan;
	}
	
	public String getDesignMan() {
		return designMan;
	}
	public void setDesignMan(String designMan) {
		this.designMan = designMan;
	}
	
	public String getProOptions() {
		return proOptions;
	}
	public void setProOptions(String proOptions) {
		this.proOptions = proOptions;
	}
	
	public String getSiteAmount() {
		return siteAmount;
	}
	public void setSiteAmount(String siteAmount) {
		this.siteAmount = siteAmount;
	}

	public String getOrderT() {
		return OrderT;
	}
	public void setOrderT(String orderT) {
		this.OrderT = orderT;
	}
	
	public String getArrange() {
		return Arrange;
	}
	public void setArrange(String arrange) {
		this.Arrange = arrange;
	}
	
	public String getReceipt() {
		return Receipt;
	}
	public void setReceipt(String receipt) {
		this.Receipt = receipt;
	}
	
	public String getSend() {
		return Send;
	}
	public void setSend(String send) {
		this.Send = send;
	}
	
	public String getPaid() {
		return Paid;
	}
	public void setPaid(String paid) {
		this.Paid = paid;
	}
	
	public String getBillUnit() {
		return billUnit;
	}
	public void setBillUnit(String billUnit) {
		this.billUnit = billUnit;
	}
}

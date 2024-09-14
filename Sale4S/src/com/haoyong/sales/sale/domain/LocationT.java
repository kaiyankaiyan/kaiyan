package com.haoyong.sales.sale.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class LocationT implements TAlongable {

	private String locationLabel, locationName, locationOther;
	private String outLabel, outName, outOther;
	private String inLabel, inName, inOther;
	private String toLabel, toName, toOther;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{locationLabel, locationName, locationOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.locationLabel = names;
		this.locationName = values;
		this.locationOther = others;
	}
	
	@Transient
	public String[] getOutValue() {
		return new String[]{outLabel, outName, outOther};
	}
	
	public void setOutValue(String names, String values, String others) {
		this.outLabel = names;
		this.outName = values;
		this.outOther = others;
	}

	@Transient
	public String[] getInValue() {
		return new String[]{inLabel, inName, inOther};
	}
	
	public void setInValue(String names, String values, String others) {
		this.inLabel = names;
		this.inName = values;
		this.inOther = others;
	}

	@Transient
	public String[] getToValue() {
		return new String[]{toLabel, toName, toOther};
	}
	
	public void setToValue(String names, String values, String others) {
		this.toLabel = names;
		this.toName = values;
		this.toOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return null;
	}
	
	public void setHandleValue(String names, String values) {
	}

	@Column(length=100)
	public String getLocationLabel() {
		return locationLabel;
	}

	public void setLocationLabel(String wbillLabel) {
		this.locationLabel = wbillLabel;
	}

	@Column(length=100)
	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String wbillName) {
		this.locationName = wbillName;
	}

	public String getLocationOther() {
		return locationOther;
	}

	public void setLocationOther(String locationOther) {
		this.locationOther = locationOther;
	}

	@Column(length=100)
	public String getOutLabel() {
		return outLabel;
	}
	public void setOutLabel(String outLabel) {
		this.outLabel = outLabel;
	}

	@Column(length=100)
	public String getOutName() {
		return outName;
	}
	public void setOutName(String outName) {
		this.outName = outName;
	}

	public String getOutOther() {
		return outOther;
	}
	public void setOutOther(String outOther) {
		this.outOther = outOther;
	}

	@Column(length=100)
	public String getInLabel() {
		return inLabel;
	}
	public void setInLabel(String inLabel) {
		this.inLabel = inLabel;
	}

	@Column(length=100)
	public String getInName() {
		return inName;
	}
	public void setInName(String inName) {
		this.inName = inName;
	}

	public String getInOther() {
		return inOther;
	}
	public void setInOther(String inOther) {
		this.inOther = inOther;
	}
	
	@Column(length=100)
	public String getToLabel() {
		return toLabel;
	}
	public void setToLabel(String toLabel) {
		this.toLabel = toLabel;
	}

	@Column(length=100)
	public String getToName() {
		return toName;
	}
	public void setToName(String toName) {
		this.toName = toName;
	}

	public String getToOther() {
		return toOther;
	}
	public void setToOther(String toOther) {
		this.toOther = toOther;
	}

	@Transient
	public String getTicket() {
		String name=this.locationName, other=this.locationOther;
		return name==null? "": new StringBuffer().append(name==null? "": name)
				.append(this.outName)
				.append(this.inName)
				.append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getOut() {
		String name=this.outName, other=this.outOther;
		return new StringBuffer().append(name==null? "": name)
				.append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getIn() {
		String name=this.inName, other=this.inOther;
		return new StringBuffer().append(name==null? "": name)
				.append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
}

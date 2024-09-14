package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;


@Embeddable
public class SupplierT implements TAlongable {
	
	private String supplierLabel, supplierName, supplierOther;
	private String supplierLabelH, supplierNameH;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{supplierLabel, supplierName, supplierOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.supplierLabel = names;
		this.supplierName = values;
		this.supplierOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{supplierLabelH, supplierNameH, null};
	}
	
	public void setHandleValue(String names, String values) {
		this.supplierLabelH = names;
		this.supplierNameH = values;
	}

	@Column(length=100)
	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	@Column(length=100)
	public String getSupplierLabel() {
		return supplierLabel;
	}

	public void setSupplierLabel(String supplierLabel) {
		this.supplierLabel = supplierLabel;
	}

	public String getSupplierOther() {
		return supplierOther;
	}

	public void setSupplierOther(String supplierOther) {
		this.supplierOther = supplierOther;
	}
	
	@Column(length=100)
	public String getSupplierLabelH() {
		return supplierLabelH;
	}

	public void setSupplierLabelH(String supplierLabelH) {
		this.supplierLabelH = supplierLabelH;
	}

	@Column(length=100)
	public String getSupplierNameH() {
		return supplierNameH;
	}

	public void setSupplierNameH(String supplierNameH) {
		this.supplierNameH = supplierNameH;
	}

	@Transient
	public String getTrunk() {
		String name=this.supplierName, other=this.supplierOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	private String getHandle() {
		String name=this.supplierNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}

package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

/**
 * 分公司、子公司、工程分公司
 */
@Embeddable
public class SubCompanyT implements TAlongable {

	private String subLabel, subName, subOther;
	private String subLabelH, subNameH;

	@Transient
	public String[] getChooseValue() {
		return new String[]{subLabel, subName, subOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.subLabel = names;
		this.subName = values;
		this.subOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{subLabelH, subNameH, null};
	}
	
	public void setHandleValue(String names, String values) {
		this.subLabelH = names;
		this.subNameH = values;
	}
	
	@Column(length=100)
	public String getSubName() {
		return subName;
	}
	
	public void setSubName(String clientName) {
		this.subName = clientName;
	}

	@Column(length=100)
	public String getSubLabel() {
		return subLabel;
	}

	public void setSubLabel(String clientLabel) {
		this.subLabel = clientLabel;
	}

	public String getSubOther() {
		return subOther;
	}

	public void setSubOther(String subOther) {
		this.subOther = subOther;
	}

	@Column(length=100)
	public String getSubLabelH() {
		return subLabelH;
	}

	public void setSubLabelH(String subLabelH) {
		this.subLabelH = subLabelH;
	}

	@Column(length=100)
	public String getSubNameH() {
		return subNameH;
	}

	public void setSubNameH(String subNameH) {
		this.subNameH = subNameH;
	}

	@Transient
	private String getTrunk() {
		String name=this.subName, other=this.subOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	private String getHandle() {
		String name=this.subNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}

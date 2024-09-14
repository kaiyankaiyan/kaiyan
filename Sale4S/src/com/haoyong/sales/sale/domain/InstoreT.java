package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.sf.mily.types.DateType;

import com.haoyong.sales.common.domain.TAlongable;

public class InstoreT implements TAlongable {

	private String instoreLabel, instoreName, instoreOther;
	private String instoreLabelH, instoreNameH;
	private Date instoreDate;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{instoreLabel, instoreName, instoreOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.instoreLabel = names;
		this.instoreName = values;
		this.instoreOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{instoreLabelH, instoreNameH};
	}
	
	public void setHandleValue(String names, String values) {
		this.instoreLabelH = names;
		this.instoreNameH = values;
	}

	@Column(length=100)
	public String getInstoreLabel() {
		return instoreLabel;
	}

	public void setInstoreLabel(String instoreLabel) {
		this.instoreLabel = instoreLabel;
	}

	@Column(length=100)
	public String getInstoreName() {
		return instoreName;
	}

	public void setInstoreName(String instoreName) {
		this.instoreName = instoreName;
	}

	@Temporal(TemporalType.DATE)
	public Date getInstoreDate() {
		return instoreDate;
	}

	public void setInstoreDate(Date instoreDate) {
		this.instoreDate = instoreDate;
	}

	@Column(length=100)
	public String getInstoreLabelH() {
		return instoreLabelH;
	}

	public void setInstoreLabelH(String instoreLabelH) {
		this.instoreLabelH = instoreLabelH;
	}

	@Column(length=100)
	public String getInstoreNameH() {
		return instoreNameH;
	}

	public void setInstoreNameH(String instoreNameH) {
		this.instoreNameH = instoreNameH;
	}

	public String getInstoreOther() {
		return instoreOther;
	}

	public void setInstoreOther(String instoreOther) {
		this.instoreOther = instoreOther;
	}
	
	@Transient
	public String getTicket() {
		String name=this.instoreName, other=this.instoreOther, date=new DateType().format(this.instoreDate);
		return new StringBuffer().append(name==null? "": name).append(date==null? "": date).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getHandle() {
		String name=this.instoreNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}

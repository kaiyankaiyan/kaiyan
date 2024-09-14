package com.haoyong.sales.sale.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class ArrangeT implements TAlongable {

	private String arrangeLabel, arrangeName, arrangeOther;
	private String arrangeLabelH, arrangeNameH;
	
	private String arrangeType;
	private String deliverNote;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{arrangeLabel, arrangeName, arrangeOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.arrangeLabel = names;
		this.arrangeName = values;
		this.arrangeOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{arrangeLabelH, arrangeNameH, null};
	}
	
	public void setHandleValue(String names, String values) {
		this.arrangeLabelH = names;
		this.arrangeNameH = values;
	}
	
	@Column(length=100)
	public String getArrangeLabel() {
		return arrangeLabel;
	}
	
	public void setArrangeLabel(String arrangeLabel) {
		this.arrangeLabel = arrangeLabel;
	}
	
	@Column(length=100)
	public String getArrangeName() {
		return arrangeName;
	}
	
	public void setArrangeName(String arrangeName) {
		this.arrangeName = arrangeName;
	}

	@Column(length=10)
	public String getArrangeType() {
		return arrangeType;
	}
	
	public void setArrangeType(String arrangeType) {
		this.arrangeType = arrangeType;
	}

	@Column(length=100)
	public String getDeliverNote() {
		return deliverNote;
	}

	public void setDeliverNote(String deliverNote) {
		this.deliverNote = deliverNote;
	}

	public String getArrangeLabelH() {
		return arrangeLabelH;
	}

	public void setArrangeLabelH(String arrangeLabelH) {
		this.arrangeLabelH = arrangeLabelH;
	}

	public String getArrangeNameH() {
		return arrangeNameH;
	}

	public void setArrangeNameH(String arrangeNameH) {
		this.arrangeNameH = arrangeNameH;
	}

	public String getArrangeOther() {
		return arrangeOther;
	}

	public void setArrangeOther(String arrangeOther) {
		this.arrangeOther = arrangeOther;
	}
	
	@Transient
	protected String getTicket() {
		String name=this.arrangeName, other=this.arrangeOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getHandle() {
		String name=this.arrangeNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}

package com.haoyong.sales.sale.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.AbstractDomain;

@Entity
@Table(name="sa_printmodel")
public class PrintModel extends AbstractDomain {
	
	private String builder;
	
	private String name;

	// 明细内容为后缀的{__tr__行}
	private StringBuffer content=new StringBuffer();

	private String ucreate;
	
	public String getBuilder() {
		return builder;
	}

	public void setBuilder(String builder) {
		this.builder = builder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getUcreate() {
		return ucreate;
	}

	public void setUcreate(String ucreate) {
		this.ucreate = ucreate;
	}

	@Transient
	public StringBuffer getContent() {
		return this.content;
	}
	
	@Column(columnDefinition="text")
	private String getPContent() {
		return this.content.toString();
	}
	private void setPContent(String s) {
		this.content.delete(0, this.content.length());
		if (s!=null)
			this.content.append(s);
	}
}

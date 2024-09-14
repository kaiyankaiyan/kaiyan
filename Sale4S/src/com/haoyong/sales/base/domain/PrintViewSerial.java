package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.haoyong.sales.common.derby.AbstractDerby;

@Entity
@Table(name="bs_printviewserial")
public class PrintViewSerial extends AbstractDerby {
	
	// 界面路径按钮，界面说明备注
	private String path, note;
	private int level;
	private StringBuffer content=new StringBuffer();
	
	public String getPath() {
		return path;
	}
	public void setPath(String builder) {
		this.path = builder;
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}

	@Transient
	public StringBuffer getContent() {
		return this.content;
	}
	
	@Column(columnDefinition="CLOB")
	private String getPContent() {
		return this.content.toString();
	}
	private void setPContent(String s) {
		this.content.delete(0, this.content.length());
		if (s!=null)
			this.content.append(s);
	}
}

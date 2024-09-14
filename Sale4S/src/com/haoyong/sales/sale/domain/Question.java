package com.haoyong.sales.sale.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.sf.mily.common.Notely;

import com.haoyong.sales.common.domain.AbstractDomain;

/**
 * 操作用户反馈提问，让产品部解决问题
 */
@Entity
@Table(name = "sa_question")
public class Question extends AbstractDomain implements Notely {

	/**
	 * 标题
	 */
	private String title;
	private int stQuestion;
	
	/**
	 * 功能点
	 */
	private String rightName;
	
	/**
	 * 问题描述
	 */
	private String question;
	
	/**
	 * 回答
	 */
	private String reply;
	
	/**
	 * 提问者
	 */
	private String questioner;
	
	/**
	 * 回答者
	 */
	private String replier;
	
	/**
	 * 指定处理人
	 */
	private String handler;
	
	private String stateName;
	
	/**
	 * 单据过程备注
	 */
	private StringBuffer stateNotes = new StringBuffer();
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(columnDefinition="text")
	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	@Column(columnDefinition="text")
	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public String getRightName() {
		return rightName;
	}

	public void setRightName(String functionPoints) {
		this.rightName = functionPoints;
	}

	@Column(length=50)
	public String getQuestioner() {
		return questioner;
	}

	public void setQuestioner(String questionerName) {
		this.questioner = questionerName;
	}

	@Column(length=50)
	public String getReplier() {
		return replier;
	}

	public void setReplier(String replierName) {
		this.replier = replierName;
	}

	@Column(length=50)
	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public int getStQuestion() {
		return stQuestion;
	}

	public void setStQuestion(int stQuestion) {
		this.stQuestion = stQuestion;
	}

	@Column(length=50)
	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	
	@Transient
	public StringBuffer getStateBuffer() {
		return this.stateNotes;
	}
	
	@Column(length=1000)
	private String getStateNotes() {
		int len=1000;
		if (this.stateNotes.length()>len)
			this.stateNotes.delete(0, this.stateNotes.length()-len);
		return this.stateNotes.toString();
	}
	
	public void setStateNotes(String notes) {
		this.stateNotes.delete(0, this.stateNotes.length()).append(notes);
	}
}

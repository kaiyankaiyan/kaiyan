package com.haoyong.salel.common.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import net.sf.mily.bean.BeanClass;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.webObject.query.Identity;

/**
 * <pre>
 * Title: 所有Domain的基类
 * Description:
 * </pre>
 */
@MappedSuperclass
public abstract class AbstractDomain implements Serializable, Cloneable, Identity {
	
	private static final long serialVersionUID = 5770618979137565238L;
	
	/**
	 * 唯一标识
	 */
	private long id;

	/**
	 * 版本号
	 */
	private int version;

	/**
	 * 最后更新日期
	 */
	private Date modifytime;
	
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 状态
	 */
	private State state = State.Save();
	
	/**
	 * 界面参数值列表
	 */
	private LinkedHashMap<String,Object> voParamMap=new LinkedHashMap<String, Object>(0);

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;//can't happen
		}
	}
	
	/**
	 * 可重载改为一个类只get一次
	 * @return
	 */
	public BeanClass beanClass(){
		return BeanClass.getBeanClass(getClass());
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Version
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Column(columnDefinition="DATETIME(3)")
	public Date getModifytime() {
		return modifytime;
	}

	public void setModifytime(Date modifytime) {
		this.modifytime = modifytime;
	}
	
	private int getStateId() {
		return this.getState().getId();
	}
	
	private void setStateId(int stateId) {
		this.getState().setId(stateId);
	}

	@Transient
	public State getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(State state) {
		this.state = state;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || (!this.getClass().equals(obj.getClass()))) {
			return false;
		}
		// 支持空对象（只有id，没有其它值）的比较
		if (id > 0) {
			return id == ((AbstractDomain) obj).getId();
		} else if (this==obj) {
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		if (id > 0) {
			return (int) id;
		}
		return super.hashCode();
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@Transient
	public Map<String, Object> getVoParamMap() {
		return voParamMap;
	}
	
	public <P extends Object> P getVoparam(Class<P> clss) {
		return (P)getVoParamMap().get(clss.getSimpleName());
	}
	
	public <P extends Object> void setVoparam(P value) {
		if (value!=null)
			getVoParamMap().put(value.getClass().getSimpleName(), value);
	}
	
	@Transient
	public <D extends AbstractDomain> D getSnapShot() {
		String k = "Self";
		D self = (D)getVoParamMap().get(k);
		if (self == null) {
			self = (D)TicketPropertyUtil.deepClone(this);
			getVoParamMap().put(k, self);
		}
		return self;
	}
}

package com.haoyong.sales.common.derby;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Index;


import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.webObject.query.Identity;


/**
 * <pre>
 * Title: 所有Domain的基类
 * Description:
 * </pre>
 */
@MappedSuperclass
public abstract class AbstractDerby implements Serializable, Cloneable, Identity {
	
	private static final long serialVersionUID = 5770618979137565238L;
	
	/**
	 * 唯一标识
	 */
	private long id;

	/**
	 * 商家Id
	 */
	private long sellerId;

	/**
	 * 版本号
	 */
	private int version;

	/**
	 * 最后更新日期
	 */
	private Date modifytime;
	
	/**
	 * 界面参数值列表
	 */
	private LinkedHashMap<String,Object> voParamMap=new LinkedHashMap<String, Object>(0);

	public long getSellerId() {
		return sellerId;
	}

	public void setSellerId(long sellerId) {
		this.sellerId = sellerId;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;//can't happen
		}
	}
	
	@Transient
	public <D extends AbstractDerby> D getSnapShot() {
		String k = "SelfSnapShot";
		D self = (D)getVoParamMap().get(k);
		if (self==null || self==this || self.getClass()!=this.getClass()) {
			self = (D)TicketPropertyUtil.deepClone(this);
			this.getVoParamMap().put(k, self);
		}
		return self;
	}
	
	public void setSnapShot1() {
		Object preShot = this.getSnapShot();
		this.getVoParamMap().remove("SelfSnapShot");
		this.getSnapShot().setVoparam(preShot);
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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

	@Temporal(TemporalType.TIMESTAMP)
	public Date getModifytime() {
		return modifytime;
	}

	public void setModifytime(Date modifytime) {
		this.modifytime = modifytime;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || (!this.getClass().equals(obj.getClass()))) {
			return false;
		}
		// 支持空对象（只有id，没有其它值）的比较
		if (id > 0) {
			return id == ((AbstractDerby) obj).getId();
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

	@Transient
	public Map<String, Object> getVoParamMap() {
		return voParamMap;
	}
	
	public <P extends Object> P getVoparam(String clssName) {
		return (P)getVoParamMap().get(clssName);
	}
	
	public <P extends Object> P getVoparam(Class<P> clss) {
		return (P)getVoParamMap().get(clss.getSimpleName());
	}
	
	public <P extends Object> void setVoparam(P value) {
		if (value!=null)
			getVoParamMap().put(value.getClass().getSimpleName(), value);
	}
}

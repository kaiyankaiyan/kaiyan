package com.haoyong.sales.base.domain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.haoyong.sales.common.domain.AbstractDomain;

/**
 * 分类库
 */
@Entity
@Table(name = "bs_typeinfos")
public class TypeInfos extends AbstractDomain {
	
	private String clss;
	
	private String infos;
	
	private List infoList;
	private Type gtype;
	
	private String getClss() {
		return clss;
	}
	
	private void setClss(String clss) {
		this.clss = clss;
	}
	
	public void setClssName(Class clss) {
		this.clss = clss.getSimpleName();
	}

	private String getInfos() {
		return infos;
	}

	private void setInfos(String infos) {
		this.infos = infos;
	}
	
	public void setType(Type gtype) {
		this.gtype = gtype;
	}
	
	@Transient
	public <T extends TInfo> List<T> getInfoList() {
		if (infoList!=null)			return this.infoList;
		if (StringUtils.isEmpty(this.infos)) {
			this.infoList = new ArrayList<T>();
		} else {
			Gson gson = new Gson();
			this.infoList = gson.fromJson(this.getInfos(), gtype);
		}
		return this.infoList;
	}
	
	public <T extends TInfo> void setInfoList(List<T> infoList) {
		Gson gson = new Gson();
		if (infoList.isEmpty()) {
			this.infos = null;
			this.infoList = infoList;
		} else {
			this.infos = gson.toJson(infoList, gtype);
			this.infoList = infoList;
		}
	}
}

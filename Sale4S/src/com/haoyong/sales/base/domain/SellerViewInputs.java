package com.haoyong.sales.base.domain;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.common.derby.AbstractDerby;

/**
 * 商家界面输入值记录
 */
@Entity
@Table(name = "bs_sellerviewinputs")
public class SellerViewInputs extends AbstractDerby {

	private String builderName;
	private String userName;
	
	private LinkedHashMap<String, String> inputs = new LinkedHashMap<String, String>();

	@Column(length=100)
	public String getBuilderName() {
		return builderName;
	}

	public void setBuilderName(String builderName) {
		this.builderName = builderName;
	}
	
	@Column(length=30)
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Transient
	public LinkedHashMap<String, String> getInputs() {
		return this.inputs;
	}
	
	public void setInputs(HashMap<String, String> map) {
		this.inputs.putAll(map);
	}

	@Column(columnDefinition="CLOB")
	private String getInputGs() {
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
		return gson.toJson(inputs, gtype);
	}

	private void setInputGs(String json) {
		Gson gson = new Gson();
		Type gtype = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
		this.inputs = gson.fromJson(json, gtype);
		this.getSnapShot();
	}
}
